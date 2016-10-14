var AmpersandView = require('ampersand-view');
var tmpl = require('./selectable-contacts.hbs');
var SelectableContactView = require('./selectable-contact');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    render: function() {

        this.renderWithTemplate(this);
        this.renderCollection(this.collection, SelectableContactView,
            this.queryByHook('contacts-list'));
        return this;
    },
    // Make sure that at least one contact is selected.
    // TODO: Is it possible to start session without contact?
    // In that case this function would always return true.
    isValid: function() {

        var selected = false;

        this.collection.forEach(function(contact) {

            if (contact.isSelected) {
                selected = true;
            }
        });

        return selected;
    },
    // Return the value of this view.
    // In this case the collection of contacts whih are selected.
    getValue: function() {

        return this.collection.filter(function(contact) {

            return contact.isSelected;
        });
    }
});
