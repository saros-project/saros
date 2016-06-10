var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var tmpl = require('./selectable-contact.jade');
var dictionary = require('../../dictionary');

module.exports = AmpersandView.extend({
    template: tmpl,
    d: dictionary,
    initialize: function() {

        bindAll(this, 'toggleSelect');
    },
    bindings: {
        'model.displayName': '[data-hook=display-name]',
        'model.isAvailable': {
            type: 'booleanClass',
            name: 'disabled',
            invert: true
        },
        'model.isSelected': {
            type: 'booleanClass',
            name: 'active'
        }
    },
    events: {
        'click': 'toggleSelect'
    },
    toggleSelect: function(event) {

        if(this.model.isAvailable){
            this.model.isSelected = !this.model.isSelected;
        }
    }
});
