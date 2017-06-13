import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'mobx-react'
import Localization from 'react-localize'

import './styles/style.css'
import App from './components/App'
import initStores from './stores'
import SarosApi from './SarosApi'
import dictionary from './dictionary'

// The initialView is injected via the html page

const stores = initStores(window.initialPage)
const api = new SarosApi(stores.core)
stores.core.sarosApi = api

// Expose the Saros API globally to be accessible for Java
window.SarosApi = api

// For debugging purposes, expose all stores so it can be tested in the browser
if (process.env.NODE_ENV !== 'production') {
  window.stores = stores
}

ReactDOM.render(
  <Provider {...stores} >
    <Localization messages={dictionary}>
      <App />
    </Localization>
  </Provider>
  ,
  document.querySelector('#root')
)
