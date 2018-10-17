import { Text } from 'react-localize'
import { noop } from 'Utils'
import Contact from './Contact'
import React from 'react'
import cn from 'classnames'

const ContactList = ({
  contactList,
  selectedContacts = new Set(),
  onClickContact = noop
}) => (
  <div className='contact-list-container'>
    <h6><Text message='headline.contacts' /></h6>
    <ul className='contact-list list-group'>
      {contactList.map(contact => (
        <li onClick={onClickContact}
          data-jid={contact.jid}
          key={contact.jid}
          className={cn('list-group-item', { active: selectedContacts.has(contact.jid) })}
        >
          <Contact {...contact} />
        </li>
      ))}
    </ul>
  </div>
)

export default ContactList
