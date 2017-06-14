import React from 'react'
import { Text } from 'react-localize'
import Contact from './Contact'

const ContactList = ({
  contactList
}) => (
  <div className='contact-list-container'>
    <h6><Text message='headline.contacts' /></h6>
    <ul className='contact-list list-group'>
      {contactList.map(contact => (
        <li key={contact.jid} className='list-group-item'>
          <Contact {...contact} />
        </li>
      ))}
    </ul>
  </div>
)

export default ContactList
