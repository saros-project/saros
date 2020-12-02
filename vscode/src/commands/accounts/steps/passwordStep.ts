import {WizardStepBase, WizardContext} from '../../../types';
import {AccountDto} from '../../../lsp';

/**
 * Wizard step to enter a password.
 *
 * @export
 * @class PasswordStep
 * @extends {WizardStepBase<AccountDto>}
 */
export class PasswordStep extends WizardStepBase<AccountDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof PasswordStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof PasswordStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const password = await context.showInputBox({
      value: context.target.password || '',
      prompt: 'Enter password',
      placeholder: undefined,
      password: true,
      validate: this.notEmpty,
    });

    context.target.password = password;
  }
}
