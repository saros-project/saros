import {WizardStep, WizardContext} from '../../../types';
import {AccountDto, config} from '../../../lsp';
import {regex} from '../../../utils';

/**
 * Wizard step to enter a domain.
 *
 * @export
 * @class DomainStep
 * @implements {WizardStep<AccountDto>}
 */
export class DomainStep implements WizardStep<AccountDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof DomainStep
   */
  canExecute(_context: WizardContext<AccountDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof DomainStep
   */
  async execute(context: WizardContext<AccountDto>): Promise<void> {
    const domain = await context.showInputBox({
      value: context.target.domain || config.getDefaultHost() || '',
      prompt: 'Enter domain',
      placeholder: undefined,
      password: false,
      validate: this._validateDomain,
    });

    context.target.domain = domain;
  }

  /**
   * Validates an input if it's a valid domain.
   *
   * @private
   * @param {string} input The input to validate
   * @return {(Promise<string|undefined>)} Awaitable result which is undefined
   *  if valid and contains the error message if not
   * @memberof DomainStep
   */
  private _validateDomain(input: string): Promise<string|undefined> {
    const isValid = regex.jidSuffix.test(input);
    const result = isValid ? undefined : 'Not a valid domain';

    return Promise.resolve(result);
  }
}
