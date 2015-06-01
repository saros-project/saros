/* global window, document */
var app = require('ampersand-app');
var bind = require('lodash.bind');
var domReady = require('domready');
var MainView = require('./views/main');
var SarosApi = require('./saros-api');

window.$ = window.jQuery = require('jquery');
require('bootstrap');

// `SarosApi` must be globally available to enable calls from Java.
window.SarosApi = require('./saros-api');

// Attach our app to `window` so we can
// easily access it from the console.
window.app = app;

app.extend({
    init: function() {

        new MainView({
            el: document.body
        });
    }
});

// Run it on domReady.
domReady(bind(app.init, app));
