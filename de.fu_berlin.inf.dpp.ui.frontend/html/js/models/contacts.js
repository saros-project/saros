var AmpersandCollection = require('ampersand-collection');
var Contact = require('./contact');
var SarosApi = require('../saros-api');

module.exports = AmpersandCollection.extend({
    model: Contact
});
