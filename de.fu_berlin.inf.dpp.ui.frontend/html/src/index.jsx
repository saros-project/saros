import './styles/style.css'
import * as runningSessionMock from '../test/runningSession.json'
import { Provider } from 'mobx-react'
import App from './components/App'
import Localization from 'react-localize'
import React from 'react'
import ReactDOM from 'react-dom'
import SarosApi from './SarosApi'
import dictionary from './dictionary'
import initStores from './stores'

// The initialView is injected via the html page
const api = new SarosApi()
const stores = initStores(window.initialPage, api)

// Expose the Saros API globally to be accessible for Java
window.SarosApi = api

// TODO remove this once the updateRunningSession action is implemented
api.trigger('updateRunningSession', runningSessionMock)

// For debugging purposes, expose all stores so it can be tested in the browser
if (process.env.NODE_ENV !== 'production') {
  window.stores = stores
}

ReactDOM.render(
  <Provider {...stores} api={api} >
    <Localization messages={dictionary}>
      <App />
    </Localization>
  </Provider>
  ,
  document.querySelector('#root')
)
