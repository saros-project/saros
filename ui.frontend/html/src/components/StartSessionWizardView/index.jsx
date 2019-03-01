import { inject } from 'mobx-react'
import ChooseContactsStep from './ChooseContactsStep'
import ChooseFilesStep from './ChooseFilesStep'
import React from 'react'
import Wizard, { Step } from '../Wizard'

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
