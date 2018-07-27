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
  doShowRenameContactView (jid) {
    this.view.doChangeView(views.ADD_CONTACT, { rename: true, jid })
  }

  @action.bound
  doSubmitAddContact (jid, displayName, rename = false) {
    if (rename) {
      this.core.doRenameContact(jid, displayName)
    } else {
      this.core.doAddContact(jid, displayName)
    }
    this.view.doChangeView(views.MAIN)
  }

  @action.bound
  doCancelAddContact () {
    this.view.doChangeView(views.MAIN)
  }
}
