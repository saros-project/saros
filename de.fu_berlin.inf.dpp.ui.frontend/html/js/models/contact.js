var AmpersandModel = require('ampersand-model');

module.exports = AmpersandModel.extend({
    props: {
        displayName: ['string', true],
        presence: ['string', true, 'offline'],
        addition: ['string', false, '']
    }
});
