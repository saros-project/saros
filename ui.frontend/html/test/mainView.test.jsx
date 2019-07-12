import 'jsdom-global/register'
import { FakeSarosApi, itRendersWithoutExploding, wrapWithContextProvider } from './utils'
import { mount } from 'enzyme'
import MainView from '~/components/MainView'
import React from 'react'
import initStores from '~/stores'

describe('<MainView />', () => {
  // We wire our stores up with the fake saros api
  const fakeApi = new FakeSarosApi()
  // Doesn't matter what page we put here
  const stores = initStores('main-page', fakeApi)
  // create the Store context and Mount the component
  const wrap = mount(wrapWithContextProvider(<MainView />, stores))

  itRendersWithoutExploding(wrap.find('MainView'))
})
