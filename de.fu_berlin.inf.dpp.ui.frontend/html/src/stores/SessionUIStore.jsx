import { action, observable, computed } from 'mobx'
import invariant from 'invariant'

/**
 * @param {Object} tree - tree to traverse over
 * @param {string} keyPrefix - the tree path of the current child e.g '0-1-2-3'
 * @param {Function} onChild - called when a child is being traversed over
 */
function traverse (tree, keyPrefix, onChild) {
  // For some reason the prefix gets printed
  // as a fraction when this isnt done
  // We want it as integer
  if (typeof keyPrefix === 'number') {
    keyPrefix = keyPrefix.toFixed(0)
  }
  // rc-tree prefixes everything with 0, so we need to do it as well
  onChild(tree, `0-${keyPrefix}`)
  tree.members.forEach((child, i) => {
    traverse(child, `${keyPrefix}-${i}`, onChild)
  })
}

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
   * The outcome gets passed to the sendInvitation browser function
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
   * The outcome gets passed to the sendInvitation browser function
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
