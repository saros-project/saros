var allowedClasses = ['eclipse', 'intellij']

// Bootstrap 4 does not allow selecting multiple contents via one nav
// therefore this method is used as workaround
function selectTabs(classToActivate, updateUrl = true) {
    var normalizedClassToActivate = classToActivate.toLowerCase()
    if (allowedClasses.indexOf(normalizedClassToActivate) < 0) return;
    // deactivate all content
    var panesToDeactivate = document.getElementsByClassName('tab-pane');

    for (const e of panesToDeactivate) {
        e.classList.remove('active');
        e.classList.remove('show');
    }

    var tabsToDeactivate = document.getElementsByClassName('nav-link')

    for (const t of tabsToDeactivate) {
        t.classList.remove('active');
    }

    var elementsToActivate = document.getElementsByClassName(normalizedClassToActivate)

    for (const e of elementsToActivate) {
        e.classList.add('active');
        if (e.nodeName === "DIV")
            e.classList.add('show');
    }

    if (updateUrl && window.history && history.pushState) {
        history.replaceState(null, null, `?tab=${normalizedClassToActivate}`);
    }
    // reload toc after changing visible tab
    loadToc()
}

// allow to reference page tab specific
// e.g www.saros-project.org/documentation/getting-started.html?tab=intellij#missing-features
$(document).ready(() => {
    function getTabParameter() {
        var regex = new RegExp('[\\?&]tab=([^&#]*)');
        var results = regex.exec(location.search);
        var name = results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
        return name.toLowerCase();
    };

    var tabParam = getTabParameter();
    if (tabParam && allowedClasses.indexOf(tabParam) >= 0) {
        selectTabs(tabParam, false);
    }
});


// toc config
var tocConfig = {
    minDepthToc: 2,
    maxDepthToc: 3,
    tocBarId: 'toc-bar',
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
            var tag = (depth > 0) ? '<ul>' : '</li></ul>';
            for (var i = 0; i < absDepth; i++) {
                content += tag;
            }
        };

        var prevHeader;
        var headers = $(createHeaderSelector());

        for (header of headers) {
            var depth = headerDiff(header, prevHeader);
            addTags(depth);

            content += `<li><a href=#${header.id}>${header.textContent}</a>`;

            prevHeader = header;
        }
        // force closing tags at the end
        addTags(-1 * headerToInt(prevHeader));
        return content;
    }

    tocBar.innerHTML = generateTocContent();
}