var AmpersandState = require('ampersand-state');
var Contacts = require('./contact/contacts');

module.exports = AmpersandState.extend({

    props: {
        participants: {
            type: Contacts,
            default: function() {

                return new Contacts();
            }
        }
    },

    derived: {

    },

    session: {

    }
});
