import { observable, action, computed } from 'mobx'
import { onlineFirst, getJid } from 'Utils'

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
  doChangeActiveAccount (jid) {
    const account = this.accounts.find(account => getJid(account) === jid)
    this.sarosApi.connect(account)
  }

  @action.bound
  doAddContact (jid, displayName) {
    this.sarosApi.addContact(jid, displayName)
  }
}
