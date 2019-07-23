import 'jsdom-global/register'
import { FakeSarosApi, itRendersWithoutExploding, wrapWithContextProvider } from './utils'
import { mount } from 'enzyme'
import MainView from '~/components/MainView'
import React from 'react'
import initStores from '~/stores'
import { expect } from 'chai';

describe('<MainView />', () => {
  // We wire our stores up with the fake saros api
  const fakeApi = new FakeSarosApi()
  // Doesn't matter what page we put here
  const stores = initStores('main-page', fakeApi)
  // create the Store context and Mount the component
  const wrap = mount(wrapWithContextProvider(<MainView />, stores))

  itRendersWithoutExploding(wrap.find('MainView'))

  it('opens the ShareProjectPage when the Start Session button is clicked', () => {
    wrap.find('button#start-session').simulate('click')
    expect(fakeApi.showShareProjectPage.calledOnce).to.equal(true)
  })

  it('opens the AddContact View when the Add Contact button is clicked', () => {
    wrap.find('button#add-contact').simulate('click')
    expect(fakeApi.showAddContactPage.calledOnce).to.equal(true)
  })
})
