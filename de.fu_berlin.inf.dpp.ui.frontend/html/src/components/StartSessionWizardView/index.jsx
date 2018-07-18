import React from 'react'
import { inject } from 'mobx-react'
import Wizard, { Step } from '../Wizard'
import ChooseFilesStep from './ChooseFilesStep'
import ChooseContactsStep from './ChooseContactsStep'

@inject('core', 'sessionUI')
export default class StartSessionWizardView extends React.Component {
  render () {
    const { sessionUI, core } = this.props
    return (
      <Wizard onFinish={sessionUI.submitSession} onClickCancel={core.doCloseSessionWizard}>
        <Step title='Choose Files' Component={ChooseFilesStep} />
        <Step title='Choose Contacts' Component={ChooseContactsStep} />
      </Wizard>
    )
  }
}
