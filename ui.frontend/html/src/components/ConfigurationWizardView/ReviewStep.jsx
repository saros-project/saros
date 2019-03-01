import { firstCharToLowerCase } from 'Utils'
import { inject, observer } from 'mobx-react'
import React from 'react'

@inject('configurationUI')
@observer
export default class ReviewStep extends React.Component {
  settingLabels = {
    autoConnect: 'Connect automatically',
    isSkypeUsernameVisible: 'Show my Skype username',
    allowSubmitAnalytics: 'Send anonymous statistical data',
    allowSubmitCrashReports: 'Send crash reports'
  }

  getLabel = (key) => {
    const { settings } = this.props.configurationUI.data
    if (settings[key]) {
      return this.settingLabels[key]
    }
    return `Do not ${firstCharToLowerCase(this.settingLabels[key])}`
  }

  render () {
    return (
      <div className='container review-step'>
        <div className='row settings-container'>
          <div className='col-sm-12'>
            { Object.keys(this.settingLabels).map(key => (
              <div className='row setting-review'>
                <div className='col-sm-12'>
                  {this.getLabel(key)}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }
}
