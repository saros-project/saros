import { Text } from 'react-localize'
import React from 'react'
import { getJid } from '../../utils';

export default class ActiveAccount extends React.Component {
  render () {
    const { activeAccount } = this.props
    let accountText
    let className = 'activeAccount '

    if (activeAccount && activeAccount.username && activeAccount.domain) {
      accountText = <span>{ getJid(activeAccount) }</span>
      className += 'bg-primary text-white'
    } else {
      accountText = <Text message='message.noAccount' />
      className += 'bg-info'
    }

    return (
      <div className={className}>
        <strong>
          { accountText }
        </strong>
      </div>
    )
  }
}
