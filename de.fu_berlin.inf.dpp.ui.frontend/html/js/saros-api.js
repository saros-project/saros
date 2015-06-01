var BackboneEvents = require('backbone-events-standalone');

var SarosApi = BackboneEvents.mixin({

    connect: function(account) {

        __java_connect(JSON.stringify(account));
    },

    disconnect: function() {

        __java_disconnect();
    },

    manageAccounts: function() {

        __java_showAddAccountWizard();
    },

    addContact: function(contact) {

    },

    editContact: function(contact) {

    },

    deleteContact: function(contact) {

    }
});

// For debugging purposes
SarosApi.on('all', function(event, data) {

    console.log(event);
    console.log(data);
});

module.exports = SarosApi;