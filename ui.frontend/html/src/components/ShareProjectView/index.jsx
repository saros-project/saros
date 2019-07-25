import { inject } from 'mobx-react'
import ChooseContactsStep from './ChooseContactsStep'
import ChooseFilesStep from './ChooseFilesStep'
import React from 'react'
import Wizard, { Step } from '../Wizard'


export default
@inject('core', 'sessionUI')
class ShareProjectView extends React.Component {
  render () {
    const { sessionUI, core } = this.props
    return (
      <Wizard onFinish={sessionUI.submitSession} onClickCancel={core.doCloseShareProjectPage}>
        <Step title='Choose Files' Component={ChooseFilesStep} />
        <Step title='Choose Contacts' Component={ChooseContactsStep} />
      </Wizard>
    )
  }
}
