import React from 'react'
import { observable, action } from 'mobx'
import { observer, inject } from 'mobx-react'
import { Text } from 'react-localize'

@inject('mainUI')
@observer
export default class AddContactView extends React.Component {
  @observable fields = {
    jid: '',
    displayName: ''
  }

  @action onChangeField = (e) => {
    this.fields[e.target.name] = e.target.value
  }

  @action onClickSubmit = () => {
    this.props.mainUI.doSubmitAddContact(this.fields.jid, this.fields.displayName)
  }

  render () {
    return (
      <div className='form-horizontal' id='add-contact-form'>
        <div className='form-group'>
          <label className='col-sm-2 control-label'>
            <Text message='label.jid' />
          </label>
          <div className='col-sm-10'>
            <input 
              autoFocus
              type='text'
              value={this.fields.jid}
              onChange={this.onChangeField}
              name='jid'
            />
          </div>
        </div>
        <div className='form-group'>
          <label className='col-sm-2 control-label'>
            <Text message='label.nickname' />
          </label>
          <div className='col-sm-10'>
            <input type='text' value={this.fields.displayName} onChange={this.onChangeField} name='displayName' />
          </div>
        </div>
        <div className='form-group'>
          <div className='col-sm-offset-2 col-sm-10'>
            <button onClick={this.onClickSubmit} className='btn btn-default'>
              <Text message='action.addContact' />
            </button>
          </div>
        </div>
      </div>
    )
  }
}
