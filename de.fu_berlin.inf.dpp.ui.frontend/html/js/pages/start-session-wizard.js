var app = require('ampersand-app');
var AmpersandView = require('ampersand-view');
var templates = require('../templates');
var SelectableContactsView = require('../views/selectable-contacts-view');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.startSessionWizard,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    subviews: {
        contacts: {
            container: '[data-hook=contacts-container]',
            prepareView: function(el) {
                return new SelectableContactsView({
                    el: el,
                    collection: app.contacts
                });
            }
        }        
    }
});
