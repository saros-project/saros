import SarosStore from './SarosStore'
import ViewStore from './ViewStore'
import MainUIStore from './MainUIStore'
import SessionUIStore from './SessionUIStore'

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
  return {
    view,
    core,
    mainUI: new MainUIStore(core, view),
    sessionUI: new SessionUIStore(core, view),
  }
}
