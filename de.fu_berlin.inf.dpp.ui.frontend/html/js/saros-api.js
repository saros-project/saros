/*
 * Saros API Documentation
 *
 * In addition to the methods listed below you can trigger and listen
 * to events on the SarosAPI object. 
 *
 * To trigger an event do:
 * `SarosApi.trigger('eventName' [, data]);`
 *
 * To listen to an event do:
 * `SarosApi.on('eventName', handler);`
 * where the first parameter of the handler function is the data object,
 * if available.
 *
 * The following events are provided:
 *
 * updateState
 * updateAccounts
 * updateContacts
 *
 */

var BackboneEvents = require('backbone-events-standalone');

var SarosApi = BackboneEvents.mixin({

    connect: function(account) {

        __java_connect(JSON.stringify(account)); // jshint ignore:line
    },

    disconnect: function() {

        __java_disconnect(); // jshint ignore:line
    },

    manageAccounts: function() {

        __java_showSarosPreferencesWizard(); // jshint ignore:line
    },

    addContact: function(jid, displayName) {

        // TODO: nickname in backend
        __java_addContact(jid, displayName); // jshint ignore:line
    },

    validateJid: function(jid) {

        // TODO: make __java_validateJid(jid) available
        return __java_validateJid(jid); // jshint ignore:line
    },
     
     showStartSessionWizard: function() {
     
         __java_showStartSessionWizard(); // jshint ignore:line
     }
});

// For debugging purposes
SarosApi.on('all', function(event, data) {

    console.log(event);
    console.log(data);
});

module.exports = SarosApi;
