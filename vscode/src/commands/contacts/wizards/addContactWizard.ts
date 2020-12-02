import {ContactDto, AddContactRequest, Saros} from '../../../lsp';
import {Wizard} from '../../../types';
import {JidStep, DomainStep, NicknameStep} from '../steps';
import {showMessage} from '../../../utils';

/**
 * Wizard to add a contact.
 *
 * @export
 * @param {Saros} saros The instance of Saros
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function addContactWizard(saros: Saros)
  : Promise<void> {
  const contact: ContactDto = {
    id: '',
    nickname: '',
  } as any;
  const wizard = new Wizard(contact, 'Add contact', [
    new JidStep(),
    new DomainStep(),
    new NicknameStep(),
  ]);
  await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await saros.client.sendRequest(AddContactRequest.type, contact);
    showMessage(result, 'Contact added successfully!');
  }
}
