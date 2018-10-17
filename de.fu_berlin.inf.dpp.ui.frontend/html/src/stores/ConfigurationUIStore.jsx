import { action, observable } from 'mobx'
import invariant from 'invariant'
export default class ConfigurationUIStore {
  @observable data = {
    account: {
      jid: '',
      password: ''
    },
    settings: {
      autoConnect: true,
      isSkypeUsernameVisible: false,
      skypeUsername: '',
      allowSubmitAnalytics: false,
      allowSubmitCrashReports: false
    },
    color: {
      selected: null
    }
  }

  assertFieldExists (page, field) {
    invariant(
      this.data[page] !== undefined &&
      this.data[page][field] !== undefined,
       `Invalid data target ${page}/${field}`
    )
  }

  @action changeWizardData (page, field, value) {
    this.assertFieldExists(page, field)
    this.data[page][field] = value
  }

  @action toggleWizardDataBool (page, field) {
    this.assertFieldExists(page, field)
    invariant(typeof this.data[page][field] === 'boolean', 'target field is not boolean')
    this.data[page][field] = !this.data[page][field]
  }
}
