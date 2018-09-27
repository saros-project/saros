import { Provider } from 'mobx-react'
import { spy, stub } from 'sinon'
import Localization from 'react-localize'
import React from 'react'

export class FakeSarosApi {
  constructor () {
    this.connect = spy()
    this.disconnect = spy()
    this.manageAccounts = spy()
    this.addContact = spy()
    this.renameContact = spy()
    this.deleteContact = spy()
    this.validateJid = stub().returns(true)
    this.showStartSessionWizard = spy()
    this.closeStartSessionWizard = spy()
    this.sendInvitation = spy()
  }
}

export function wrapWithContextProvider (element, stores) {
  return (
    <Localization messages={{}}>
      <Provider {...stores} >
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
    expect(wrapper.length).to.equal(1)
  })
}
