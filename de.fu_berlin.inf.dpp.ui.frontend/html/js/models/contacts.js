var AmpersandCollection = require('ampersand-collection');
var Contact = require('./contact');
var SarosApi = require('../saros-api');

module.exports = AmpersandCollection.extend({
    initialize: function() {

        var self = this;

        SarosApi.on('contactsChanged', function(data) {

            self.set(data.contactList);
        });
    },
    model: Contact
});
