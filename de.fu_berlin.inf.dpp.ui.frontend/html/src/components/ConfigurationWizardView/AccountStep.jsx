import { Text } from 'react-localize'
import { inject, observer } from 'mobx-react'
import React from 'react'

@inject('configurationUI')
@observer
export default class AccountStep extends React.Component {
  onChangeField = (e) => {
    this.props.configurationUI.changeWizardData('account', e.target.name, e.target.value)
  }

  render () {
    const { account } = this.props.configurationUI.data
    return (
      <div className='form-horizontal'>
        <div className='form-group'>
          <label className='col-sm-2 control-label'>
            <Text message='label.jid' />
          </label>
          <div className='col-sm-10'>
            <input autoFocus type='text' value={account.jid} onChange={this.onChangeField} name='jid' />
          </div>
        </div>
        <div className='form-group'>
          <label className='col-sm-2 control-label'>
            <Text message='label.password' />
          </label>
          <div className='col-sm-10'>
            <input type='password' value={account.password} onChange={this.onChangeField} name='password' />
          </div>
        </div>
      </div>
    )
  }
}
