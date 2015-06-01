var app = require('ampersand-app');
var View = require('ampersand-view');
var templates = require('../templates');
var AccountsDropdownView = require('../views/accounts-dropdown');
var dictionary = require('../dictionary');

module.exports = View.extend({
    template: templates.main,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    subviews: {
        accounts: {
            container: '[data-hook=account-list]',
            prepareView: function(el) {

                return new AccountsDropdownView({
                    el: el,
                    collection: app.accounts
                });
            }
        }
    },
    events: {
        'click [data-hook=connection-button]': 'toggleConnect'
    },
    bindings: {
        'model.activeAccountLabel': '[data-hook=active-account]',
        'model.connectionStateLabel': '[data-hook=connection-button]',
        'model.isReady': {
            type: 'booleanClass',
            yes: '',
            no: 'disabled',
            hook: 'connection-button'
        },
        'model.isConnected': {
            type: 'booleanClass',
            yes: 'disabled',
            no: '',
            selector: '.account'
        }
    },
    toggleConnect: function() {

        this.model.updateConnectionState();
    }
});
