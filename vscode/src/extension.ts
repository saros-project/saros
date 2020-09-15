import {
  activateAccounts,
  activateContacts,
} from './commands';
import {SarosContactView, SarosAccountView} from './views';
import {sarosInstance} from './lsp';
import {ExtensionContext, workspace, window} from 'vscode';
import {variables} from './utils/variables';
import {activateConnections} from './commands/connections/activator';

/**
 * Activates the extension.
 *
 * @export
 * @param {ExtensionContext} context - The extension context
 */
export function activate(context: ExtensionContext) {
  const activationConditionError = getActivationConditionError();
  if (activationConditionError) {
    window.showErrorMessage(activationConditionError);
    deactivate();
    return;
  }
  sarosInstance.setContext(context)
      .init()
      .then(() => {
        activateAccounts(sarosInstance);
        activateContacts(sarosInstance);
        activateConnections(sarosInstance);

        context.subscriptions
            .push(new SarosAccountView(sarosInstance));
        context.subscriptions
            .push(new SarosContactView(sarosInstance));

        variables.setInitialized(true);
      });
}

/**
 * Checks if extension is supported within the opened workspace.
 *
 * @return {(string|undefined)} undefined if extension can be activated
 *  and a reason if extension doesn't support the opened workspace.
 */
function getActivationConditionError(): string|undefined {
  if (workspace.workspaceFolders === undefined) {
    return 'Workspace is empty - Saros deactivated';
  } else if (workspace.workspaceFolders.length > 1) {
    return 'Multiple workspaces aren\'t currently supported' +
           ' - Saros deactivated';
  }
}

/**
 * Deactivates the extension.
 *
 * @export
 */
export function deactivate() {
  sarosInstance.deactivate();
  variables.setInitialized(false);
}
