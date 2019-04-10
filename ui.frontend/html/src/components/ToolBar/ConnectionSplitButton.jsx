import { Account } from 'Utils/propTypes'
import { Dropdown, OverlayTrigger, SplitButton, Tooltip } from 'react-bootstrap'
import { PropTypes as PM } from 'mobx-react'
import { Text } from 'react-localize'
import { getJid, noop } from 'Utils'

import React from 'react'
import images from '~/images'

const ConnectionSplitButtonProps = {
  accounts: PM.observableArrayOf(Account).isRequired
}

class ConnectionSplitButton extends React.Component {
  constructor (props) {
    super(props)
    this.connectionStateMap = {
      'INITIALIZING': { onClick: this.props.onConnect, messageId: 'action.connect', icon: images.accountDisconnectedIcon },
      'NOT_CONNECTED': { onClick: this.props.onConnect, messageId: 'action.connect', icon: images.accountDisconnectedIcon },
      'CONNECTED': { onClick: this.props.onDisconnect, messageId: 'action.disconnect', icon: images.accountConnectedIcon },
      'ERROR': { onClick: noop, messageId: 'action.connectionError', icon: images.accountConnectionErrorIcon },
      'CONNECTING': { onClick: noop, messageId: 'action.connecting', icon: images.accountConnectingIcon },
      'DISCONNECTING': { onClick: noop, messageId: 'action.disconnecting', icon: images.accountDisconnectingIcon }
    }
  }

  render () {
    const { accounts, connectionState } = this.props
    const { onClick, messageId, icon } = this.connectionStateMap[connectionState]

    return (
      <OverlayTrigger placement='bottom' overlay={<Tooltip><Text message={messageId} /></Tooltip>}>
        <SplitButton
          id='connection-split-button'
          title={<img src={icon} />}
          onClick={onClick}>

          {accounts.length <= 0 &&
            <Dropdown.Item disabled><Text message='message.noAccountConfigured' /></Dropdown.Item>
          }
          {accounts.map(getJid).map(jid => (
            <Dropdown.Item key={jid} eventKey={jid}>{jid}</Dropdown.Item>
          ))}
        </SplitButton>
      </OverlayTrigger>
    )
  }
}

ConnectionSplitButton.propTypes = ConnectionSplitButtonProps

export default ConnectionSplitButton
