var app = require('ampersand-app');
var bindAll = require('lodash.bindall');
var AmpersandWizard = require('../views/ampersand-wizard');
var templates = require('../templates');
var SelectableProjectTreesView = require('../views/selectable-project-trees');
var SelectableContactsView = require('../views/selectable-contacts');
var dictionary = require('../dictionary');

module.exports = AmpersandWizard.extend({
    template: templates.startSessionWizard,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    subviews: {
        projectTrees: {
            hook: 'project-trees-container',
            prepareView: function(el) {
                return new SelectableProjectTreesView({
                    el: el,
                    collection: app.projectTrees
                });
            }
        },
        contacts: {
            hook: 'contacts-container',
            prepareView: function(el) {

                var contacts = app.state.contactList.getAvailable();

                return new SelectableContactsView({
                    el: el,
                    collection: contacts
                });
            }
        }
    },
    order: ['projectTrees', 'contacts'],
    finish: function() {

        var contacts = this.contacts.getValue();
        var projectTrees = this.projectTrees.getValue();

        //TODO: SarosApi.???
    },
    cancel: function() {

        SarosApi.closeStartSessionWizard();
    }
});
