const { execFile } = require('node:child_process');
const { promisify } = require('node:util');

const executeFile = promisify(execFile);

async function git(args) {
    const { stdout } = await executeFile('git', [
        '-c', 'log.showSignature=false', '-c', 'i18n.logOutputEncoding=UTF-8', ...args,
    ], {
        encoding: 'utf8',
        maxBuffer: 64 * 1024 * 1024,
    });
    return stdout.trimEnd();
}

async function ensureCompleteHistory() {
    if (await git(['rev-parse', '--is-shallow-repository']) === 'true') {
        throw new Error('Complete Git history is required. Use actions/checkout with fetch-depth: 0.');
    }
}

async function resolveTag(tag) {
    await git(['check-ref-format', `refs/tags/${tag}`]);
    return git(['rev-parse', '--verify', '--end-of-options', `refs/tags/${tag}^{commit}`]);
}

async function isAncestor(base, head) {
    try {
        await git(['merge-base', '--is-ancestor', base, head]);
        return true;
    } catch (error) {
        if (error.code === 1) return false;
        throw error;
    }
}

async function selectPreviousRelease(releases, current, head, previousTag, excludedTags) {
    if (previousTag) {
        if (excludedTags.has(previousTag)) throw new Error('The previous tag must not be excluded.');
        const sha = await resolveTag(previousTag);
        if (previousTag === current.tag_name || !(await isAncestor(sha, head))) {
            throw new Error('The previous tag must be a different tag in the target tag history.');
        }
        return { tag: previousTag, sha };
    }

    const tags = new Set((await git(['tag', '--list'])).split('\n'));
    const candidates = releases.filter(release =>
        !release.draft && release.tag_name !== current.tag_name && !excludedTags.has(release.tag_name)
        && (current.prerelease || !release.prerelease)
        && release.published_at <= current.published_at
        && tags.has(release.tag_name));
    candidates.sort((left, right) => right.published_at.localeCompare(left.published_at));

    let previous;
    let shortestDistance = Infinity;
    for (const release of candidates) {
        const sha = await resolveTag(release.tag_name);
        if (!(await isAncestor(sha, head))) continue;
        const distance = Number(await git(['rev-list', '--count', `${sha}..${head}`]));
        if (distance < shortestDistance) {
            previous = { tag: release.tag_name, sha };
            shortestDistance = distance;
        }
    }
    return previous;
}

async function readCommits(base, head) {
    const output = await git([
        'log', '--reverse', '--topo-order', '--no-show-signature', '--abbrev=12', '-z',
        '--format=%H%x00%h%x00%P%x00%s%x00%an', base ? `${base}..${head}` : head, '--',
    ]);
    if (!output) return [];
    const fields = output.split('\0');
    const commits = [];
    for (let index = 0; index + 4 < fields.length; index += 5) {
        commits.push({
            sha: fields[index],
            shortSha: fields[index + 1],
            parents: fields[index + 2].split(' ').filter(Boolean),
            title: fields[index + 3],
            authorName: fields[index + 4],
        });
    }
    return commits;
}

async function mergedCommits(commit) {
    if (commit.parents.length < 2) return [commit.sha];
    return (await git(['rev-list', `${commit.parents[0]}..${commit.sha}`, '--'])).split('\n');
}

async function readAncestorShas(head) {
    return new Set((await git(['rev-list', head, '--'])).split('\n'));
}

async function hasChangelogOverride(head, tag) {
    const path = `.changelogs/${tag}.md`;
    return (await git(['ls-tree', '-z', '--name-only', head, '--', `:(literal)${path}`])) === `${path}\0`;
}

module.exports = {
    ensureCompleteHistory, resolveTag, selectPreviousRelease, readCommits, mergedCommits, readAncestorShas, hasChangelogOverride,
};
