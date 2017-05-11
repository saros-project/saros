import React from 'react'
import { Text } from 'react-localize'
import { observer } from 'mobx-react'
import { withStore } from 'Utils'
import ContactList from './ContactList'
import Accounts from './Accounts'
import SarosStore from '../../stores/SarosStore'
import P from 'prop-types'

const DebugViewProps = {
  store: P.instanceOf(SarosStore)
}
const DebugView = ({ store }) => (
  <div>
    <h4>State</h4>
    <pre>
      {JSON.stringify(store.state, null, 2)}
    </pre>
    <h4>Accounts</h4>
    <pre>
      {JSON.stringify(store.accounts, null, 2)}
    </pre>
  </div>
)

DebugView.propTypes = DebugViewProps

const MainViewProps = {
  store: P.instanceOf(SarosStore)
}

const MainView = withStore(observer(
({
  store,
  store: {
    state: { activeAccount, contactList },
    accounts,
    doAddContact,
    doShowStartSessionWizard,
    doChangeActiveAccount
  }
}) => (
  <div>
    <nav className='navbar navbar-default'>
      <Accounts
        onChangeActiveAccount={doChangeActiveAccount}
        activeAccount={activeAccount}
        accounts={accounts}
      />
      <div className='btn-list'>
        <button type='button' onClick={doAddContact} className='btn btn-default btn-sm add'>
          <Text message='action.addContact' />
        </button>
        <button type='button' onClick={doShowStartSessionWizard} className='btn btn-default btn-sm'>
          <Text message='action.startSession' />
        </button>
      </div>
    </nav>
    <div className='content-container'>
      <ContactList contactList={contactList} />
    </div>
    <DebugView store={store} />
  </div>
)))

MainView.propTypes = MainViewProps

export default MainView
