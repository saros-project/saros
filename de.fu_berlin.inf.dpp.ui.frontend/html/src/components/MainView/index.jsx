import React from 'react'
import { Text } from 'react-localize'
import { inject, observer } from 'mobx-react'
import ContactList from './ContactList'
import Accounts from './Accounts'

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
            <button type='button' onClick={mainUI.doShowAddContactView} className='btn btn-default btn-sm add'>
              <Text message='action.addContact' />
            </button>
            <button type='button' onClick={core.doShowStartSessionWizard} className='btn btn-default btn-sm'>
              <Text message='action.startSession' />
            </button>
          </div>
        </nav>
        <div className='content-container'>
          <ContactList contactList={core.sortedContactList} />
        </div>
      </div>
    )
  }
}

export default MainView
