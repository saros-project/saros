/* global $$ */
var bindAll = require('lodash.bindall');
var AmpersandView = require('ampersand-view');
var AmpersandFormView = require('ampersand-form-view');
var AmpersandInputView = require('ampersand-input-view');
var templates = require('../templates');
var dictionary = require('../dictionary');
var SarosApi = require('../saros-api');

module.exports = AmpersandView.extend({
    template: templates.deleteContactDialog,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    initialize: function() {

        bindAll(this, 'deleteContact');
    },
    events: {
        'click button[data-hook=delete]': 'deleteContact'
    },
    render: function() {

        this.renderWithTemplate();

        // Call Bootstraps function for modals and ensure to remove this
        // view when closing the modal. The view is attached to `body` by
        // Bootstrap.
        $$(this.el).modal().on('hidden.bs.modal', this.remove.bind(this));

        return this;
    },
    deleteContact: function() {

        this.model.delete();
        $$(this.el).modal('hide');
    }
});
