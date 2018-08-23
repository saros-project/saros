import { Contact as ContactProps } from 'Utils/propTypes'
import React from 'react'
import { Text } from 'react-localize'

const Contact = ({
  displayName,
  presence,
  addition,
  jid,
  renderOps,
}) => (
  <div className='contact-item row'>
    <div className='col-xs-7'>
      <span className='contact-item-display-name'>{displayName || jid}</span>
      <span className='text-muted ml-1'>{ addition && `(${addition})`}</span>
      {presence === 'Online' &&
        <span className='badge badge-success'>
          <Text message='label.online' />
        </span>
      }
    </div>
    { renderOps &&
      <div className='contact-ops col-xs-5'>
        {renderOps(jid, displayName, presence, addition)}
      </div>
    }
  </div>
)

Contact.propTypes = ContactProps

export default Contact
