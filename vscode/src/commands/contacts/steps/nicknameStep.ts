import {WizardContext, WizardStepBase} from '../../../types';
import {ContactDto} from '../../../lsp';
import {regex} from '../../../utils';

/**
 * Wizard step to enter a nickname.
 *
 * @export
 * @class NicknameStep
 * @extends {WizardStepBase<ContactDto>}
 */
export class NicknameStep extends WizardStepBase<ContactDto> {
  /**
   * Checks if step can be executed.
   *
   * @param {WizardContext<AccountDto>} _context Current wizard context
   * @return {boolean} true if step can be executed, false otherwise
   * @memberof NicknameStep
   */
  canExecute(_context: WizardContext<ContactDto>): boolean {
    return true;
  }

  /**
   * Executes the step.
   *
   * @param {WizardContext<AccountDto>} context Current wizard context
   * @return {Promise<void>} Awaitable promise with no result
   * @memberof NicknameStep
   */
  async execute(context: WizardContext<ContactDto>): Promise<void> {
    const nickname = await context.showInputBox({
      value: context.target.nickname ||
        this.getJidPrefix(context.target.id) || '',
      prompt: 'Enter nickname',
      placeholder: undefined,
      password: false,
      validate: this.notEmpty,
    });

    context.target.nickname = nickname;
  }

  /**
   * Strips the JID prefix of the id.
   *
   * @private
   * @param {string} jid The JID
   * @return {string} The prefix of the JID
   * @memberof NicknameStep
   */
  private getJidPrefix(jid: string): string {
    return jid.split(regex.jidPartDivider)[0];
  }
}
