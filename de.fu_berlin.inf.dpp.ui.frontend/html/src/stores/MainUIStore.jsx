import { action } from 'mobx'
import { views } from 'Constants'

export default class MainUIStore {
  constructor (core, view) {
    this.core = core
    this.view = view
  }

  @action.bound
  doShowAddContactView () {
    // TODO
  }

}
