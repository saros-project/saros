import { action, observable } from 'mobx'
import { views } from 'Constants'
import invariant from 'invariant'

export default class ViewStore {
  @observable currentView

  constructor (initialView) {
    this.currentView = initialView
  }

  @action.bound
  doChangeView (viewname) {
    invariant(Object.values(views).includes(viewname), `View ${viewname} does not exist`)
    this.currentView = viewname
  }
}
