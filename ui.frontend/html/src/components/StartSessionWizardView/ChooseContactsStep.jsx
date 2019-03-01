import { inject, observer } from 'mobx-react'
import ContactList from '../ContactList'
import React from 'react'

@inject('sessionUI')
@observer
export default class ChooseContactsStep extends React.Component {
  toggleSelectContact = (e) => {
    const jid = e.target.dataset.jid
    const selectedContacts = new Set(this.props.sessionUI.selectedContacts)
    if (selectedContacts.has(jid)) {
      selectedContacts.delete(jid)
    } else {
      selectedContacts.add(jid)
    }
    this.props.sessionUI.setSelectedContacts(selectedContacts)
  }

  render () {
    const { availableContacts, selectedContacts } = this.props.sessionUI
    if (!selectedContacts) {
      return null
    }
    return (
      <ContactList
        contactList={availableContacts}
        selectedContacts={selectedContacts}
        onClickContact={this.toggleSelectContact}
      />
    )
  }
}
