import { inject, observer } from 'mobx-react'

import ActiveAccount from './ActiveAccount'
import ContactList from '../ContactList'
import React from 'react'
import RunningSession from './RunningSession'
import ToolBar from '../ToolBar'

export default
@inject('core', 'mainUI')
@observer
class MainView extends React.Component {
  render () {
    const { core, mainUI } = this.props
    return (
      <div className="main-view">
        <ToolBar core={core} mainUI={mainUI} />
        <div className='content-container'>
          <ActiveAccount activeAccount={core.state.activeAccount} />
          <RunningSession runningSession={core.runningSession} />
          <ContactList contactList={core.sortedContactList} />
        </div>
      </div>
    )
  }
}
