var BackboneEvents = require('backbone-events-standalone');

var SarosApi = BackboneEvents.mixin({

    connect: function(account) {

        __java_connect(JSON.stringify(account)); // jshint ignore:line
    },

    disconnect: function() {

        __java_disconnect(); // jshint ignore:line
    },

    manageAccounts: function() {

        __java_showAddAccountWizard(); // jshint ignore:line
    }
});

// For debugging purposes
SarosApi.on('all', function(event, data) {

    console.log(event);
    console.log(data);
});

module.exports = SarosApi;