import {ContactDto, RemoveContactRequest, Saros} from '../../../lsp';
import {Wizard} from '../../../types';
import {ContactListStep} from '../steps';
import {showMessage} from '../../../utils';

/**
 * Wizard to remove a contact.
 *
 * @export
 * @param {ContactDto} contact The contact to remove or undefined
 * @param {Saros} saros The instance of Saros
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function removeContactWizard(contact: ContactDto,
    saros: Saros): Promise<void> {
  const wizard = new Wizard(contact, 'Remove contact', [
    new ContactListStep(saros),
  ]);
  contact = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await saros.client.sendRequest(RemoveContactRequest.type, contact);
    showMessage(result, 'Contact removed successfully!');
  }
}
