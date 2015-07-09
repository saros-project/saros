var expect = require('expect.js');
var SarosState = require('../js/models/saros-state');
var SarosApi = require('../js/saros-api');
var Account = require('../js/models/account');

function getDummyAccount() {

    return new Account({
            jid: {
                jid: 'some@example.tld'
            },
            username: 'some',
            domain: 'example.tld'
        });
}

describe('saros-state', function(){

    describe('#isReady', function(){

        it('should be false initially', function(){

            var state = new SarosState();
            expect(state.isReady).to.be(false);
        });

        it('should be true after activating an account', function(){

            var state = new SarosState();
            state.activateAccount(getDummyAccount());
            expect(state.isReady).to.be(true);
        });

        it('should be false while connecting', function(){

            var state = new SarosState();
            state.activateAccount(getDummyAccount());
            state.connectionState = 'CONNECTING';
            expect(state.isReady).to.be(false);
        });

        it('should be false while disconnecting', function(){

            var state = new SarosState();
            state.activateAccount(getDummyAccount());
            state.connectionState = 'DISCONNECTING';
            expect(state.isReady).to.be(false);
        });
    });

    describe('#activeAccount', function(){

        it('should be active after activating', function(){
          
            var state = new SarosState();
            var account = getDummyAccount();
            state.activateAccount(account);
            expect(account.isActive).to.be(true);
        });

        it('should not be active after activating another', function(){
          
            var state = new SarosState();
            var acc1 = getDummyAccount();
            var acc2 = getDummyAccount();
            state.activateAccount(acc1);
            state.activateAccount(acc2);
            expect(acc1.isActive).to.be(false);
        });        
    });
});
