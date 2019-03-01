import { Text } from 'react-localize'
import React from 'react'

function SessionMember ({ openedFile, jid, displayName, isHost }) {
  return (
    <div className='session-member'>
      <div>
        {displayName || jid}
        {isHost &&
          <span className='badge badge-success'>
            <Text message='label.host' />
          </span>
        }
      </div>
      <div>
        {
          openedFile ||
          <Text message='message.nonSharedFileOpen' />
        }
      </div>
    </div>
  )
}

export default function RunningSession ({ runningSession }) {
  if (!runningSession) {
    return null
  }
  const { members } = runningSession
  return (
    <ul className='list-group'>
      {members.map(member => (
        <li className='list-group-item'>
          <SessionMember {...member} />
        </li>
      ))}
    </ul>
  )
}
