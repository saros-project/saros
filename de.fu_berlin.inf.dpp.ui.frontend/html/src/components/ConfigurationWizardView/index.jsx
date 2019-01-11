import './style.css'
import { inject } from 'mobx-react'
import AccountStep from './AccountStep'
import ColorStep from './ColorStep'
import GeneralSettingsStep from './GeneralSettingsStep'
import React from 'react'
import ReviewStep from './ReviewStep'
import Wizard, { Step } from '../Wizard'

const noop = () => {}

const configurationDict = {
  account: {
    title: 'Enter Jabber Identifier',
    description: 'In order to use Saros you need to configure a XMPP account'
  },
  generalSettings: {
    title: 'General Settings',
    description: 'Configure your settings for use with Saros'
  },
  color: {
    title: 'Favorite Session Color',
    description: 'Please choose your favorite color that should be used during a Saros session'
  },
  review: {
    title: 'Configuration Complete',
    description: 'Please click finish to complete the Saros configuration'
  }
}

@inject('configurationUI')
export default class ConfigurationWizardView extends React.Component {
  render () {
    return (
      <Wizard onFinish={noop}>
        <Step
          title={configurationDict.account.title}
          description={configurationDict.account.description}
          Component={AccountStep}
        />
        <Step
          title={configurationDict.generalSettings.title}
          description={configurationDict.generalSettings.description}
          Component={GeneralSettingsStep}
        />
        <Step
          title={configurationDict.color.title}
          description={configurationDict.color.description}
          Component={ColorStep}
        />
        <Step
          title={configurationDict.review.title}
          description={configurationDict.review.description}
          Component={ReviewStep}
        />
      </Wizard>
    )
  }
}
