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

// Due to the circular dependency we have to use this
// "workaround" with the use of the `prototype` property:
// https://github.com/AmpersandJS/ampersand-state/issues/78
ProjectTreeNodes.prototype.model = ProjectTreeNode;

module.exports = {
    model: ProjectTreeNode,
    colelction: ProjectTreeNodes
};