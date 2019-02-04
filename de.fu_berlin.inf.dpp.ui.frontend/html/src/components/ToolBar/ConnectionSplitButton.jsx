import { Account } from 'Utils/propTypes'
import { MenuItem, SplitButton } from 'react-bootstrap'
import { PropTypes as PM } from 'mobx-react'
import { Text } from 'react-localize'
import { getJid, noop } from 'Utils'

import React from 'react'

const ConnectionSplitButtonProps = {
  accounts: PM.observableArrayOf(Account).isRequired
}

class ConnectionSplitButton extends React.Component {
  constructor (props) {
    super(props)
    this.connectionStateMap = {
      'INITIALIZING': { onClick: this.props.onConnect, messageId: 'action.connect' },
      'NOT_CONNECTED': { onClick: this.props.onConnect, messageId: 'action.connect' },
      'CONNECTED': { onClick: this.props.onDisconnect, messageId: 'action.disconnect' },
      'ERROR': { onClick: noop, messageId: 'action.connectionError' },
      'CONNECTING': { onClick: noop, messageId: 'action.connecting' },
      'DISCONNECTING': { onClick: noop, messageId: 'action.disconnecting' }
    }
  }

  render () {
    const { accounts, connectionState } = this.props
    const { onClick, messageId } = this.connectionStateMap[connectionState]

    return (
      <SplitButton
        id='connection-split-button'
        title={<Text message={messageId} />}
        onClick={onClick}>

        {accounts.length <= 0 &&
        <MenuItem disabled><Text message='message.noAccountConfigured' /></MenuItem>
        }
        {accounts.map(getJid).map(jid => (
          <MenuItem
            key={jid}
            eventKey={jid}
      >
            {jid}
          </MenuItem>
    ))}
      </SplitButton>
    )
  }
}

ConnectionSplitButton.propTypes = ConnectionSplitButtonProps

export default ConnectionSplitButton
