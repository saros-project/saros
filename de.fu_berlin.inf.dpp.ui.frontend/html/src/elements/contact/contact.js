var AmpersandView = require('ampersand-view');
var bindAll = require('lodash.bindall');
var tmpl = require('./contact.hbs');
var dictionary = require('../../dictionary');
var RenameContactDialog = require('./rename-contact-dialog');
var DeleteContactDialog = require('./delete-contact-dialog');

module.exports = AmpersandView.extend({
    template: tmpl,
    d: dictionary,
    initialize: function() {

        bindAll(this, 'rename', 'delete');

        // Set id of the `li` element-specific context-menu.
        this.contextMenuBinding = '#' + this.model.cid;
    },
    session: {
        // This property is necessary to bind the current `li` element
        // to its specific context-menu element since every `li` 
        // element has its own.
        contextMenuBinding: 'string'
    },
    bindings: {
        'model.displayName': '[data-hook=display-name]',
        'model.isAvailable': {
            type: 'toggle',
            hook: 'is-available'
        },
        // Bind the corresponding context-menu with an id to this `li`
        // element.
        'contextMenuBinding': {
            type: 'attribute',
            name: 'data-target'
        },
        // Set the unique `id` for the context-menu element.
        'model.cid': {
            type: 'attribute',
            selector: '[data-hook=context-menu]',
            name: 'id'
        }
    },
    events: {
        'click [data-hook=rename]': 'rename',
        'click [data-hook=delete]': 'delete'
    },
    rename: function() {

        new RenameContactDialog({
            model: this.model
        });
    },
    delete: function() {

        new DeleteContactDialog({
            model: this.model
        });
    },    
});
