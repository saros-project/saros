import {Saros} from '../../lsp';
import {commands} from 'vscode';
import {
  addContactWizard,
  editContactWizard,
  removeContactWizard,
} from './wizards';

/**
 * Registers all commands of the contact module.
 *
 * @export
 * @param {Saros} saros - The instance of Saros
 */
export function activateContacts(saros: Saros) {
  saros.context.subscriptions.push(
      commands.registerCommand('saros.contact.add', async () => {
        await saros.onReady();
        return addContactWizard(saros);
      }),

      commands.registerCommand('saros.contact.remove', async (contact) => {
        await saros.onReady();
        return removeContactWizard(contact, saros);
      }),

      commands.registerCommand('saros.contact.rename', async (contact) => {
        await saros.onReady();
        return editContactWizard(contact, saros);
      }),
  );
}
