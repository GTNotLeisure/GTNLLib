const { mergedCommits, readAncestorShas } = require('./history.cjs');

async function mapConcurrent(items, operation) {
    const results = new Array(items.length);
    let next = 0;
    await Promise.all(Array.from({ length: Math.min(4, items.length) }, async () => {
        while (next < items.length) {
            const index = next++;
            results[index] = await operation(items[index]);
        }
    }));
    return results;
}

async function collectContributions({ github, repo, commits, previous, nativeBody, repositoryUrl }) {
    const bySha = new Map(commits.map(commit => [commit.sha, commit]));
    const pullRequests = new Map();
    const associations = await mapConcurrent(commits, async commit => {
        const pulls = await github.paginate(github.rest.repos.listPullRequestsAssociatedWithCommit, {
            ...repo, commit_sha: commit.sha, per_page: 100,
        });
        for (const pull of pulls) {
            if (pull.merged_at && bySha.has(pull.merge_commit_sha)
                && pull.base.repo.full_name.toLowerCase() === `${repo.owner}/${repo.repo}`.toLowerCase()) {
                pullRequests.set(pull.number, pull);
            }
        }
        return { commit, pulls };
    });

    const covered = new Set();
    for (const pull of pullRequests.values()) {
        // Git ancestry covers every side-branch commit, including nested merges, without the PR API's 250-commit cap.
        for (const sha of await mergedCommits(bySha.get(pull.merge_commit_sha))) {
            if (bySha.has(sha)) covered.add(sha);
        }
    }
    for (const { commit, pulls } of associations) {
        // GitHub tracks the rewritten SHAs of rebase merges; open and out-of-range PRs cannot hide commits.
        if (pulls.some(pull => pullRequests.has(pull.number))) covered.add(commit.sha);
    }

    const directCommits = await mapConcurrent(commits.filter(commit => !covered.has(commit.sha)), async commit => {
        const { data } = await github.rest.repos.getCommit({ ...repo, ref: commit.sha });
        return {
            ...commit,
            login: data.author?.login,
            authorType: data.author?.type,
            url: `${repositoryUrl}/commit/${commit.sha}`,
        };
    });

    const nativeNewContributors = new Set(
        [...nativeBody.matchAll(/^\* @([^\s]+) made their first contribution in /gm)]
            .map(match => match[1].toLowerCase()));
    const authors = new Map();
    for (const commit of directCommits) {
        if (commit.login && commit.authorType !== 'Bot' && !nativeNewContributors.has(commit.login.toLowerCase())) {
            // Commits are in chronological topology order, so the first link is stable across reruns.
            if (!authors.has(commit.login.toLowerCase())) authors.set(commit.login.toLowerCase(), commit);
        }
    }
    let previousPullAuthors;
    const contributors = await mapConcurrent([...authors.values()], async commit => {
        let isNew = !previous;
        if (previous) {
            const { data } = await github.rest.repos.listCommits({
                ...repo, sha: previous.sha, author: commit.login, per_page: 1,
            });
            isNew = data.length === 0;
            if (isNew) {
                // A previous PR author may have no authored commit after a squash or a maintainer's rewrite.
                previousPullAuthors ??= Promise.all([
                    github.paginate(github.rest.pulls.list, { ...repo, state: 'closed', per_page: 100 }),
                    readAncestorShas(previous.sha),
                ]).then(([pulls, ancestors]) => new Set(pulls
                    .filter(pull => pull.merged_at && ancestors.has(pull.merge_commit_sha) && pull.user)
                    .map(pull => pull.user.login.toLowerCase())));
                isNew = !(await previousPullAuthors).has(commit.login.toLowerCase());
            }
        }
        return { login: commit.login, url: commit.url, isNew };
    });

    return { commits: directCommits.reverse(), contributors, pullRequestCount: pullRequests.size };
}

module.exports = { collectContributions };
