/* global $$ */
var AmpersandView = require('ampersand-view');
var tmpl = require('./error-dialog.hbs');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    bindings: {
        'model.message': '[data-hook=message-container]'
    },
    render: function() {

        this.renderWithTemplate();

        // Call Bootstraps function for modals and ensure to remove this
        // view when closing the modal. The view is attached to `body` by
        // Bootstrap.
        $$(this.el).modal({
            backdrop: 'static'
        }).on('hidden.bs.modal', this.remove.bind(this));
    }
});
