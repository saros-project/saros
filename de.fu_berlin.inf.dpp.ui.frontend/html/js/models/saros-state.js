var AmpersandState = require('ampersand-state');
var Account = require('./account');
var Contacts = require('./contacts');
var dictionary = require('../dictionary');
var SarosApi = require('../saros-api');

// Private mapping of connection states.
// Must correspond with the data given from the backend.
var CS = {
    CONNECTED: 'CONNECTED',
    NOT_CONNECTED: 'NOT_CONNECTED',
    CONNECTING: 'CONNECTING',
    DISCONNECTING: 'DISCONNECTING'
};

module.exports = AmpersandState.extend({
    initialize: function() {

        var self = this;

        SarosApi.on('updateState', function(data) {

            self.set(data);

            // If there is an active account, it must explicitly set.
            // Via the previous set operation it is not added as an
            // `Account` model but only as plain JSON.
            if (data.activeAccount) {
                self.set('activeAccount', new Account(data.activeAccount));
            }
        });
    },

    props: {

        activeAccount: {
            type: Account,
            default: function() {

                return new Account();
            }
        },

        connectionState: {
            type: {
                type: 'string',
                values: [
                    CS.CONNECTED, 
                    CS.NOT_CONNECTED,
                    CS.CONNECTING,
                    CS.DISCONNECTING
                ]
            },
            default: CS.NOT_CONNECTED
        }
    },

    collections: {

        contactList: Contacts
    },

    derived: {

        isConnected: {
            deps: ['connectionState'],
            fn: function() {

                return this.connectionState == CS.CONNECTED;
            }
        },

        isBusy: {
            deps: ['connectionState'],
            fn: function() {

                return this.connectionState == CS.CONNECTING || this.connectionState == CS.DISCONNECTING;
            }
        },

        isReady: {
            deps: ['isBusy', 'activeAccount'],
            fn: function() {

                // Checks whether there is an active account
                if (!this.activeAccount.jid) {
                    return false;
                }

                return !this.isBusy;
            }
        },        

        activeAccountLabel: {
            deps: ['activeAccount.jid'],
            fn: function() {
                if (this.activeAccount.label) {
                    return this.activeAccount.label;
                } else {
                    return dictionary.message.noAccount;
                }
            }
        },

        connectionStateLabel: {
            deps: ['connectionState'],
            fn: function() {

                if (this.connectionState == CS.DISCONNECTING) {
                    return dictionary.action.disconnecting;
                } 

                if (this.connectionState == CS.CONNECTING) {
                    return dictionary.action.connecting;
                } 

                if (this.connectionState == CS.CONNECTED) {
                    return dictionary.action.disconnect;
                }

                return dictionary.action.connect;
            }
        }
    },

    // Activates an account.
    // Use this methond instead of `state.activeAccount = acc;`
    activateAccount: function(account) {

        var currentActiveAccount = this.activeAccount;
        currentActiveAccount.isActive = false;
        account.isActive = true;
        this.set('activeAccount', account);
    },

    updateConnectionState: function() {

        if (this.isConnected) {
            this.connectionState == CS.DISCONNECTING;
            SarosApi.disconnect();
        } else {
            this.connectionState == CS.CONNECTING;
            SarosApi.connect(this.activeAccount.toJSON());
        }
    }
});
