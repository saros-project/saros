import { action, observable } from 'mobx'
import { inject, observer } from 'mobx-react'
import React from 'react'
import { Text } from 'react-localize'
import cn from 'classnames'
import invariant from 'invariant'

@inject('core', 'mainUI')
@observer
export default class AddContactView extends React.Component {
  @observable fields = {
    jid: '',
    displayName: '',
  }

  constructor (props) {
    super()
    if (props.intent.rename) {
      const { jid } = props.intent
      const contact = props.core.contactIndex.get(jid)
      invariant(contact, `Cannot rename a contact that does not exist`)
      this.fields.jid = jid
      this.fields.displayName = contact.displayName
    }
  }

  @action onChangeField = (e) => {
    this.setFieldValue(e.target.name, e.target.value)
  }

  @action onClickSubmit = () => {
    const { mainUI, intent } = this.props
    mainUI.doSubmitAddContact(this.fields.jid, this.fields.displayName, intent.rename)
  }

  @action setFieldValue = (field, value) => {
    this.fields[field] = value
  }

  componentDidMount () {
    // Expose this view globally to be accessible for Java
    window.view = this
  }

  render () {
    const { rename } = this.props.intent
    return (
      <div className='form-horizontal' id='add-contact-form'>
        <div className='form-group'>
          <label className='col-sm-2 control-label'>
            <Text message='label.jid' />
          </label>
          <div className='col-sm-10'>
            <input
              autoFocus={!rename}
              className={cn({ disabled: rename })}
              disabled={rename}
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
            <input
              autoFocus={rename}
              type='text'
              value={this.fields.displayName}
              onChange={this.onChangeField}
              name='displayName'
            />
          </div>
        </div>
        <div className='form-group'>
          <div className='col-sm-offset-2 col-sm-10'>
            <button id='cancel-add-contact' onClick={this.props.mainUI.doCancelAddContact} className='cancel-add-contact-btn btn btn-default'>
              <Text message='action.cancel' />
            </button>
            <button id='add-contact' onClick={this.onClickSubmit} className='submit-add-contact btn btn-primary'>
              <Text message={rename ? 'action.renameContact' : 'action.addContact'} />
            </button>
          </div>
        </div>
      </div>
    )
  }
}
