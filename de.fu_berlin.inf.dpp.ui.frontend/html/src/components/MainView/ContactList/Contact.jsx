import React from 'react'
import { Text } from 'react-localize'
import { Contact as ContactProps } from 'Utils/propTypes'

const Contact = ({
  displayName,
  presence,
  addition
}) => (
  <div className='contact-item'>
    <span>{displayName}</span>
    <span className='text-muted ml-1'>{ addition && `(${addition})`}</span>
    {presence === 'Online' &&
      <span className='badge badge-success'>
        <Text message='label.online' />
      </span>
    }
    <div data-hook='context-menu'>
      <ul role='menu' className='dropdown-menu'>
        <li><Text message='action.rename' /></li>
        <li><Text message='action.delete' /></li>
      </ul>
    </div>
  </div>
)

Contact.propTypes = ContactProps

export default Contact
