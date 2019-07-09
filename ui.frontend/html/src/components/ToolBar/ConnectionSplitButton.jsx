import { Account } from 'Utils/propTypes'
import { Dropdown, OverlayTrigger, SplitButton, Tooltip } from 'react-bootstrap'
import { PropTypes as PM } from 'mobx-react'
import { connectionStates } from '~/constants'
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
    this.connectionStateMap = {}
    this.connectionStateMap[connectionStates.INITIALIZING] = { onClick: this.props.onConnect, messageId: 'action.connect', icon: images.accountDisconnectedIcon }
    this.connectionStateMap[connectionStates.NOT_CONNECTED] = { onClick: this.props.onConnect, messageId: 'action.connect', icon: images.accountDisconnectedIcon }
    this.connectionStateMap[connectionStates.CONNECTED] = { onClick: this.props.onDisconnect, messageId: 'action.disconnect', icon: images.accountConnectedIcon }
    this.connectionStateMap[connectionStates.ERROR] = { onClick: noop, messageId: 'action.connectionError', icon: images.accountConnectionErrorIcon }
    this.connectionStateMap[connectionStates.CONNECTING] = { onClick: noop, messageId: 'action.connecting', icon: images.accountConnectingIcon }
    this.connectionStateMap[connectionStates.DISCONNECTING] = { onClick: noop, messageId: 'action.disconnecting', icon: images.accountDisconnectingIcon }
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
