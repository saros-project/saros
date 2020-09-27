import {WizardStep, WizardContext, QuickPickItem} from '../../../types';
import {AccountDto} from '../../../lsp';
import {mapToQuickPickItems} from '../../../utils';

/**
 * Wizard step to enter SASL.
 *
 * @export
 * @class SaslStep
 * @implements {WizardStep<AccountDto>}
 */
export class SaslStep implements WizardStep<AccountDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof SaslStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof SaslStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const items = [true, false];
    const pick = await context.showQuickPick({
      items: mapToQuickPickItems(items, (b) => b ? 'Yes' : 'No'),
      activeItem: undefined,
      placeholder: 'Use SASL?',
      buttons: undefined,
    }) as QuickPickItem<boolean>;

    context.target.useSASL = pick.item;
  }
}
