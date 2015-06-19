var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var templates = require('../templates');
var dictionary = require('../dictionary');

var ProjectTreeNodeView = AmpersandView.extend({
    template: templates.projectTreeNode,
    d: dictionary,
    initialize: function() {

        bindAll(this, 'toggleSelect');
    },
    bindings: {
        'model.displayName': '[data-hook=display-name]',
        'model.isSelectedForSharing': {
            type: 'booleanClass',
            name: 'jstree-clicked',
            selector: 'a'
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
