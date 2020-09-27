import {WizardStep, WizardContext, QuickPickItem} from '../../../types';
import {ContactDto, GetAllContactRequest, Saros} from '../../../lsp';
import {mapToQuickPickItems} from '../../../utils';
import * as _ from 'lodash';

/**
 * Wizard step to select a contact.
 *
 * @export
 * @class ContactListStep
 * @implements {WizardStep<ContactDto>}
 */
export class ContactListStep implements WizardStep<ContactDto> {
  /**
   * Creates an instance of ContactListStep.
   *
   * @param {Saros} _saros The instance of Saros
   * @memberof ContactListStep
   */
  public constructor(private _saros: Saros) {}

  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof ContactListStep
   */
  canExecute(_context: WizardContext<ContactDto>): boolean {
    return !_context.target || !_context.target.id;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof ContactListStep
   */
  async execute(context: WizardContext<ContactDto>): Promise<void> {
    const contacts =
      await this._saros.client.sendRequest(GetAllContactRequest.type, null);
    const pick = await context.showQuickPick({
      items: mapToQuickPickItems(contacts.result,
          (c) => c.nickname, (c) => c.id),
      activeItem: undefined,
      placeholder: 'Select contact',
      buttons: undefined,
    }) as QuickPickItem<ContactDto>;

    context.target = pick.item;
  }
}
