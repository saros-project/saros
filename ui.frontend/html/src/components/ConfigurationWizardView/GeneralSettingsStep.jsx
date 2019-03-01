import { inject, observer } from 'mobx-react'
import React from 'react'

const dict = {
  autoConnect: {
    description: 'Automatically connect to XMPP server on startup?',
    label: 'Connect automatically'
  },
  skype: {
    description: 'Shall your contacts see your Skype username?',
    label: 'Yes, use:'
  },
  gateway: {
    description: 'TODO'
  },
  analytics: {
    description: (
      <span>
        Saros is a research project of the <a href='#'>Software Engineering Group</a>
         at <a href='#'>Freie Universität Berlin</a>. We need your help to gather scientific data about distributed
        pair programming.
        We will not collect any personal data. <a href='#'>Tell me more</a>
      </span>
    ),
    label: 'Allow submission of anonymous statistical data'
  },
  crashReport: {
    label: 'Allow submission of crash reports'
  }
}

@inject('configurationUI')
@observer
export default class GeneralSettingsStep extends React.Component {
  onChangeField = (e) => {
    this.props.configurationUI.changeWizardData('settings', e.target.name, e.target.value)
  }

  onToggleCheckbox = (e) => {
    this.props.configurationUI.toggleWizardDataBool('settings', e.target.name)
  }

  render () {
    const { settings } = this.props.configurationUI.data
    return (
      <div className='container general-settings-step'>
        <div className='row'>
          <Column>
            <Section description={dict.skype.description}>
              <span className='input-group-addon'>
                <input
                  type='checkbox'
                  name='isSkypeUsernameVisible'
                  checked={settings.isSkypeUsernameVisible}
                  onChange={this.onToggleCheckbox}
                />
                <span>{dict.skype.label}</span>
              </span>
              <input
                type='text'
                className='form-control skype-username-input'
                name='skypeUsername'
                value={settings.skypeUsername}
                onChange={this.onChangeField} />
            </Section>
            <Section description={dict.autoConnect.description}>
              <input
                type='checkbox'
                name='autoConnect'
                checked={settings.autoConnect}
                onChange={this.onToggleCheckbox}
              />
              <span>{dict.autoConnect.label}</span>
            </Section>
            <Section description={dict.gateway.description}>
              Insert gateway setting
            </Section>
          </Column>
          <Column>
            <Section description={dict.analytics.description}>
              <span>{dict.analytics.label}</span>
              <input
                type='checkbox'
                name='allowSubmitAnalytics'
                checked={settings.allowSubmitAnalytics}
                onChange={this.onToggleCheckbox}
              />
            </Section>
            <Section>
              <span>{dict.crashReport.label}</span>
              <input
                type='checkbox'
                name='allowSubmitCrashReports'
                checked={settings.allowSubmitCrashReports}
                onChange={this.onToggleCheckbox}
              />
            </Section>
          </Column>
        </div>
      </div>
    )
  }
}

function Column ({ children }) {
  return (
    <div className='col-sm-6 settings-col'>
      <div className='settings-col-wrapper row'>
        <div className='col-sm-12'>
          {children}
        </div>
      </div>
    </div>
  )
}

function Section ({ description, children }) {
  return (
    <div className='row section'>
      { description &&
        <div className='col-sm-12'>
          {description}
        </div>
      }
      <div className='col-sm-12'>
        {children}
      </div>
    </div>
  )
}
