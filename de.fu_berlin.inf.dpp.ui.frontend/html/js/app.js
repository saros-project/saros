/* global window, document */
var app = require('ampersand-app');
var bind = require('lodash.bind');
var domReady = require('domready');
var SarosState = require('./models/saros-state');
var MainView = require('./views/main');
var Accounts = require('./models/accounts');

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
    init: function() {

        new MainView({
            el: document.body,
            model: this.state
        });
    }
});

// Run it on domReady.
domReady(bind(app.init, app));
