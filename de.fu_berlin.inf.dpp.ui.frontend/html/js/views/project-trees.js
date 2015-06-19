var AmpersandView = require('ampersand-view');
var templates = require('../templates');
var ProjectTreeNodeView = require('../views/project-tree-node');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.projectTrees,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    render: function() {

        this.renderWithTemplate(this);
        this.renderCollection(this.collection, ProjectTreeNodeView,
            this.queryByHook('project-trees'));

        $$(this.el).jstree({
            'plugins' : [ 'checkbox' ],
            'checkbox' : {
                'three_state': false
            }
        });

        return this;
    }
});
