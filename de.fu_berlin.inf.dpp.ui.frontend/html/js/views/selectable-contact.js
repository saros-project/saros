var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var templates = require('../templates');
var dictionary = require('../dictionary');

module.exports = AmpersandView.extend({
    template: templates.selectableContact,
    d: dictionary,
    initialize: function() {

        bindAll(this, 'toggleSelect');
    },
    bindings: {
        'model.displayName': '[data-hook=display-name]',
        'model.isSelected': {
            type: 'booleanClass',
            name: 'active'
        }
    },
    events: {
        'click input[type="checkbox"]': 'toggleSelect'
    },
    toggleSelect: function(event) {

        this.model.isSelected = !this.model.isSelected;
    }
});
