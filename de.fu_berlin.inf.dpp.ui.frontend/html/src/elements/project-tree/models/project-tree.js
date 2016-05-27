var AmpersandState = require('ampersand-state');
var ProjectTreeNode = require('./project-tree-node').model;

module.exports = AmpersandState.extend({

    props: {
        displayName: ['string', true]
    },

    children: {
        root: ProjectTreeNode
    }
});
