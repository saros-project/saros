
import {Saros} from '../../lsp';
import {commands} from 'vscode';
import {
  addAccountWizard,
  editAccountWizard,
  removeAccountWizard,
  defaultAccountWizard,
} from './wizards';

/**
 * Registers all commands of the account module.
 *
 * @export
 * @param {Saros} saros The instance of Saros
 */
export function activateAccounts(saros: Saros) {
  saros.context.subscriptions.push(
      commands.registerCommand('saros.account.add', async () => {
        await saros.onReady();
        return addAccountWizard(saros);
      }),

      commands.registerCommand('saros.account.update', async () => {
        await saros.onReady();
        return editAccountWizard(saros);
      }),

      commands.registerCommand('saros.account.remove', async () => {
        await saros.onReady();
        return removeAccountWizard(saros);
      }),

      commands.registerCommand('saros.account.setActive', async () => {
        await saros.onReady();
        return defaultAccountWizard(saros);
      }),
  );
}
