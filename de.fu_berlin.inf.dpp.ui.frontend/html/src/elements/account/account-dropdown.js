var app = require('ampersand-app');
var View = require('ampersand-view');
var tmpl = require('./account-dropdown.jade');

module.exports = View.extend({
    template: tmpl,
    bindings: {
        'model.label': '[data-hook=jid]',
        'model.isActive': {
            type: 'booleanClass',
            yes: 'font-bold',
            no: ''
        }
    },
    events: {
        'click': 'setToActiveAccount'
    },
    setToActiveAccount: function() {
        app.state.activateAccount(this.model);
    }
});
