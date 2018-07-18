import { action } from 'mobx'
import { views } from 'Constants'

export default class MainUIStore {
  constructor (core, view) {
    this.core = core
    this.view = view
  }

  @action.bound
  doShowAddContactView () {
    this.view.doChangeView(views.ADD_CONTACT)
  }

  @action.bound
  doSubmitAddContact (jid, displayName) {
    this.core.doAddContact(jid, displayName)
    this.view.doChangeView(views.MAIN)
  }

  @action.bound
  doCancelAddContact () {
    this.view.doChangeView(views.MAIN)
  }
}
