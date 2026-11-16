function escapeMarkdown(value) {
    return value.replace(/[\r\n\t]+/g, ' ').replace(/[\u0000-\u001f\u007f]/g, '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/([\\`*_{}\[\]~|])/g, '\\$1');
}

function insertSection(body, heading, lines, before) {
    if (lines.length === 0) return body;
    const marker = `## ${heading}\n`;
    if (body.includes(marker)) return body.replace(marker, `${marker}${lines.join('\n')}\n`);
    const index = body.search(before);
    const section = `${marker}${lines.join('\n')}\n\n`;
    return index < 0 ? `${body.trimEnd()}\n\n${section}`.trimStart()
        : `${body.slice(0, index)}${section}${body.slice(index)}`;
}

function renderContributor(author) {
    const noun = author.references.length === 1 ? 'contribution' : 'contributions';
    return `* @${author.login} made their ${author.isNew ? 'first ' : ''}${noun} in ${author.references.join(', ')}`;
}

function renderReleaseNotes({ nativeBody, commits, contributors, repositoryUrl, tag, previous }) {
    let body = `${nativeBody.replace(/\r\n/g, '\n').trim()}\n`;
    const pullUrlPrefix = `${repositoryUrl}/pull/`;
    body = body.replace(/^([*-] .+ in )(https?:\/\/\S+)$/gm, (line, prefix, url) => {
        const number = url.startsWith(pullUrlPrefix) ? url.slice(pullUrlPrefix.length) : '';
        return /^\d+$/.test(number) ? `${prefix}#${number}` : line;
    });
    const commitLines = commits.map(commit => {
        const author = commit.login ? `@${commit.login}` : escapeMarkdown(commit.authorName || 'Unknown author');
        return `* ${escapeMarkdown(commit.title)} by ${author} in ${commit.shortSha}`;
    });
    body = insertSection(body, "What's Changed", commitLines, /^## |^\*\*Full Changelog\*\*:/m);
    const newContributors = new Map(contributors.filter(author => author.isNew)
        .map(author => [author.login.toLowerCase(), author]));
    const nativeContributors = new Set();
    body = body.replace(/^[*-] @([^\s]+) made their first contribution in [^\n]*$/gm, (line, login) => {
        const author = newContributors.get(login.toLowerCase());
        if (!author) return line;
        nativeContributors.add(login.toLowerCase());
        return renderContributor(author);
    });
    body = insertSection(body, 'Contributors', contributors.filter(author => !author.isNew)
        .map(renderContributor),
    /^## New Contributors$|^\*\*Full Changelog\*\*:/m);
    body = insertSection(body, 'New Contributors', contributors.filter(author =>
        author.isNew && !nativeContributors.has(author.login.toLowerCase())).map(renderContributor),
    /^\*\*Full Changelog\*\*:/m);

    const changelogUrl = previous
        ? `${repositoryUrl}/compare/${encodeURIComponent(previous.tag)}...${encodeURIComponent(tag)}`
        : `${repositoryUrl}/commits/${encodeURIComponent(tag)}`;
    body = body.replace(/^\*\*Full Changelog\*\*:.*(?:\n|$)/gm, '').trimEnd();
    return `${body}\n\n**Full Changelog**: ${changelogUrl}\n`.trimStart();
}

module.exports = { renderReleaseNotes };
