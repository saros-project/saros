import { action, computed, observable } from 'mobx'
import { traverse } from 'Utils'
import invariant from 'invariant'

export default class SessionUIStore {
  @observable.ref checkedKeys = new Set()
  @observable.ref selectedContacts = new Set()

  constructor (core, view) {
    this.core = core
    this.view = view
  }

  @computed
  get availableContacts () {
    return this.core.sortedContactList.filter(contact => contact.presence === 'Online')
  }

  /**
   * This functions takes the project trees passed by the Saros Core and
   * marks every file that is listed in 'checkedKeys' as 'isSelectedForSharing'
   * @returns {tree} The outcome gets passed to the sendInvitation browser function
   */
  packSharedProjects () {
    // cheap trick for deep copying this object
    const tree = JSON.parse(JSON.stringify(this.core.projectTrees))
    // This is to set the 'isSelectedForSharing' flag on all checked files
    tree.forEach((project, i) => {
      traverse(project.root, i, (child, keyPrefix) => {
        child.isSelectedForSharing = this.checkedKeys.has(keyPrefix)
      })
    })
    return tree
  }

  /**
   * Creates a list of all contacts that were selected in the Choose Contacts Step
   * @returns {object} The outcome gets passed to the sendInvitation browser function
   */
  packSelectedContacts () {
    return this.core.state.contactList.filter(
      contact => this.selectedContacts.has(contact.jid)
    )
  }

  @action.bound
  setCheckedKeys (checkedKeys) {
    this.checkedKeys = new Set(checkedKeys)
  }

  @action.bound
  setSelectedContacts (selectedContacts) {
    this.selectedContacts = selectedContacts
  }

  @action.bound
  submitSession () {
    invariant(this.checkedKeys.size && this.selectedContacts.size, `Invalid session data`)
    this.core.doSendInvitation(this.packSharedProjects(), this.packSelectedContacts())
    this.projectTrees = null
    this.selectedContacts = null
  }
}
