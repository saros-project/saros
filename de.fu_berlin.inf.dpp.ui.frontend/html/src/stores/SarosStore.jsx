import { action, computed, observable } from 'mobx'
import { getJid, onlineFirst } from 'Utils'

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
    connectionState: 'INITIALIZING'
  }

  @observable accounts = []
  @observable projectTrees = null
  @observable runningSession = null

  sarosApi = null

  @computed
  get sortedContactList () {
    return this.state.contactList.sort(onlineFirst)
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
  doShowStartSessionWizard () {
    this.sarosApi.showStartSessionWizard()
  }

  @action.bound
  doCloseSessionWizard () {
    this.sarosApi.closeStartSessionWizard()
  }

  @action.bound
  doSendInvitation (projectTrees, contacts) {
    this.sarosApi.sendInvitation(projectTrees, contacts)
  }
}
