/* global window, document */
var app = require('ampersand-app');
var bind = require('lodash.bind');
var domReady = require('domready');
var AmpersandState = require('ampersand-state');
var SarosState = require('./models/saros-state');
var MainPage = require('./pages/main-page');
var StartSessionWizard = require('./pages/start-session-wizard');
var Accounts = require('./models/accounts');
var Contacts = require('./models/contacts');
var ErrorDialog = require('./views/error-dialog');
var ProjectTrees = require('./models/project-trees');

// Hack for now to get jQuery and Bootstrap etc. work together with
// the injected jQuery version from the SWT browser...
window.$$ = window.jQuery = require('jquery').noConflict(true);
require('bootstrap');
require('jstree');

// `SarosApi` must be globally available to enable calls from Java.
var SarosApi = window.SarosApi = require('./saros-api');

// Attach our app to `window` so we can
// easily access it from the console.
window.app = app;

app.extend({
    state: new SarosState(),
    accounts: new Accounts(),
    contacts: new Contacts(),
    projectTrees: new ProjectTrees(),
    init: function() {

        this.listenTo(SarosApi, 'showError', this.showError);

        var appContainer = document.getElementById('saros');
        // Decide which view to render.
        // the `page` property is set directly in the .html file.
        switch (this.page) {

            case 'main-page':

                new MainPage({
                    el: appContainer,
                    model: this.state
                });
                break;

            case 'start-session-wizard':

                new StartSessionWizard({
                    el: appContainer
                });
                break;
        }
    },
    showError: function(message) {

        // TODO: introduce dedicated model?
        var model = AmpersandState.extend({
            props: {
                message: 'string'
            }
        });

        new ErrorDialog({
            model: new model({
                message: message
            })
        });
    }
});

// Run it on domReady.
domReady(bind(app.init, app));
