import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'mobx-react'
import Localization from 'react-localize'

import './styles/style.css'
import App from './components/App'
import SarosStore from './stores/SarosStore'
import SarosApi from './SarosApi'
import dictionary from './dictionary'

const store = new SarosStore()
const api = new SarosApi(store)
store.sarosApi = api

// Expose the Saros API globally to be accessible for Java
window.SarosApi = api
window.SarosStore = store

ReactDOM.render(
  <Provider store={store} >
    <Localization messages={dictionary}>
      <App />
    </Localization>
  </Provider>
  ,
  document.querySelector('#root')
)
