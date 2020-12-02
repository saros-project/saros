import {
  AccountDto,
  RemoveAccountRequest,
  Saros,
  events,
} from '../../../lsp';
import {Wizard} from '../../../types';
import {showMessage} from '../../../utils';
import {AccountListStep} from '../steps';
import * as _ from 'lodash';

/**
 * Wizard to remove an account.
 *
 * @export
 * @param {Saros} saros The instance of Saros
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function removeAccountWizard(saros: Saros)
  : Promise<void> {
  const wizard = new Wizard<AccountDto>({} as any, 'Remove account', [
    new AccountListStep(saros),
  ]);
  const account = await wizard.execute();

  if (!wizard.aborted) {
    const result =
      await saros.client.sendRequest(RemoveAccountRequest.type, account);
    showMessage(result, 'Account removed successfully!');

    saros.publish(events.AccountRemoved, account);
  }
}
