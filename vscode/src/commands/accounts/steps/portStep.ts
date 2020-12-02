import {WizardStep, WizardContext} from '../../../types';
import {AccountDto} from '../../../lsp';

/**
 * Wizard step to enter a port.
 *
 * @export
 * @class PortStep
 * @implements {WizardStep<AccountDto>}
 */
export class PortStep implements WizardStep<AccountDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof PortStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return !!_context.target.server;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof PortStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const port = await context.showInputBox({
      value: context.target.port.toString(),
      prompt: 'Enter port',
      placeholder: 'optional',
      password: false,
      validate: this._isNumber,
    });

    context.target.port = +port;
  }

  /**
   * Validates input if it's a number.
   *
   * @private
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof PortStep
   */
  private _isNumber(input: string): Promise<string|undefined> {
    return Promise.resolve(+input > 0 ?
      '' : 'port has to be a number greater than 0');
  }
}
