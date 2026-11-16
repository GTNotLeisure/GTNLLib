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

function renderReleaseNotes({ nativeBody, commits, contributors, repositoryUrl, tag, previous }) {
    let body = `${nativeBody.replace(/\r\n/g, '\n').trim()}\n`;
    const commitLines = commits.map(commit => {
        const author = commit.login ? `@${commit.login}` : escapeMarkdown(commit.authorName || 'Unknown author');
        return `* ${escapeMarkdown(commit.title)} by ${author} in ${commit.url}`;
    });
    body = insertSection(body, "What's Changed", commitLines, /^## |^\*\*Full Changelog\*\*:/m);
    body = insertSection(body, 'Contributors', contributors.filter(author => !author.isNew)
        .map(author => `* @${author.login} made their contribution in ${author.url}`),
    /^## New Contributors$|^\*\*Full Changelog\*\*:/m);
    body = insertSection(body, 'New Contributors', contributors.filter(author => author.isNew)
        .map(author => `* @${author.login} made their first contribution in ${author.url}`),
    /^\*\*Full Changelog\*\*:/m);

    const changelogUrl = previous
        ? `${repositoryUrl}/compare/${encodeURIComponent(previous.tag)}...${encodeURIComponent(tag)}`
        : `${repositoryUrl}/commits/${encodeURIComponent(tag)}`;
    body = body.replace(/^\*\*Full Changelog\*\*:.*(?:\n|$)/gm, '').trimEnd();
    return `${body}\n\n**Full Changelog**: ${changelogUrl}\n`.trimStart();
}

module.exports = { renderReleaseNotes };
