import { Account } from 'Utils/propTypes'
import { DropdownButton, MenuItem } from 'react-bootstrap'
import { PropTypes as PM } from 'mobx-react'
import { Text } from 'react-localize'
import { getJid } from 'Utils'
import P from 'prop-types'
import React from 'react'

const AccountsProps = {
  activeAccount: Account.isRequired,
  accounts: PM.observableArrayOf(Account).isRequired,
  onChangeActiveAccount: P.func.isRequired
}

const Accounts = ({
  activeAccount,
  accounts,
  onChangeActiveAccount
}) => (
  <div>
    { (typeof activeAccount === 'object' && activeAccount.username && activeAccount.domain)
      ? `${activeAccount.username}@${activeAccount.domain}`
      : <Text message='message.noAccount' />
    }
    <DropdownButton
      id='accounts'
      onSelect={onChangeActiveAccount}
      bsStyle={'primary'}
      title='Accounts'
    >
      {accounts.map(getJid).map(jid => (
        <MenuItem
          key={jid}
          eventKey={jid}
        >
          {jid}
        </MenuItem>
      ))}
    </DropdownButton>
  </div>
)

Accounts.propTypes = AccountsProps

export default Accounts
