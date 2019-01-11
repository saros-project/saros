import ConfigurationUIStore from './ConfigurationUIStore'
import MainUIStore from './MainUIStore'
import SarosStore from './SarosStore'
import SessionUIStore from './SessionUIStore'
import ViewStore from './ViewStore'

/**
 * creates all stores, which store the application data
 * @param {string} initialPage - the Page that the Application initially opens
 * @param {ISarosApi} api - the saros-core interface to use
 * @returns {Object} - An object containing all stores
 */
export default function initStores (initialPage, api) {
  const view = new ViewStore(initialPage)
  const core = new SarosStore(view)
  core.sarosApi = api
  api.sarosStore = core
  api.viewStore = view
  return {
    view,
    core,
    mainUI: new MainUIStore(core, view),
    sessionUI: new SessionUIStore(core, view),
    configurationUI: new ConfigurationUIStore(core, view)
  }
}
