var AmpersandModel = require('ampersand-model');

module.exports = AmpersandModel.extend({
    
    props: {
        displayName: ['string', true],
        presence: ['string', true, 'offline'],
        addition: ['string', false, '']
    },

    derived: {
        isAvailable: {
            deps: ['presence'],
            fn: function() {

                // TODO: correct predicate
                return this.presence === 'Online';
            }
        }
    },

    session: {
        isSelected: {
            type: 'boolean',
            default: false
        }
    }
});
