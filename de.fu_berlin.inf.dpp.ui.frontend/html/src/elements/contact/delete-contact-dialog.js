/* global $$ */
var bindAll = require('lodash.bindall');
var AmpersandView = require('ampersand-view');
var tmpl = require('./delete-contact-dialog.hbs');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
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
        $$(this.el).modal({
            backdrop: 'static'
        }).on('hidden.bs.modal', this.remove.bind(this));

        return this;
    },
    deleteContact: function() {

        this.model.delete();
        $$(this.el).modal('hide');
    }
});
