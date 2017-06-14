import SarosStore from './SarosStore'
import ViewStore from './ViewStore'
import MainUIStore from './MainUIStore'
import SessionUIStore from './SessionUIStore'

/**
 * creates all stores, which store the application data
 * @param {string} initialPage - the Page that the Application initially opens
 */
export default function initStores (initialPage) {
  const view = new ViewStore(initialPage)
  const core = new SarosStore(view)
  return {
    view,
    core,
    mainUI: new MainUIStore(core, view),
    sessionUI: new SessionUIStore(core, view)
  }
}
