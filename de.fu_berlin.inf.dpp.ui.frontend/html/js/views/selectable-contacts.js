var AmpersandView = require('ampersand-view');
var templates = require('../templates');
var SelectableContactView = require('../views/selectable-contact');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.selectableContacts,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    render: function() {

        this.renderWithTemplate(this);
        this.renderCollection(this.collection, SelectableContactView,
            this.queryByHook('contact-list'));
        return this;
    }
});
