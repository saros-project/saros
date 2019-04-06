import { Text } from 'react-localize'
import { action, observable } from 'mobx'
import { inject, observer } from 'mobx-react'
import React from 'react'
import { Form, Row, Col, Button, ButtonGroup } from 'react-bootstrap';
import Dictionary from '~/dictionary';
import './style.css'

export default
@inject('mainUI')
@observer
class AddContactView extends React.Component {
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

  componentDidMount() {
    // Expose this view globally to be accessible for Java
    window.view = this
  }

  render() {
    return (
      <Row id='add-contact-form'>
        <Col sm={12} md={{offset: 1, span: 10}} lg={{ offset: 2, span: 8 }}>
          <Form>
            <Form.Group>
              <Form.Label><Text message='label.jid' /></Form.Label>
              <Form.Control autoFocus type='text' placeholder={Dictionary.label.jid}
                value={this.fields.jid} onChange={this.onChangeField} name='jid' />
            </Form.Group>

            <Form.Group>
              <Form.Label><Text message='label.nickname' /></Form.Label>
              <Form.Control type='text' placeholder={Dictionary.label.nickname}
                value={this.fields.displayName} onChange={this.onChangeField} name='displayName' />
            </Form.Group>

            <Form.Group className="d-flex flex-column">
              <ButtonGroup >
                <Button variant='light' id='cancel-add-contact' onClick={this.props.mainUI.doCancelAddContact}>
                  <Text message='action.cancel' />
                </Button>
                <Button variant='primary' id='add-contact' onClick={this.onClickSubmit}>
                  <Text message='action.addContact' />
                </Button>
              </ButtonGroup>
            </Form.Group>
          </Form>
        </Col>
      </Row>
    )
  }
}
