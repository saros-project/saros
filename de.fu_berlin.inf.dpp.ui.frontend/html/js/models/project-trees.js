var AmpersandCollection = require('ampersand-collection');
var ProjectTree = require('./project-tree');
var SarosApi = require('../saros-api');

module.exports = AmpersandCollection.extend({
    initialize: function() {

        var self = this;

        SarosApi.on('updateProjectTrees', function(data) {

            self.set(data);
        });
    },
    model: ProjectTree
});
