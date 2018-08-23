import React from 'react'
import { inject, observer } from 'mobx-react'
import { views } from 'Constants'
import MainView from './MainView'
import AddContactView from './AddContactView'
import StartSessionWizardView from './StartSessionWizardView'
import ConfigurationWizardView from './ConfigurationWizardView'

const viewComponents = {
  [views.MAIN]: MainView,
  [views.ADD_CONTACT]: AddContactView,
  [views.START_SESSION_WIZARD]: StartSessionWizardView,
  [views.CONFIGURATION_WIZARD]: ConfigurationWizardView
}

@inject('view')
@observer
class App extends React.Component {
  render () {
    const View = viewComponents[this.props.view.currentView]
    if (!View) {
      return null
    }
    return <View />
  }
}

export default App
