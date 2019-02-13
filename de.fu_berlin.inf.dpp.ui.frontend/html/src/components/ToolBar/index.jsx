import { Button, Navbar } from 'react-bootstrap'
import { Text } from 'react-localize'

import ConnectionSplitButton from './ConnectionSplitButton'
import React from 'react'

const ToolBar = ({core, mainUI}) => (
  <Navbar fixedTop>
    <ConnectionSplitButton
      accounts={core.accounts}
      onConnect={core.doConnect}
      onDisconnect={core.doDisconnect}
      connectionState={core.state.connectionState}
      />

    <Button id='add-contact' onClick={mainUI.doShowAddContactView}>
      <Text message='action.addContact' />
    </Button>

    <Button id='start-session' onClick={core.doShowStartSessionWizard}>
      <Text message='action.startSession' />
    </Button>
  </Navbar>
)

export default ToolBar
