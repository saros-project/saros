import { Button, Navbar } from 'react-bootstrap'
import { Text } from 'react-localize'
import { connectionStates } from '~/constants'

import ConnectionSplitButton from './ConnectionSplitButton'
import React from 'react'

const isNotConnected = ( connectionState ) => (connectionState != connectionStates.CONNECTED)

const ToolBar = ({ core }) => (
  <Navbar fixed={"top"}>
    <ConnectionSplitButton core={ core } />

    <Button id='add-contact' onClick={core.doShowAddContactPage} disabled={ isNotConnected(core.state.connectionState) }>
      <Text message='action.addContact' />
    </Button>

    <Button id='start-session' onClick={core.doShowShareProjectPage} disabled={ isNotConnected(core.state.connectionState) }>
      <Text message='action.startSession' />
    </Button>
  </Navbar>
)

export default ToolBar
