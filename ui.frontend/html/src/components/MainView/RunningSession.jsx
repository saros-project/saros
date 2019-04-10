import { Text } from 'react-localize'
import React from 'react'
import { inject } from 'mobx-react'
import FocusableItem from '../Focusable/FocusableItem';
import FocusableMenu from '../Focusable/FocusableMenu';

export default
@inject('contextMenu')
class RunningSession extends React.Component {

  showContextMenu(jid, event) {
    this.props.contextMenu.showSessionMemberMenu(jid, event.currentTarget, event);
  }

  render() {
    let runningSession = this.props.runningSession;
    if (!runningSession) {
      return null
    }

    let members = runningSession.members;
    return (
      <FocusableMenu>
        {members.map(({ openedFile, jid, displayName, isHost }, index) => (
          <FocusableItem key={jid} className= 'session-member'
            onContextMenu={e => this.showContextMenu(jid, e)}>
            <div>
              {displayName || jid}
              &nbsp;
              {isHost && <span className='badge badge-success'><Text message='label.host' /></span>}
            </div>
            <div>
              {openedFile || <Text message='message.nonSharedFileOpen' />}
            </div>
          </FocusableItem>
        ))}
      </FocusableMenu>
    )
  }
}
