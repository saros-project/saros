/* global $$ */
var bindAll = require('lodash.bindall');
var AmpersandView = require('ampersand-view');
var AmpersandFormView = require('ampersand-form-view');
var AmpersandInputView = require('ampersand-input-view');
var tmpl = require('./rename-contact-dialog.hbs');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    initialize: function() {

        bindAll(this, 'renameContact');
    },
    bindings: {
        'model.jid': '[data-hook=jid]'
    },
    events: {
        'keypress': 'handleEnter'
    },
    render: function() {

        this.renderWithTemplate();
        this.form = new AmpersandFormView({
            el: this.query('form'),
            submitCallback: this.renameContact,
            // This is just an array of field views that follow
            // the rules described above. I'm using an input-view
            // here, but again, *this could be anything* you would
            // pass it whatever config items needed to instantiate
            // the field view you made.
            fields: [
                new AmpersandInputView({
                    name: 'displayName',
                    value: this.model.displayName || '',
                    label: dictionary.label.nickname,
                    placeholder: dictionary.label.optional,
                    required: false
                })
            ]
        });

        // Registering the form view as a subview ensures that
        // it`s `remove` method will get called when the parent
        // view is removed.
        this.registerSubview(this.form);

        // Call Bootstraps function for modals and ensure to remove this
        // view when closing the modal. The view is attached to `body` by
        // Bootstrap.
        $$(this.el).modal({
            backdrop: 'static'
        }).on('hidden.bs.modal', this.remove.bind(this));
    },
    renameContact: function(contact) {

        this.model.rename(contact.displayName);
        $$(this.el).modal('hide');
    },
    handleEnter: function(e) {

        if (e.keyCode === 13) {
            // Trigger click on add button to add contact
            $$(this.queryByHook('save')).trigger('click');
        }
    }
});
