var app = require('ampersand-app');
var AmpersandView = require('ampersand-view');
var tmpl = require('./main-page.jade');
var AccountsDropdownView = require('../../elements/account/accounts-dropdown');
var ContactsView = require('../../elements/contact/contacts');
var AddContactDialog = require('../../elements/contact/add-contact-dialog');
var SarosApi = require('../../saros-api');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
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
        },
        contacts: {
            container: '[data-hook=contact-container]',
            prepareView: function(el) {
                return new ContactsView({
                    el: el,
                    collection: app.state.contactList
                });
            }
        }        
    },
    events: {
        'click [data-hook=connection-button]': 'toggleConnect',
        'click [data-hook=add-contact]': 'openAddContactDialog',
        'click [data-hook=start-session]': 'startSession',
        'contextmenu': 'handleContextmenu'
    },
    bindings: {
        'model.activeAccountLabel': '[data-hook=active-account]',
        'model.connectionStateLabel': '[data-hook=connection-button]',
        // disable/enable connection button
        'model.isReady': {
            type: 'booleanClass',
            yes: '',
            no: 'disabled',
            hook: 'connection-button'
        },
        'model.isConnected': [
            // disable certain buttons when not connected
            {
                type: 'booleanClass',
                yes: 'disabled',
                no: '',
                selector: '.account'
            },
            {
                type: 'booleanClass',
                yes: '',
                no: 'disabled',
                hook: 'add-contact'
            },
            {
                type: 'booleanClass',
                yes: '',
                no: 'disabled',
                hook: 'start-session'
            },
            // disable content-container when not connected
            {
                type: 'toggle',
                selector: '.content-container'
            }
        ]
    },
    handleContextmenu: function(e) {

        e.preventDefault();
    },
    toggleConnect: function() {

        this.model.updateConnectionState();
    },
    openAddContactDialog: function() {

        new AddContactDialog();
    },
    startSession: function() {

        SarosApi.showStartSessionWizard();
    }
});

