var app = require('ampersand-app');
var bindAll = require('lodash.bindall');
var WizardView = require('../../elements/wizard-view');
var tmpl = require('./start-session-wizard.jade');
var SelectableProjectTreesView = require('../../elements/project-tree/selectable-project-trees');
var SelectableContactsView = require('../../elements/contact/selectable-contacts');
var dictionary = require('../../dictionary');

module.exports = WizardView.extend({
    template: tmpl,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    // Inherit all events from the WizardView Prototype
    events: WizardView.prototype.events,
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

                return new SelectableContactsView({
                    el: el,
                    collection: app.state.contactList
                });
            }
        }
    },
    order: ['projectTrees', 'contacts'],
    finish: function() {

        var contacts = this.contacts.getValue();
        var projectTrees = this.projectTrees.getValue();

        SarosApi.sendInvitation(projectTrees, contacts);
    },
    cancel: function() {

        SarosApi.closeStartSessionWizard();
    }
});
