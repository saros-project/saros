var AmpersandCollection = require('ampersand-collection');
var Contact = require('./contact');
var SarosApi = require('../saros-api');

var Contacts = AmpersandCollection.extend({

    model: Contact,

    // Returns a new collection with all contacts which are
    // currently available (determined by derived property 
    // contact.isAvailable).
    getAvailable: function() {

        return new Contacts(this.filter(function(contact) {

            return contact.isAvailable;
        }));
    }
});

module.exports = Contacts;