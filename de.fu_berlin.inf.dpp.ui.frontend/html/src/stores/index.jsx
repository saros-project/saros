import SarosStore from './SarosStore'
import ViewStore from './ViewStore'
import MainUIStore from './MainUIStore'

export default function initStores (initialPage) {
  const view = new ViewStore(initialPage)
  const core = new SarosStore(view)
  return {
    view,
    core,
    mainUI: new MainUIStore(core, view)
  }
}
