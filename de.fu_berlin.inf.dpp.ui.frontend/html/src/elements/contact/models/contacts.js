var AmpersandCollection = require('ampersand-collection');
var Contact = require('./contact');

var Contacts = AmpersandCollection.extend({

    model: Contact
});

module.exports = Contacts;
