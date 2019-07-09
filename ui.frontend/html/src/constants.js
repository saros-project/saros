// Here the different views are specified
const views = {
  MAIN: 'main-page',
  ADD_CONTACT_PAGE: 'add-contact-page',
  SHARE_PROJECT_PAGE: 'share-project-page',
  CONFIGURATION_PAGE: 'configuration-page',
  BASIC_WIDGET_TEST: 'basic-widget-test'
}

const connectionStates = {
  INITIALIZING: 'INITIALIZING',
  NOT_CONNECTED: 'NOT_CONNECTED',
  CONNECTED: 'CONNECTED',
  ERROR: 'ERROR',
  CONNECTING: 'CONNECTING',
  DISCONNECTING: 'DISCONNECTING'
}

module.exports = { views, connectionStates };
