import {WizardStep, WizardContext} from '../../../types';
import {AccountDto} from '../../../lsp';

const regexServer = /^[a-z0-9.-]+\.[a-z]{2,10}$/;

/**
 * Wizard step to enter a server.
 *
 * @export
 * @class ServerStep
 * @implements {WizardStep<AccountDto>}
 */
export class ServerStep implements WizardStep<AccountDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof ServerStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof ServerStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const server = await context.showInputBox({
      value: context.target.server || '',
      prompt: 'Enter server',
      placeholder: 'optional',
      password: false,
      validate: this._validateServer,
    });

    context.target.server = server;
  }

  /**
   * Validates input if it's a server address.
   *
   * @private
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof ServerStep
   */
  private _validateServer(input: string): Promise<string|undefined> {
    const isValid = !input || regexServer.test(input);
    const result = isValid ? undefined : 'Not a valid address';

    return Promise.resolve(result);
  }
}
