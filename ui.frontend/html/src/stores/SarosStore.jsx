import { action, computed, observable } from 'mobx'
import { getJid, onlineFirst } from 'Utils'
import { connectionStates } from '~/constants'

export default class SarosStore {
  @observable state = {
    activeAccount: {
      username: null,
      password: null,
      domain: null,
      server: '',
      port: 0,
      useTLS: false,
      useSASL: false
    },
    contactList: [],
    connectionState: connectionStates.INITIALIZING
  }

  @observable accounts = []
  @observable projectTrees = []
  @observable runningSession = null

  sarosApi = null

  @computed
  get sortedContactList () {
    return this.state.contactList.slice(0).sort(onlineFirst)
  }

  @action.bound
  doUpdateAccounts (accounts) {
    this.accounts = accounts
  }

  @action.bound
  doUpdateState (state) {
    this.state = { ...this.state, ...state }
  }

  @action.bound
  doUpdateContacts () {
    // TODO
  }

  @action.bound
  doUpdateRunningSession (runningSession) {
    this.runningSession = runningSession
  }

  @action.bound
  doUpdateProjectTrees (projectTrees) {
    this.projectTrees = projectTrees
  }

  @action.bound
  doChangeActiveAccount (jid) {
    const account = this.accounts.find(account => getJid(account) === jid)
    this.sarosApi.connect(account)
  }

  @action.bound
  doAddContact (jid, displayName) {
    this.sarosApi.addContact(jid, displayName)
  }

  @action.bound
  doShowShareProjectPage () {
    this.sarosApi.showShareProjectPage()
  }

  @action.bound
  doCloseShareProjectPage () {
    this.sarosApi.closeShareProjectPage()
  }

  @action.bound
  doShowAddContactPage () {
    this.sarosApi.showAddContactPage()
  }

  @action.bound
  doCloseAddContactPage () {
    this.sarosApi.closeAddContactPage()
  }
  @action.bound
  doSendInvitation (projectTrees, contacts) {
    this.sarosApi.sendInvitation(projectTrees, contacts)
  }

  @action.bound
  doConnect () {
    this.sarosApi.connect(this.state.activeAccount)
  }

  @action.bound
  doDisconnect () {
    this.sarosApi.disconnect()
  }
}
