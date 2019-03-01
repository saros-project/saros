import { Text } from 'react-localize'
import React from 'react'

class ActiveAccount extends React.Component {
  render () {
    const { activeAccount } = this.props
    let accountText
    let className

    if (activeAccount && activeAccount.username && activeAccount.domain) {
      accountText = <span>{ activeAccount.username + '@' + activeAccount.domain }</span>
      className = 'bg-primary text-white'
    } else {
      accountText = <Text message='message.noAccount' />
      className = 'bg-info'
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

export default ActiveAccount
