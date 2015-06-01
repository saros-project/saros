var AmpersandState = require('ampersand-state');

module.exports = AmpersandState.extend({
    props: {
        jid: ['object'],
        username: ['string', true, ''],
        domain: ['string', true, '']
    },
    derived: {
        label: {
            deps: ['username', 'domain'],
            fn: function() {
                if (this.jid) {

                    return this.username + '@' + this.domain;
                } else {

                    return null;
                }
            }
        }
    },
    session: {
        isActive: {
            type: 'boolean',
            default: false
        }
    }
});
