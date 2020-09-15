import {AccountDto, UpdateAccountRequest, Saros} from '../../../lsp';
import {Wizard} from '../../../types';
import {showMessage} from '../../../utils';
import {
  UsernameStep,
  DomainStep,
  PasswordStep,
  ServerStep,
  PortStep,
  TlsStep,
  SaslStep,
  AccountListStep,
} from '../steps';
import * as _ from 'lodash';

/**
 * Wizard to edit an account.
 *
 * @export
 * @param {Saros} saros The instance of Saros
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function editAccountWizard(saros: Saros)
  : Promise<void> {
  const wizard = new Wizard<AccountDto>({} as any, 'Edit account', [
    new AccountListStep(saros),
    new UsernameStep(),
    new DomainStep(),
    new PasswordStep(),
    new ServerStep(),
    new PortStep(),
    new TlsStep(),
    new SaslStep(),
  ]);
  const account = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await saros.client.sendRequest(UpdateAccountRequest.type, account);
    showMessage(result, 'Account updated successfully!');
  }
}
