var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var templates = require('../templates');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.contact,
    d: dictionary,
    initialize: function() {

        bindAll(this, 'delete', 'rename');
    },
    bindings: {
        'model.displayName': '[data-hook=display-name]'
    },
    events: {
        'click [data-hook=rename]': 'rename',
        'click [data-hook=delete]': 'delete'
    },
    delete: function() {

        this.model.destroy();
    },
    rename: function() {

        // TODO
    }
});
