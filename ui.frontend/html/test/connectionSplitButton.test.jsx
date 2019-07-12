import 'jsdom-global/register'
import { itRendersWithoutExploding, SarosStoreBuilder } from './utils'
import { shallow, mount } from 'enzyme'
import { spy } from 'sinon'
import React from 'react'
import { expect } from 'chai';
import { connectionStates } from '~/constants'
import { getJid } from 'Utils'
import ConnectionSplitButton, { DropdownItem, DropdownItems } from '~/components/ToolBar/ConnectionSplitButton';


describe('<ConnectionSplitButton />', () => {


    function createEmptyStore() {
        return new SarosStoreBuilder().build()
    }

    itRendersWithoutExploding(mount(<ConnectionSplitButton core={createEmptyStore()} />).find('ConnectionSplitButton'))
    
    describe('<DropdownItem />', () => {

        function createTestContext(isActive) {
            const testJid = 'testuser@domain.de'

            const action = spy()
            const wrap = shallow(<DropdownItem isActive={isActive} jid={testJid} key={testJid} doChangeActiveAccount={action} />)

            return { action: action, wrap: wrap }
        }

        it('is active item and performs no action', () => {
            const context = createTestContext(true)
            
            context.wrap.simulate('click')
            expect(context.action.notCalled).to.be.true
        })

        it('is not active and performs an action', () => {
            const context = createTestContext(false)

            context.wrap.simulate('click')
            expect(context.action.calledOnce).to.be.true
        })
    })

    describe('<DropdownItems />', () => {

        it('shows only <EmptyDropdownItem /> if no accounts are provided', () => {
            const noAccounts = []
            const wrap = shallow(<DropdownItems accounts={noAccounts} />)

            expect(wrap.exists('EmptyDropdownItem')).to.be.true
            expect(wrap.exists('DropdownItem')).to.be.false
        })

        it('shows only <DropdownItem /> if accounts are provided', () => {
            const otherAccount = { username: 'test1', password: 'testpwd1', domain: 'domain', server: 'server' }
            const activeAccount = { username: 'activeUser', password: 'activePwd', domain: 'activeDomain', server: 'activeServer' }
            const accounts = [ otherAccount, activeAccount ]
            const wrap = shallow(<DropdownItems accounts={accounts} activeAccount={activeAccount} />)

            expect(wrap.exists('EmptyDropdownItem')).to.be.false

            const items = wrap.find('DropdownItem')
            expect(items).to.have.lengthOf(accounts.length)

            expect(items.get(0).props.isActive).to.be.true
            expect(items.get(0).props.jid).to.be.equal(getJid(activeAccount))

            expect(items.get(1).props.isActive).to.be.false
            expect(items.get(1).props.jid).to.be.equal(getJid(otherAccount))
        })
    })

    describe('<SplitButton />', () => {

        function findConnectionStateButton(wrap) {
            return wrap.find('#connection-split-button')
        }

        function createTestContext(state) {
            const core = new SarosStoreBuilder()
                .withConnectionState(state)
                .build()
            const wrap = shallow(<ConnectionSplitButton core={core} />)
            return { core: core, wrap: wrap }
        }

        it('establishes a connection on click if current state is INITIALIZING', () => {
            const context = createTestContext(connectionStates.INITIALIZING)

            findConnectionStateButton(context.wrap).simulate('click')
            expect(context.core.sarosApi.connect.calledOnce).to.be.true
        })

        it('establishes a connection on click if current state is NOT_CONNECTED', () => {
            const context = createTestContext(connectionStates.NOT_CONNECTED)

            findConnectionStateButton(context.wrap).simulate('click')
            expect(context.core.sarosApi.connect.calledOnce).to.be.true
        })

        it('disconnects on click if current state is CONNECTED', () => {
            const context = createTestContext(connectionStates.CONNECTED)

            findConnectionStateButton(context.wrap).simulate('click')
            expect(context.core.sarosApi.disconnect.calledOnce).to.be.true
        })

        const noopStates = [connectionStates.ERROR, connectionStates.DISCONNECTING, connectionStates.CONNECTING]
        noopStates.forEach(state => {
            it(`does nothing on click if current state is ${state}`, () => {
                const context = createTestContext(state)

                findConnectionStateButton(context.wrap).simulate('click')
                expect(context.core.sarosApi.disconnect.notCalled).to.be.true
                expect(context.core.sarosApi.connect.notCalled).to.be.true
            })
        })
    })

})
