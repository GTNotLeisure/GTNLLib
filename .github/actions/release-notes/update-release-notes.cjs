const { mkdtemp, writeFile } = require('node:fs/promises');
const { join } = require('node:path');
const { ensureCompleteHistory, resolveTag, selectPreviousRelease, readCommits, hasChangelogOverride } = require('./history.cjs');
const { collectContributions } = require('./collect.cjs');
const { renderReleaseNotes } = require('./render.cjs');

module.exports = async function updateReleaseNotes({
    github, repo, core, tag, previousTag, dryRun = true, excludedTags = [],
}) {
    const excluded = new Set(excludedTags);
    if (!tag || excluded.has(tag)) throw new Error('A non-excluded release tag is required.');
    if (typeof dryRun !== 'boolean') throw new Error('The dry-run option must be a boolean.');

    await ensureCompleteHistory();
    const head = await resolveTag(tag);
    if (await hasChangelogOverride(head, tag)) {
        core.info(`Keeping the explicit changelog override for ${tag}.`);
        core.setOutput('status', 'skipped');
        return;
    }

    const { data: release } = await github.rest.repos.getReleaseByTag({ ...repo, tag });
    if (release.draft) throw new Error('Only published release descriptions can be regenerated.');
    const releases = await github.paginate(github.rest.repos.listReleases, { ...repo, per_page: 100 });
    const previous = await selectPreviousRelease(releases, release, head, previousTag, excluded);
    core.info(`Generating notes for ${previous?.tag || 'the beginning of history'} -> ${tag}.`);

    const commits = await readCommits(previous?.sha, head);
    const { data: notes } = await github.rest.repos.generateReleaseNotes({
        ...repo, tag_name: tag, ...(previous && { previous_tag_name: previous.tag }),
    });
    const repositoryUrl = release.html_url.slice(0, release.html_url.lastIndexOf('/releases/tag/'));
    const contributions = await collectContributions({
        github, repo, commits, previous, nativeBody: notes.body,
    });
    const body = renderReleaseNotes({ nativeBody: notes.body, ...contributions, repositoryUrl, tag, previous });
    if (body.length > 125000) throw new Error('Release notes exceed the GitHub release body limit.');

    const outputDirectory = await mkdtemp(join(process.env.RUNNER_TEMP, 'release-notes-'));
    const outputPath = join(outputDirectory, 'release-notes.md');
    await writeFile(outputPath, body, 'utf8');
    core.setOutput('notes-path', outputPath);
    core.info(`Covered ${contributions.pullRequestCount} merged PRs; added ${contributions.commits.length} direct commits.`);
    await core.summary.addRaw(body).write();

    if (dryRun) {
        core.info('Preview only; the release has not been changed.');
        core.setOutput('status', 'preview');
        return;
    }
    // Detect edits made while collecting notes before replacing the release body.
    const { data: latest } = await github.rest.repos.getRelease({ ...repo, release_id: release.id });
    if (latest.body !== release.body || latest.updated_at !== release.updated_at) {
        throw new Error('The release changed during note generation. Review it and rerun the workflow.');
    }
    if (body !== release.body) {
        await github.rest.repos.updateRelease({ ...repo, release_id: release.id, body });
        core.info(`Updated the description of ${tag}.`);
        core.setOutput('status', 'updated');
    } else {
        core.setOutput('status', 'unchanged');
    }
};
