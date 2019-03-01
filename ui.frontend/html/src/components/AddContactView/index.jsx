import { Text } from 'react-localize'
import { action, observable } from 'mobx'
import { inject, observer } from 'mobx-react'
import React from 'react'

@inject('mainUI')
@observer
export default class AddContactView extends React.Component {
  @observable fields = {
    jid: '',
    displayName: ''
  }

  @action onChangeField = (e) => {
    this.setFieldValue(e.target.name, e.target.value)
  }

  @action onClickSubmit = () => {
    this.props.mainUI.doSubmitAddContact(this.fields.jid, this.fields.displayName)
  }

  @action setFieldValue = (field, value) => {
    this.fields[field] = value
  }

  componentDidMount () {
    // Expose this view globally to be accessible for Java
    window.view = this
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
            <button id='cancel-add-contact' onClick={this.props.mainUI.doCancelAddContact} className='cancel-add-contact-btn btn btn-default'>
              <Text message='action.cancel' />
            </button>
            <button id='add-contact' onClick={this.onClickSubmit} className='submit-add-contact btn btn-primary'>
              <Text message='action.addContact' />
            </button>
          </div>
        </div>
      </div>
    )
  }
}
