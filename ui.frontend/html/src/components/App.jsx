import { inject, observer } from 'mobx-react'
import { views } from 'Constants'
import $ from 'jquery'
import AddContactView from './AddContactView'
import BasicWidgetTestView from './BasicWidgetTestView'
import ConfigurationView from './ConfigurationView'
import MainView from './MainView'
import React from 'react'
import ShareProjectView from './ShareProjectView'

const viewComponents = {
  [views.MAIN]: MainView,
  [views.ADD_CONTACT_PAGE]: AddContactView,
  [views.SHARE_PROJECT_PAGE]: ShareProjectView,
  [views.CONFIGURATION_PAGE]: ConfigurationView,
  [views.BASIC_WIDGET_TEST]: BasicWidgetTestView
}


export default
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
