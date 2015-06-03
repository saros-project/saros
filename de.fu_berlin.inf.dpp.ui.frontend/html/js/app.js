/* global window, document */
var app = require('ampersand-app');
var bind = require('lodash.bind');
var domReady = require('domready');
var SarosState = require('./models/saros-state');
var MainView = require('./views/main');
var Accounts = require('./models/accounts');
var Contacts = require('./models/contacts');

window.$ = window.jQuery = require('jquery');
require('bootstrap');

// `SarosApi` must be globally available to enable calls from Java.
window.SarosApi = require('./saros-api');

// Attach our app to `window` so we can
// easily access it from the console.
window.app = app;

app.extend({
    state: new SarosState(),
    accounts: new Accounts(),
    contacts: new Contacts(),
    init: function() {

        // Decide which view to render.
        // the `page` property is set directly in the .html file.
        switch (this.page) {

            case 'main-page':
                
                new MainView({
                    el: document.body,
                    model: this.state
                });
                break;
        }
    }
});

// Run it on domReady.
domReady(bind(app.init, app));
