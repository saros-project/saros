var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var tmpl = require('./project-tree-node.hbs');
var dictionary = require('../../dictionary');

var ProjectTreeNodeView = AmpersandView.extend({
    template: tmpl,
    d: dictionary,
    initialize: function() {

        bindAll(this, 'toggleSelect');
    },
    bindings: {
        'model.label': '[data-hook=label]',
        'model.isSelectedForSharing': {
            type: 'booleanClass',
            name: 'jstree-clicked',
            selector: 'a'
        },
        // A litlle bit hackish approach to link the projectTreeNode model
        // to the jstree instance.
        'model.cid': {
            type: 'attribute',
            name: 'id'
        }
    },
    events: {
        'click': 'toggleSelect'
    },
    render: function() {

        // Check whether the model is a ProjectTree or a ProjectTreeNode,
        // since we want to enable this view to work with both model types.
        var members = (this.model.root) ? this.model.root.members : this.model.members;

        this.renderWithTemplate(this);
        this.renderCollection(members, ProjectTreeNodeView,
            this.queryByHook('members'));
        return this;
    },
    toggleSelect: function() {

        console.log(this.model);
    }
});

module.exports = ProjectTreeNodeView;
