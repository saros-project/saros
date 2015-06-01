var AmpersandModel = require('ampersand-model');
var Account = require('./account');
var dictionary = require('../dictionary');
var SarosApi = require('../saros-api');

module.exports = AmpersandModel.extend({
    initialize: function() {

        var self = this;

        SarosApi.on('updateState', function(data) {

            self.set(data);
        });
    },

    props: {

        activeAccount: {
            type: Account,
            default: function() {

                return new Account();
            }
        },

        isConnected: {
            type: 'boolean',
            default: false
        },

        isConnecting: {
            type: 'boolean',
            default: false
        },

        isDisconnecting: {
            type: 'boolean',
            default: false
        }
    },

    derived: {

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
            deps: ['isConnected', 'isConnecting', 'isDisconnecting'],
            fn: function() {

                if (this.isDisconnecting) {
                    return dictionary.action.disconnecting;
                } else if (this.isConnecting) {
                    return dictionary.action.connecting;
                } else if (this.isConnected) {
                    return dictionary.action.disconnect;
                }
                return dictionary.action.connect;
            }
        },

        isBusy: {
            deps: ['isConnecting', 'isDisconnecting'],
            fn: function() {

                return !this.isConnecting && !this.isDisconnecting;
            }
        },

        isReady: {
            deps: ['isBusy', 'activeAccount'],
            fn: function() {

                // Checks whether there is an active account
                if (!this.activeAccount.jid) {
                    return false;
                }

                return this.isBusy;
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
            this.isDisconnecting = true;
            SarosApi.disconnect();
        } else {
            this.isConnecting = true;
            SarosApi.connect(this.activeAccount.toJSON());
        }
    }
});
