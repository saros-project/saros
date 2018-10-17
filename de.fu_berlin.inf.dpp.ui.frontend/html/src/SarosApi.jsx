/*
 * Saros API Documentation
 *
 * In addition to the methods listed below you can trigger and listen
 * to events on the SarosAPI object.
 *
 * To trigger an event do:
 * `SarosApi.trigger('eventName' [, data]);`
 *
 * To listen to an event do:
 * `SarosApi.on('eventName', handler);`
 * where the first parameter of the handler function is the data object,
 * if available.
 *
 * The following events are provided:
 *
 * updateState
 * updateAccounts
 * updateContacts
 * updateProjectTrees
 *
 */

class SarosApi {
  constructor (sarosStore, viewStore) {
    this.sarosStore = sarosStore
    this.viewStore = viewStore
  }

  trigger (event, ...args) {
    switch (event) {
      case 'updateState':
        return this.sarosStore.doUpdateState(args[0])
      case 'updateAccounts':
        return this.sarosStore.doUpdateAccounts(args[0])
      case 'updateContacts':
        return this.sarosStore.doUpdateContacts(args[0])
      case 'updateProjectTrees':
        return this.sarosStore.doUpdateProjectTrees(args[0])
      case 'updateRunningSession':
        return this.sarosStore.doUpdateRunningSession(args[0])
    }
  }

  connect (account) {
    if (typeof window.__java_connect !== 'undefined') {
      window.__java_connect(JSON.stringify(account))
    }
  }

  disconnect () {
    window.__java_disconnect()
  }

  manageAccounts () {
    window.__java_showSarosPreferencesWizard()
  }

  addContact (jid, displayName) {
    window.__java_addContact(jid, displayName)
  }

  renameContact (jid, displayName) {
    window.__java_renameContact(jid, displayName)
  }

  deleteContact (jid) {
    window.__java_deleteContact(jid)
  }

  validateJid (jid) {
    return JSON.parse(window.__java_validateJid(jid))
  }

  showStartSessionWizard () {
    window.__java_showSessionWizard()
  }

  closeStartSessionWizard () {
    window.__java_closeStartSessionWizard()
  }

  sendInvitation (projectTrees, contacts) {
    window.__java_sendInvitation(JSON.stringify(projectTrees), JSON.stringify(contacts))
  }

  /**
   * @returns {Object} a map of the colors the user can choose in the config wizard
   */
  getUserColorSet () {
    // TODO implement an actual Browser Function for this
    return {
      lightblue: '#9CCFE4',
      brown: '#BEBB88',
      green: '#BEDA69',
      darkcyan: '#8DB4B1'
    }
  }
}

export default SarosApi
