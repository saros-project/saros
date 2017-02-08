var bindAll = require('lodash.bindall');
var AmpersandView = require('ampersand-view');
var AmpersandFormView = require('ampersand-form-view');
var AmpersandInputView = require('ampersand-input-view');
var tmpl = require('./add-contact-dialog.hbs');
var dictionary = require('../../dictionary');
var SarosApi = require('../../saros-api');
var Contact = require('./models/contact');

module.exports = AmpersandView.extend({
    template: tmpl,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    autoRender: true,
    initialize: function() {

        AmpersandView.prototype.initialize.apply(this, arguments);
        bindAll(this, 'updateValidity', 'addContact', 'handleEnter');
    },
    props: {
        valid: 'boolean'
    },
    bindings: {
        'valid': {
            type: 'booleanClass',
            yes: '',
            no: 'disabled',
            hook: 'add'
        }
    },
    events: {
        'keypress': 'handleEnter'
    },
    render: function() {

        this.renderWithTemplate();
        this.form = new AmpersandFormView({
            el: this.query('form'),
            submitCallback: this.addContact,
            // This valid callback gets called (if it exists)
            // when the form first loads and any time the form
            // changes from valid to invalid or vice versa.
            // You might use this to disable the "submit" button
            // any time the form is invalid, for exmaple.
            validCallback: this.updateValidity,
            // This is just an array of field views that follow
            // the rules described above. I'm using an input-view
            // here, but again, *this could be anything* you would
            // pass it whatever config items needed to instantiate
            // the field view you made.
            fields: [
                new AmpersandInputView({
                    name: 'jid',
                    label: dictionary.label.jid,
                    placeholder: 'john.doe@jabber.org',
                    // an intial value if it has one
                    value: '@saros-con.imp.fu-berlin.de',
                    // this one takes an array of tests
                    tests: [
                        function(val) {

                            var validationResult = SarosApi.validateJid(val);

                            if (!validationResult.valid) {
                                return validationResult.message;
                            }
                        }
                    ]
                }),
                new AmpersandInputView({
                    name: 'displayName',
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
    updateValidity: function(valid) {

        // We are updating the valid property here to keep the
        // submit button disabled/enabled w.r.t. to the validity.
        this.valid = valid;
    },
    addContact: function(contactInfo) {

        var contact = new Contact(contactInfo);
        contact.create();
        $$(this.el).modal('hide');
    },
    handleEnter: function(e) {

        if (e.keyCode === 13) {
            if(this.valid) {
                // Trigger click on add button to add contact
                $$(this.queryByHook('add')).trigger('click');
            } else {
                // Avoid closing the modal on enter
                e.preventDefault();
            }
        }
    }
});
