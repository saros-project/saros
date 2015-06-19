var AmpersandState = require('ampersand-state');
var AmpersandCollection = require('ampersand-collection');

var ProjectTreeNodes = AmpersandCollection.extend({
});

var ProjectTreeNode = AmpersandState.extend({

    props: {
        displayName: ['string', true],
        path: ['object'],
        type: ['string', true],
        isSelectedForSharing: ['boolean', true, true]
    },

    collections: {
        members: ProjectTreeNodes
    }
});

ProjectTreeNodes.prototype.model = ProjectTreeNode;

module.exports = {
    model: ProjectTreeNode,
    colelction: ProjectTreeNodes
};