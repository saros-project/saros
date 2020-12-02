import {
  AccountDto,
  SetActiveAccountRequest,
  Saros,
  events,
} from '../../../lsp';
import {Wizard} from '../../../types';
import {showMessage} from '../../../utils';
import {AccountListStep} from '../steps';
import * as _ from 'lodash';

/**
 * Wizard to select the default account.
 *
 * @export
 * @param {Saros} saros The instance of Saros
 * @return {Promise<void>} An awaitable promise that returns
 *  once wizard finishes or aborts
 */
export async function defaultAccountWizard(saros: Saros)
  : Promise<void> {
  const wizard =
    new Wizard<AccountDto|undefined>(undefined, 'Set active account', [
      new AccountListStep(saros),
    ]);
  const account = await wizard.execute();

  if (!wizard.aborted && account) {
    const result =
      await saros.client.sendRequest(SetActiveAccountRequest.type,
          account);
    showMessage(result, 'Active account set successfully!');

    if (result.success) {
      saros.publish(events.ActiveAccountChanged, account);
    }
  }
}
