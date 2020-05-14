// Load at the bottom the page.
function addAnchorLinks () {
    var icon = '<small><i class="fa fa-link ml-2"></i></small>';

    ['h2', 'h3', 'h4', 'h5', 'h6'].forEach(function(tag){
        var headingNodes = document.getElementsByTagName(tag);
        var headingNodeList = Array.prototype.slice.call(headingNodes);
        headingNodeList.forEach(function(node){
            var link = document.createElement('a');
            link.className = 'header-link';
            link.innerHTML = icon;
            link.href = '#' + node.getAttribute('id');
            node.appendChild(link);
        });
    });
}
addAnchorLinks();