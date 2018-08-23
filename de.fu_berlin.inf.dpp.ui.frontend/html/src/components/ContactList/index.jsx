import React from 'react'
import './style.css'
import { Text } from 'react-localize'
import Contact from './Contact'
import cn from 'classnames'
import { noop } from 'Utils'

const ContactList = ({
  contactList,
  selectedContacts = new Set(),
  onClickContact = noop,
  renderOps,
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
          <Contact {...contact} renderOps={renderOps} />
        </li>
      ))}
    </ul>
  </div>
)

export default ContactList
