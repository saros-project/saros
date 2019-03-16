import { Button, Col, FormLabel, DropdownButton, Form, FormControl, FormGroup, Container, Dropdown, ProgressBar, Row, SplitButton } from 'react-bootstrap'
import { action, observable } from 'mobx'
import { inject, observer } from 'mobx-react'
import React from 'react'

export default
@inject('mainUI')
@observer
class BasicWidgetTestView extends React.Component {
  componentDidMount () {
    window.view = this
  }

  @observable fields = {
    text: '',
    email: '',
    password: '',
    checkbox1: '',
    checkbox2: '',
    checkbox3: '',
    radioGroup: '1',
    select: 'option1', // Default value
    multiSelect: [],
    progressBar: 50
  }

  @action setFieldValue = (field, value) => {
    this.fields[field] = value
  }

  @action getFieldValue = (field) => {
    return this.fields[field]
  }

  @action onChangeField = (e) => {
    var name = e.target.name
    var value = e.target.value

    if (e.target.type === 'checkbox') { // handle checkboxes
      value = e.target.checked
    }
    if (e.target.multiple === true) { // handle multi selects
      value = []
      for (var i = 0; i < e.target.options.length; i++) {
        if (e.target.options[i].selected) {
          value.push(e.target.options[i].value)
        }
      }
    }

    this.setFieldValue(name, value)
  }

  @action onClickButton = (e) => {
    var value
    if (e.target === undefined) {
      value = e // key
    } else {
      value = e.target.id
    }
    document.querySelector('#button-display-text').innerHtml = value
    window.console.log(value)
  }

  render () {
    return (
      <div style={{margin: 20}} id='basic-widget-test-root'>
        <Container>
          <Row>
            <Col sm={12}>
              <Form>
                <FormGroup as={Row} controlId='text'>
                  <Col as={FormLabel} sm={2}>
                   Text
                  </Col>
                  <Col sm={10}>
                    <FormControl type='text' name='text' onChange={this.onChangeField} value={this.fields.text} />
                  </Col>
                </FormGroup>

                <FormGroup as={Row} controlId='email'>
                  <Col as={FormLabel} sm={2}>
                    Email
                  </Col>
                  <Col sm={10}>
                    <FormControl type='email' name='email' onChange={this.onChangeField} value={this.fields.email} />
                  </Col>
                </FormGroup>

                <FormGroup as={Row} controlId='password'>
                  <Col as={FormLabel} sm={2}>
                    Password
                  </Col>
                  <Col sm={10}>
                    <FormControl type='password' name='password' onChange={this.onChangeField} value={this.fields.password} />
                  </Col>
                </FormGroup>

                <FormGroup as={Row}>
                  <Col as={FormLabel} sm={2}>
                    Checkbox
                  </Col>
                  <Col sm={10}>
                    <Form.Check type="checkbox" inline name='checkbox1' value='1' onChange={this.onChangeField} checked={this.fields.checkbox1} label="1" />
                    <Form.Check type="checkbox" inline name='checkbox2' value='2' onChange={this.onChangeField} checked={this.fields.checkbox2} label="2" />
                    <Form.Check type="checkbox" inline name='checkbox3' value='3' onChange={this.onChangeField} checked={this.fields.checkbox3} label="3" />
                  </Col>
                </FormGroup>

                <FormGroup as={Row}>
                  <Col as={FormLabel} sm={2}>
                    Radio
                  </Col>
                  <Col sm={10}>
                    <Form.Check type="radio" inline name='radioGroup' value='1' onChange={this.onChangeField} checked={this.fields.radioGroup === '1'} label="1" />
                    <Form.Check type="radio" inline name='radioGroup' value='2' onChange={this.onChangeField} checked={this.fields.radioGroup === '2'} label="2" />
                    <Form.Check type="radio" inline name='radioGroup' value='3' onChange={this.onChangeField} checked={this.fields.radioGroup === '3'} label="3" />
                  </Col>
                </FormGroup>

                <FormGroup as={Row} controlId='select'>
                  <Col as={FormLabel} sm={2}>
                    Select
                  </Col>
                  <Col sm={10}>
                    <FormControl as='select' name='select' onChange={this.onChangeField} value={this.fields.select}>
                      <option value='option1'>Option1</option>
                      <option value='option2'>Option2</option>
                      <option value='option3'>Option3</option>
                    </FormControl>
                  </Col>
                </FormGroup>

                <FormGroup as={Row} controlId='multiSelect'>
                  <Col as={FormLabel} sm={2}>
                    MultiSelect
                  </Col>
                  <Col sm={10}>
                    <FormControl as='select' multiple name='multiSelect' onChange={this.onChangeField} value={this.fields.multiSelect}>
                      <option value='option1'>MultiOption1</option>
                      <option value='option2'>MultiOption2</option>
                      <option value='option3'>MultiOption3</option>
                    </FormControl>
                  </Col>
                </FormGroup>

                <FormGroup as={Row} controlId='progressBar'>
                  <Col as={FormLabel} sm={2}>
                    ProgressBar
                  </Col>
                  <Col sm={10}>
                    <ProgressBar now={this.fields.progressBar} name='progressBar' />
                  </Col>
                </FormGroup>

                <FormGroup as={Row}>
                  <Col sm={10}>
                    <div>
                      <span>Pressed Button: </span><span id='button-display-text'>none</span>
                    </div>
                    <div className='btn-toolbar'>
                      <Button type='button' id='button' onClick={this.onClickButton} >Button</Button>
                      <DropdownButton title='DropdownButton' id='button-dropdown'>
                        <Dropdown.Item eventKey='key-dropdown-1' id='button-dropdown-1' onSelect={this.onClickButton}>Dropdown link 1</Dropdown.Item>
                        <Dropdown.Item eventKey='key-dropdown-2' id='button-dropdown-2' onSelect={this.onClickButton}>Dropdown link 2</Dropdown.Item>
                        <Dropdown.Item eventKey='key-dropdown-3' id='button-dropdown-3' onSelect={this.onClickButton}>Dropdown link 3</Dropdown.Item>
                      </DropdownButton>
                      <SplitButton title='SplitButton' id='split-button' onClick={this.onClickButton}>
                        <Dropdown.Item eventKey='key-split-1' id='split-button-1' onSelect={this.onClickButton}>SplitButton link 1</Dropdown.Item>
                        <Dropdown.Item eventKey='key-split-2' id='split-button-2' onSelect={this.onClickButton}>SplitButton link 2</Dropdown.Item>
                        <Dropdown.Item eventKey='key-split-3' id='split-button-3' onSelect={this.onClickButton}>SplitButton link 3</Dropdown.Item>
                      </SplitButton>
                    </div>
                  </Col>
                </FormGroup>
              </Form>
            </Col>
          </Row>
        </Container>
      </div>
    )
  }
}
