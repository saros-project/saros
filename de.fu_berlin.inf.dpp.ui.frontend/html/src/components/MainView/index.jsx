import { Text } from 'react-localize'
import { inject, observer } from 'mobx-react'
import Accounts from './Accounts'
import ContactList from '../ContactList'
import React from 'react'
import RunningSession from './RunningSession'

@inject('core', 'mainUI')
@observer
class MainView extends React.Component {
  render () {
    const { core, mainUI } = this.props
    return (
      <div>
        <nav className='navbar navbar-default'>
          <Accounts
            onChangeActiveAccount={core.doChangeActiveAccount}
            activeAccount={core.state.activeAccount}
            accounts={core.accounts}
          />
          <div className='btn-list'>
            <button id='add-contact' type='button' onClick={mainUI.doShowAddContactView} className='ac-btn btn btn-default btn-sm add'>
              <Text message='action.addContact' />
            </button>
            <button id='start-session' type='button' onClick={core.doShowStartSessionWizard} className='ssw-btn btn btn-default btn-sm'>
              <Text message='action.startSession' />
            </button>
          </div>
        </nav>
        <div className='content-container'>
          <RunningSession
            runningSession={core.runningSession}
          />
          <ContactList contactList={core.sortedContactList} />
        </div>
      </div>
    )
  }
}

export default MainView
