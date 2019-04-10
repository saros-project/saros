import '../node_modules/bootstrap/dist/css/bootstrap.css';
import './styles/style.css';

import { Provider } from 'mobx-react'
import App from './components/App'
import Localization from 'react-localize'
import React from 'react'
import ReactDOM from 'react-dom'
import SarosApi from './SarosApi'
import dictionary from './dictionary'
import initStores from './stores'
import ContextMenu from './components/ContextMenu/Component';
import ContextMenuController from './components/ContextMenu/Controller';

// The initialView is injected via the html page
const api = new SarosApi()
const stores = initStores(window.initialPage, api)

// Expose the Saros API globally to be accessible for Java
window.SarosApi = api

// For debugging purposes, expose all stores so it can be tested in the browser
window.stores = stores

const ctxMenuController = new ContextMenuController(stores.core);

ReactDOM.render(
  <Provider {...stores} api={api} contextMenu={ctxMenuController}>
    <Localization messages={dictionary}>
      <App />
    </Localization>
  </Provider>,
  document.querySelector('#root')
)

ReactDOM.render(
  <Provider {...stores}>
    <Localization messages={dictionary}>
      <ContextMenu ref={menu => ctxMenuController.setRef(menu)} />
    </Localization>
  </Provider>,
  document.getElementById('contextMenuContainer')
);


// TODO remove this once the api is fully implemented
import SessionMock from '../test/runningSession.json'
import ProjectTreeMock from '../test/projectTrees.json'
api.trigger('updateRunningSession', SessionMock)
api.trigger('updateProjectTrees', ProjectTreeMock)
