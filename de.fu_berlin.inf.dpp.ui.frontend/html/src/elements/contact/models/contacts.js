var AmpersandCollection = require('ampersand-collection');
var Contact = require('./contact');
var SarosApi = require('../../../saros-api');

var Contacts = AmpersandCollection.extend({

    model: Contact
});

module.exports = Contacts;