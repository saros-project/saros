import React from 'react'
import $ from 'jquery'
import { inject, observer } from 'mobx-react'
import { views } from 'Constants'
import MainView from './MainView'
import AddContactView from './AddContactView'
import StartSessionWizardView from './StartSessionWizardView'
import BasicWidgetTestView from './BasicWidgetTestView'

const viewComponents = {
  [views.MAIN]: MainView,
  [views.ADD_CONTACT]: AddContactView,
  [views.START_SESSION_WIZARD]: StartSessionWizardView,
  [views.BASIC_WIDGET_TEST]: BasicWidgetTestView,
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
