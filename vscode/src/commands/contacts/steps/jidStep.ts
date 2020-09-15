import {WizardStep, WizardContext} from '../../../types';
import {ContactDto} from '../../../lsp';
import {regex} from '../../../utils';

/**
 * Wizard step to enter a JID.
 *
 * @export
 * @class JidStep
 * @implements {WizardStep<ContactDto>}
 */
export class JidStep implements WizardStep<ContactDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof JidStep
   */
  canExecute(_context: WizardContext<ContactDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof JidStep
   */
  async execute(context: WizardContext<ContactDto>): Promise<void> {
    const id = await context.showInputBox({
      value: context.target.id || '',
      prompt: 'Enter name or JID',
      placeholder: undefined,
      password: false,
      validate: this._validateJid,
    });

    context.target.id = id;
  }

  /**
   * Validates input if it's a JID.
   *
   * @private
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof JidStep
   */
  private _validateJid(input: string): Promise<string|undefined> {
    const isValid = regex.jid.test(input) || regex.jidPrefix.test(input);
    const result = isValid ? undefined : 'Not a valid JID';

    return Promise.resolve(result);
  }
}
