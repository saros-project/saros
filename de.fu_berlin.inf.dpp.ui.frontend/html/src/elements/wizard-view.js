/* global jQuery */
var bindAll = require('lodash.bindall');
var AmpersandView = require('ampersand-view');

var errorPrefix = 'WizardView: ';

module.exports = AmpersandView.extend({

    initialize: function() {

        bindAll(this, 'back', 'next', 'finish', 'cancel', '_renderWizard');

        if (!this.order) {
            throw new Error(errorPrefix + 'order property must be defined.');
        }

        this._order = this.order;
        this._currentSubview = this._order[0];
        this.on('change:_currentSubview', this._renderWizard);
    },
    // Session and derived properties to manage the wizard state
    // (for example enabling/disabling buttons, etc.).
    session: {

        _currentSubview: 'string',
        _order: 'array'
    },
    derived: {

        _currentIndex: {
            deps: ['_currentSubview'],
            fn: function() {

                return this._order.indexOf(this._currentSubview);
            }
        },

        hasNext: {
            deps: ['_currentIndex'],
            fn: function() {

                if (this._currentIndex < this._order.length - 1) {

                    return true;
                }

                return false;
            }
        },

        hasPrevious: {
            deps: ['_currentIndex'],
            fn: function() {

                if (this._currentIndex === 0) {

                    return false;
                }

                return true;
            }
        }
    },
    bindings: {

        'hasNext': {
            type: 'booleanClass',
            no: 'disabled',
            hook: 'next'
        },

        'hasPrevious': {
            type: 'booleanClass',
            no: 'disabled',
            hook: 'back'
        }
    },
    events: {
        'click button[data-hook=back]': 'back',
        'click button[data-hook=next]': 'next',
        'click button[data-hook=finish]': 'finish',
        'click button[data-hook=cancel]': 'cancel',
        'contextmenu': 'handleContextmenu'
    },
    render: function() {

        this.renderWithTemplate(this);
        this._renderWizard();
        return this;
    },
    _renderWizard: function() {

        // Hide all subviews..
        this._order.forEach(function(subview) {

            jQuery(this[subview].el).hide();
        }, this);

        // ...and show the current one.
        jQuery(this[this._currentSubview].el).show();
    },
    back: function() {

        if (this.hasPrevious) {
            this._currentSubview = this._order[this._currentIndex - 1];
        }
    },
    next: function() {

        if (this.hasNext) {
            this._currentSubview = this._order[this._currentIndex + 1];
        }
    },
    finish: function() {

    },
    cancel: function() {

    },
    handleContextmenu: function(e) {

        e.preventDefault();
    }
});
