var app = require('ampersand-app');
var View = require('ampersand-view');
var templates = require('../templates');

module.exports = View.extend({
    template: templates.main,
    autoRender: true
});
