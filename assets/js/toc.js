// toc config
var tocConfig = {
    minDepthToc: 2,
    maxDepthToc: 3,
    tocBarId: 'toc-bar',
    tocBarColumnId: 'toc-bar-column',
    mainContentId: 'main-content'
}

// load toc of currently visible content
function loadToc() {

    var tocBar = document.getElementById(tocConfig.tocBarId);
    if (!tocBar) return;

    function generateTocContent() {

        // create selector as "#main-content h2, h3, h4" (with minDepthToc: 2, maxDepthToc: 4)
        function createHeaderSelector() {
            var selector = `#${tocConfig.mainContentId} `;

            for (var i = tocConfig.minDepthToc; i <= tocConfig.maxDepthToc; i++) {
                // :visible is added in order to ignore non visible content form tabs
                selector += `h${i}:visible`;
                if (i < tocConfig.maxDepthToc) selector += ', ';
            }
            return selector;
        }

        function headerToInt (h) {
            if (h === undefined) return 0;
            return parseInt(h.tagName.replace('H', ''), 10);
        };

        function headerDiff (h1, h2) {
            return headerToInt(h1) - headerToInt(h2);
        };

        var content = "";

        if (!tocBar) return;

        // Add opening or closing list tags
        var addTags = function (depth) {
            var absDepth = Math.abs(depth);
            if (depth == 0) {
                content += '</li>';
                return;
            }
            // requires class nav in order to enable nested scrollspy
            var tag = (depth > 0) ? '<ul class="nav flex-column">' : '</li></ul>';
            for (var i = 0; i < absDepth; i++) {
                content += tag;
            }
        };

        console.log(content);

        var prevHeader;
        var headers = $(createHeaderSelector());

        for (header of headers) {
            var depth = headerDiff(header, prevHeader);
            addTags(depth);

            content += `<li class="py-0"><a class="nav-link py-0" href=#${header.id}>${header.textContent}</a>`;

            prevHeader = header;
        }
        // force closing tags at the end
        addTags(-1 * headerToInt(prevHeader));
        return content;
    }

    tocBar.innerHTML = generateTocContent();
}

//TODO: the file mixes jQuery and vanilla javascript
$(document).ready(() => {
    loadToc();
});