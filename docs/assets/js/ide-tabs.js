// Load this after toc.js, because of the dependency to "loadToc();"
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

    var tabsToDeactivate = document.getElementsByClassName('nav-tab-link');

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
    loadToc();
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