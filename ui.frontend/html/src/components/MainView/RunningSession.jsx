import { Text } from 'react-localize'
import React from 'react'

function SessionMember ({ openedFile, jid, displayName, isHost }) {
  return (
    <div className='session-member'>
      <div>
        {displayName || jid}
        {isHost && <span className='badge badge-success'><Text message='label.host' /></span>}
      </div>
      <div>
        {openedFile || <Text message='message.nonSharedFileOpen' />}
      </div>
    </div>
  )
}

export default function RunningSession ({ runningSession }) {
  if (!runningSession) {
    return null
  }

  let members = runningSession.members;
  return (
    <ul className='list-group'>
      {members && members.map(member => (
        <li className='list-group-item' key={member.jid}>
          <SessionMember {...member} />
        </li>
      ))}
    </ul>
  )
}
