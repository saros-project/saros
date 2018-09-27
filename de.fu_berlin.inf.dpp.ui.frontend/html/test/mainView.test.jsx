import 'jsdom-global/register'
import { FakeSarosApi, itRendersWithoutExploding, wrapWithContextProvider } from './utils'
import { mount } from 'enzyme'
import { spy } from 'sinon'
import MainView from '~/components/MainView'
import React from 'react'
import initStores from '~/stores'

describe('<MainView />', () => {
  // We wire our stores up with the fake saros api
  const fakeApi = new FakeSarosApi()
  // Doesn't matter what page we put here
  const stores = initStores('main-page', fakeApi)
  stores.mainUI = { doShowAddContactView: spy() }
  // create the Store context and Mount the component
  const wrap = mount(wrapWithContextProvider(<MainView />, stores))

  itRendersWithoutExploding(wrap.find('MainView'))

  it('opens the SessionWizard when the Start Session button is clicked', () => {
    wrap.find('.ssw-btn').simulate('click')
    expect(fakeApi.showStartSessionWizard.calledOnce).to.equal(true)
  })

  it('opens the AddContact View when the Add Contact button is clicked', () => {
    wrap.find('.ac-btn').simulate('click')
    expect(stores.mainUI.doShowAddContactView.calledOnce).to.equal(true)
  })
})
