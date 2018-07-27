import { observable, action } from 'mobx'
import { views } from 'Constants'
import invariant from 'invariant'

export default class ViewStore {
  @observable currentView
  @observable intent = { }

  constructor (initialView) {
    this.currentView = initialView
  }

  @action.bound
  doChangeView (viewname, intent = { }) {
    invariant(Object.values(views).includes(viewname), `View ${viewname} does not exist`)
    this.currentView = viewname
    this.intent = intent
  }
}
