import { Provider } from 'mobx-react'
import { spy, stub } from 'sinon'
import Localization from 'react-localize'
import React from 'react'
import { expect } from 'chai';
import SarosStore from '~/stores/SarosStore'


export class FakeSarosApi {
  constructor () {
    this.connect = spy()
    this.disconnect = spy()
    this.manageAccounts = spy()
    this.addContact = spy()
    this.renameContact = spy()
    this.deleteContact = spy()
    this.validateJid = stub().returns(true)
    this.showShareProjectPage = spy()
    this.closeShareProjectPage = spy()
    this.sendInvitation = spy()
    this.showAddContactPage = spy()
    this.closeAddContactPage = spy()
    this.doChangeActiveAccount = spy()
  }
}

export class SarosStoreBuilder {
	constructor() {
		this.core = new SarosStore()
		this.core.sarosApi = new FakeSarosApi()
	}

	withConnectionState(connectionState) {
		this.core.doUpdateState({ connectionState: connectionState })
		return this
  }

	build() {
		return this.core
	}
}	

// Don't use this method if you want to create a shallow wrap in enzyme
// otherwise the component "element" is not rendered (only the Provider).
export function wrapWithContextProvider (element, stores) {
  return (
    <Localization messages={{}}>
      <Provider {...stores} contextMenu={{}}>
        {element}
      </Provider>
    </Localization>
  )
}

export function shouldRender (wrapper) {
  expect(wrapper.length).to.equal(1)
}

export function itRendersWithoutExploding (wrapper) {
  it('renders without exploding', () => {
    shouldRender(wrapper)
  })
}
