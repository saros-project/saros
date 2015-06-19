var app = require('ampersand-app');
var AmpersandView = require('ampersand-view');
var templates = require('../templates');
var ProjectTreesView = require('../views/project-trees');
var SelectableContactsView = require('../views/selectable-contacts');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.startSessionWizard,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    subviews: {
        projectTrees: {
            container: '[data-hook=projectTrees-container]',
            prepareView: function(el) {
                return new ProjectTreesView({
                    el: el,
                    collection: app.projectTrees
                });
            }
        },
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
