import React from 'react'
import { inject } from 'mobx-react'
import Wizard, { Step } from '../Wizard'
import ChooseFilesStep from './ChooseFilesStep'
import ChooseContactsStep from './ChooseContactsStep'

@inject('sessionUI')
export default class StartSessionWizardView extends React.Component {
  render () {
    return (
      <Wizard onFinish={this.props.sessionUI.submitSession}>
        <Step title='Choose Files' Component={ChooseFilesStep} />
        <Step title='Choose Contacts' Component={ChooseContactsStep} />
      </Wizard>
    )
  }
}
