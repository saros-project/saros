import {WizardStep, WizardContext} from '../../../types';
import {ContactDto, config} from '../../../lsp';
import {regex} from '../../../utils';

/**
 * Wizard step to enter a domain.
 *
 * @export
 * @class DomainStep
 * @implements {WizardStep<ContactDto>}
 */
export class DomainStep implements WizardStep<ContactDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof DomainStep
   */
  canExecute(_context: WizardContext<ContactDto>): boolean {
    return !!_context.target.id && !regex.jid.test(_context.target.id);
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof DomainStep
   */
  async execute(context: WizardContext<ContactDto>): Promise<void> {
    const domain = await context.showInputBox({
      value: config.getDefaultHost() || '',
      prompt: 'Enter domain',
      placeholder: undefined,
      password: false,
      validate: this._validateHost,
    });

    context.target.id += `@${domain}`;
  }

  /**
   * Validates input if it's a domain.
   *
   * @private
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof DomainStep
   */
  private _validateHost(input: string): Promise<string|undefined> {
    const isValid = regex.jidSuffix.test(input);
    const result = isValid ? undefined : 'Not a valid host';

    return Promise.resolve(result);
  }
}
