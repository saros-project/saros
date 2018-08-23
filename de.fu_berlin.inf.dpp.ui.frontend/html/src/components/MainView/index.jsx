import './style.css'
import React from 'react'
import { Text } from 'react-localize'
import { inject, observer } from 'mobx-react'
import ContactList from '../ContactList'
import Accounts from './Accounts'
import RunningSession from './RunningSession'

const mapStoresToProps = ({ core, mainUI }) => {
  return {
    core,
    mainUI,
    activeAccount: core.state.activeAccount,
    contactList: core.sortedContactList,
    accounts: core.accounts,
    doShowAddContactView: mainUI.doShowAddContactView,
    doShowSessionWizard: core.doShowSessionWizard,
    doChangeActiveAccount: core.doChangeActiveAccount,
  }
}

@inject('core', 'mainUI')
@observer
class MainView extends React.Component {
  onClickContactAction = e => {
    const { jid } = e.target.parentNode.parentNode.dataset
    const { action } = e.target.parentNode.dataset
    if (action === 'rename') {
      this.props.mainUI.doShowRenameContactView(jid)
    } else if (action === 'delete') {
      this.props.core.doDeleteContact(jid)
    }
  }

  renderContactOps = (jid, displayName, presence, addition) => {
    return (
      <div data-jid={jid} className='main-contact-ops'>
        <button data-action='rename' onClick={this.onClickContactAction} className='btn btn-primary'>
          <Text message='action.rename' />
        </button>
        <button data-action='delete' onClick={this.onClickContactAction} className='btn btn-danger'>
          <Text message='action.delete' />
        </button>
      </div>
    )
  }

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
          <ContactList contactList={core.sortedContactList} renderOps={this.renderContactOps} />
        </div>
      </div>
    )
  }
}

export default MainView
