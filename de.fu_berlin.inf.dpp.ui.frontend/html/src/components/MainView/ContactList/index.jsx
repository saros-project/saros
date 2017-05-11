import React from 'react'
import { Text } from 'react-localize'
import Contact from './Contact'

const onlineFirst = (a, b) => {
  const on = x => x.presence === 'Online'
  if (on(a) && !on(b)) {
    return -1
  } else if (on(b) && !on(a)) {
    return 1
  }
  return a.displayName.localeCompare(b.displayName)
}

const ContactList = ({
  contactList
}) => (
  <div className='contact-list-container'>
    <h6><Text message='headline.contacts' /></h6>
    <ul className='contact-list list-group'>
      {contactList.sort(onlineFirst).map(contact => (
        <li key={contact.jid} className='list-group-item'>
          <Contact {...contact} />
        </li>
      ))}
    </ul>
  </div>
)

export default ContactList
