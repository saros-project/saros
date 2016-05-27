var AmpersandCollection = require('ampersand-collection');
var Account = require('./account');
var SarosApi = require('../../../saros-api');

module.exports = AmpersandCollection.extend({
    initialize: function() {

        var self = this;

        SarosApi.on('updateAccounts', function(data) {

            self.set(data);
        });
    },
    model: Account
});
