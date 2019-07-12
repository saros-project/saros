import 'jsdom-global/register'
import { itRendersWithoutExploding, SarosStoreBuilder } from './utils'
import { shallow, mount } from 'enzyme'
import ToolBar from '~/components/ToolBar'
import React from 'react'
import { expect } from 'chai';
import { connectionStates } from '~/constants'

describe('<ToolBar />', () => {

	function createToolBarShallowWrap(core) {
		return shallow(<ToolBar core={core} />)
	}

	function createEmptyStore() {
		return new SarosStoreBuilder().build()
	}


	function findAddContactButton(wrap) {
		return wrap.find('#add-contact')
	}

	function findStartSessionButton(wrap) {
		return wrap.find('#start-session')
	}

	itRendersWithoutExploding(mount(<ToolBar core={createEmptyStore()} />).find('ToolBar'))

	describe('without an established connection', () => {

		let wrap

		beforeEach(() => {
			wrap = createToolBarShallowWrap(createEmptyStore())
		})

		it('add contact/start session button is disabled if no connection is established', () => {
			const addContactButton = findAddContactButton(wrap)
			const startSessionButton = findStartSessionButton(wrap)
	
			expect(addContactButton.prop('disabled')).to.equal(true)
			expect(startSessionButton.prop('disabled')).to.equal(true)
		})
	})


	describe('with an established connection', () => {

		let wrap
		let core

		beforeEach(() => {
			core = new SarosStoreBuilder()
				.withConnectionState(connectionStates.CONNECTED)
				.build()
			wrap = createToolBarShallowWrap(core)
		})

		it('opens the AddContact View when the Add Contact button is clicked', () => {
			findAddContactButton(wrap).simulate('click')
			expect(core.sarosApi.showAddContactPage.calledOnce).to.equal(true)
		})

		it('opens the ShareProject View when the start session button is clicked', () => {
			findStartSessionButton(wrap).simulate('click')
			expect(core.sarosApi.showShareProjectPage.calledOnce).to.equal(true)
		})

		it('add contact/start session button is enable', () => {
			const addContactButton = findAddContactButton(wrap)
			const startSessionButton = findStartSessionButton(wrap)

			expect(addContactButton.prop('disabled')).to.equal(false)
			expect(startSessionButton.prop('disabled')).to.equal(false)
		})
	})

})
