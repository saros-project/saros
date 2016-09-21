var AmpersandState = require('ampersand-state');

module.exports = AmpersandState.extend({
    props: {
        username: ['string', true, ''],
        domain: ['string', true, ''],
        password:['string', true, ''],
        server:['string',true,''],
        port:['number',true,''],
        useTLS:['boolean',true, ''],
        useSASL:['boolean',true,'']
    },
    derived: {
        label: {
            deps: ['username', 'domain'],
            fn: function() {
                if (this.username) {

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
