import { Dropdown, OverlayTrigger, SplitButton, Tooltip } from 'react-bootstrap'
import { connectionStates } from '~/constants'
import { Text } from 'react-localize'
import { getJid, noop } from 'Utils'

import React from 'react'
import images from '~/images'

export const EmptyDropdownItem = () => (<Dropdown.Item disabled><Text message='message.noAccountConfigured' /></Dropdown.Item>)

export const DropdownItem = ({ isActive, jid, doChangeActiveAccount }) => (
  <Dropdown.Item active={isActive} key={jid} eventKey={jid} onClick={isActive ? noop : () => doChangeActiveAccount(jid)}>{jid}</Dropdown.Item>
)

export const DropdownItems = ({ accounts, activeAccount, doChangeActiveAccount }) => (
  <div>
    {accounts.length <= 0 &&
      <EmptyDropdownItem />
    }
    {accounts.map(getJid).sort().map(jid => (
      <DropdownItem isActive={getJid(activeAccount) == jid} jid={jid} key={jid} doChangeActiveAccount={doChangeActiveAccount} />
    ))}
  </div>
)

class ConnectionSplitButton extends React.Component {
  constructor(props) {
    super(props)
    const onConnect = this.props.core.doConnect
    const onDisconnect = this.props.core.doDisconnect

    this.connectionStateMap = {}
    this.connectionStateMap[connectionStates.INITIALIZING] = { onClick: onConnect, messageId: 'action.connect', icon: images.accountDisconnectedIcon }
    this.connectionStateMap[connectionStates.NOT_CONNECTED] = { onClick: onConnect, messageId: 'action.connect', icon: images.accountDisconnectedIcon }
    this.connectionStateMap[connectionStates.CONNECTED] = { onClick: onDisconnect, messageId: 'action.disconnect', icon: images.accountConnectedIcon }
    this.connectionStateMap[connectionStates.ERROR] = { onClick: noop, messageId: 'action.connectionError', icon: images.accountConnectionErrorIcon }
    this.connectionStateMap[connectionStates.CONNECTING] = { onClick: noop, messageId: 'action.connecting', icon: images.accountConnectingIcon }
    this.connectionStateMap[connectionStates.DISCONNECTING] = { onClick: noop, messageId: 'action.disconnecting', icon: images.accountDisconnectingIcon }
  }


  render() {
    const { onClick, messageId, icon } = this.connectionStateMap[this.props.core.state.connectionState]
    const accounts = this.props.core.accounts
    const activeAccount = this.props.core.state.activeAccount
    const doChangeActiveAccount = this.props.core.doChangeActiveAccount

    return (
      <OverlayTrigger placement='bottom' overlay={<Tooltip><Text message={messageId} /></Tooltip>}>
        <SplitButton
          id='connection-split-button'
          title={<img src={icon} />}
          onClick={onClick}>

          <DropdownItems accounts={accounts} activeAccount={activeAccount} doChangeActiveAccount={doChangeActiveAccount} />

        </SplitButton>
      </OverlayTrigger>
    )
  }
}

export default ConnectionSplitButton
