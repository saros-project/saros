var AmpersandState = require('ampersand-state');
var SarosApi = require('../saros-api');

module.exports = AmpersandState.extend({
    
    props: {
        jid: ['string', true],
        displayName: ['string', true],
        presence: ['string', true, 'offline'],
        addition: ['string', false, '']
    },

    derived: {
        isAvailable: {
            deps: ['presence'],
            fn: function() {

                // TODO: correct predicate
                return this.presence === 'Online';
            }
        }
    },

    session: {
        isSelected: {
            type: 'boolean',
            default: false
        }
    },

    rename: function(displayName) {

        this.displayName = displayName;
        SarosApi.renameContact(this.jid, this.displayName);
    },

    delete: function() {

        SarosApi.deleteContact(this.jid);
    },

    create: function() {

        SarosApi.addContact(this.jid, this.displayName);
    }    
});
