var AmpersandView = require('ampersand-view');
var templates = require('../templates');
var ContactView = require('../views/contact');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.contacts,
    // Add the dictionary to the context so that the template
    // engine can use it.
    d: dictionary,
    render: function() {

        this.renderWithTemplate(this);
        this.renderCollection(this.collection, ContactView,
            this.queryByHook('contact-list'));
        return this;
    }
});
