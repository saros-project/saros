/*
 * Global Saros object to encapsulate the Saros Java <-> Javascript interface.
 *
 *  __java_methodName are methods globally injected from Java-side and shall
 *  not be accessed directly but via the Saros object.
 *
 * To be able to react to function calls from Java-side (which shall only call
 * methods provided by the Saros object) the corresponding function provided by
 * the Saros object shall trigger an event on the Saros object:
 *
 * Saros.trigger('eventName', params);
 *
 * On Javascript-side you can listen to those events:
 *
 * Saros.on('eventName', eventHandler);
 *
 */

var Saros;

(function () {
    'use strict';

    Saros = _.extend({}, Backbone.Events, {

        /**
         * Account functions
         */
		
		connect: function (account) {

            __java_connect(account);
		},

		disconnect: function () {

			__java_disconnect();
		},

		showAddAccountWizard: function () {

			__java_showAddAccountWizard();
		},

		cancelAddAccountWizard: function () {

			__java_cancelAddAccountWizard();
		},

		saveAccount: function (account) {

		    __java_saveAccount(account.jid, account.password);
		},

		setAccountList: function (accountList) {

			this.trigger('setAccountList', accountList);
		},

		setIsConnected: function (connected) {

			this.trigger('setIsConnected', connected);
		},

		setIsConnecting: function () {

			this.trigger('setIsConnecting');
		},

		setIsDisconnecting: function () {

			this.trigger('setIsDisconnecting');
		},

        /**
         * Contact functions
         */

		showAddContactWizard: function () {

			__java_showAddContactWizard();
		},

		cancelAddContactWizard: function () {

			__java_cancelAddAccountWizard();
		},

		addContact: function (jid, nickname) {

		    __java_addContact(jid, nickname);
		},

		renameContact: function (contact) {

		    __java_renameContact(contact);
		},

		deleteContact: function (contact) {

		    __java_deleteContact(contact);
		},

        displayContactList: function (contactList) {

            this.trigger('displayContactList', contactList);
        }
	});

	Saros.on("all", function (eventName) {

		console.log(eventName + ' event was triggerd');
	});

})();