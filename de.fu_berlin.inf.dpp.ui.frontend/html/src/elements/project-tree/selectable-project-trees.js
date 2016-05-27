var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var tmpl = require('./selectable-project-trees.jade');
var ProjectTreeNodeView = require('./project-tree-node');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    initialize: function() {

        // TODO: should not be necessary, check why render is not
        // triggered on collection change event without this line
        bindAll(this, 'render');
        this.collection.on('all', this.render);
    },
    render: function() {

        this.renderWithTemplate(this);

        // We build a nested list structure to map our projectTrees to
        // the dom.
        this.renderCollection(this.collection, ProjectTreeNodeView,
            this.queryByHook('project-trees'));

        // Create the jstree from the list structure.
        // The problem with that: jstree replaces each of our list elements
        // (`ul` and `li`) such that all attached event listeners in the
        // `ProjectTreeNodeView` are useless. This means for example, we
        // can not observe check and uncheck events in our views and keep
        // the model up-to-date like that.
        $$(this.queryByHook('jstree')).jstree({
            'plugins' : [ 'checkbox' ],
            'checkbox' : {
                'three_state': false
            }
        });

        return this;
    },
    isValid: function() {

        // You to select at least one node...
        return $$(this.queryByHook('jstree')).jstree('get_selected') != 0;
    },
    // Recursively, visit the given `node` and its `members`-nodes and mark
    // each as selected/unselecte with respect to the given `selected`
    // array.
    markProjectTreeNodes: function(node, selected) {

        // Check whether node is selected/unselected.
        var isSelected = (selected.indexOf(node.cid) >= 0);

        // Use option `silent: true` to avoid change events cause they
        // have no effect anymore.
        node.set('isSelectedForSharing', isSelected, {
            silent: true
        });

        node.members.each(function(projectTreeNode) {

            this.markProjectTreeNodes(projectTreeNode, selected);
        }, this);
    },
    getValue: function() {

        // Get the array (of cids) of selected nodes from the jstree.
        var selected = $$(this.queryByHook('jstree')).jstree('get_selected');

        // We have to recursively visit every node in our projectTrees and
        // check wheter the node is selected in the jstree.
        // Currently, there is no other way to check that.
        app.projectTrees.each(function(projectTree) {

                this.markProjectTreeNodes(projectTree.root, selected);
        }, this);

        return this.collection;
    }
});
