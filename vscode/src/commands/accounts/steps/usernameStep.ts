import {WizardContext, WizardStepBase} from '../../../types';
import {AccountDto} from '../../../lsp';
import {regex} from '../../../utils';

/**
 * Wizard step to enter a username.
 *
 * @export
 * @class UsernameStep
 * @extends {WizardStepBase<AccountDto>}
 */
export class UsernameStep extends WizardStepBase<AccountDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof UsernameStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof UsernameStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const username = await context.showInputBox({
      value: context.target.username || '',
      prompt: 'Enter username',
      placeholder: undefined,
      password: false,
      validate: this._validateUsername,
    });

    context.target.username = username;
  }

  /**
   * Validates input if it's a username.
   *
   * @private
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof UsernameStep
   */
  private _validateUsername(input: string): Promise<string|undefined> {
    const isValid = regex.jidPrefix.test(input);
    const result = isValid ? undefined : 'Not a valid username';

    return Promise.resolve(result);
  }
}
