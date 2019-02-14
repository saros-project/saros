import { inject, observer } from 'mobx-react'
import { views } from 'Constants'
import $ from 'jquery'
import AddContactView from './AddContactView'
import BasicWidgetTestView from './BasicWidgetTestView'
import ConfigurationWizardView from './ConfigurationWizardView'
import MainView from './MainView'
import React from 'react'
import StartSessionWizardView from './StartSessionWizardView'

const viewComponents = {
  [views.MAIN]: MainView,
  [views.ADD_CONTACT]: AddContactView,
  [views.START_SESSION_WIZARD]: StartSessionWizardView,
  [views.CONFIGURATION_WIZARD]: ConfigurationWizardView,
  [views.BASIC_WIDGET_TEST]: BasicWidgetTestView
}

@inject('view')
@observer
class App extends React.Component {
  render () {
    window.$ = $
    const View = viewComponents[this.props.view.currentView]
    if (!View) {
      return null
    }
    return <View />
  }
}

export default App
