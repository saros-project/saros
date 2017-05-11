import { observable, action } from 'mobx'

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

  @action
  doUpdateAccounts (accounts) {
    this.accounts = accounts
  }

  @action
  doUpdateState (state) {
    this.state = { ...this.state, ...state }
  }

  @action
  doUpdateContacts () {
    // TODO
  }

  @action
  doChangeActiveAccount (accId) {
    this.sarosApi && this.sarosApi.connect(this.accounts.find(({ jid }) => jid === accId))
  }
}
