import {Disposable, window, StatusBarItem, StatusBarAlignment} from 'vscode';
import {
  Saros,
  GetAllAccountRequest,
  AccountDto,
  events,
  SarosClient,
} from '../lsp';

/**
 * View that displays the currently selected account.
 *
 * @export
 * @class SarosAccountView
 * @implements {Disposable}
 */
export class SarosAccountView implements Disposable {
    private _statusBarItem: StatusBarItem;
    private _sarosClient: SarosClient;

    /**
     * Creates an instance of SarosAccountView.
     *
     * @param {Saros} extension
     * @memberof SarosAccountView
     */
    constructor(extension: Saros) {
      this._sarosClient = extension.client;
      this._statusBarItem = window.createStatusBarItem(StatusBarAlignment.Left);

      extension.subscribe(events.ActiveAccountChanged,
          () => this._refreshAccount());
      extension.subscribe(events.AccountRemoved, () => this._refreshAccount());

      this._statusBarItem.command = 'saros.account.setActive';
      this._setAccount(undefined);

      extension.client.onReady().then(() => {
        this._statusBarItem.show();

        extension.client.sendRequest(GetAllAccountRequest.type, null)
            .then((result) => {
              const accounts = result.result;
              const defaultAccount = accounts.find(
                  (account) => account.isDefault,
              );
              this._setAccount(defaultAccount);
            });
      });
    }

    /**
     * Refreshes the currently displayed account.
     *
     * @private
     * @memberof SarosAccountView
     */
    private _refreshAccount() {
      this._sarosClient.sendRequest(GetAllAccountRequest.type, null)
          .then((result) => {
            const accounts = result.result;
            const defaultAccount = accounts.find(
                (account) => account.isDefault,
            );
            this._setAccount(defaultAccount);
          });
    }

    /**
     * Sets the currently displayed account.
     *
     * @private
     * @param {(AccountDto | undefined)} account Account to display
     * @memberof SarosAccountView
     */
    private _setAccount(account: AccountDto | undefined) {
      this._statusBarItem.text =
        `$(account) Saros: ${account?.username || 'n/A'}`;
      this._statusBarItem.tooltip =
        `Domain: ${account?.domain} (click to change)`;
    }

    /**
     * Disposes all disposable resources.
     *
     * @memberof SarosAccountView
     */
    dispose() {
      this._statusBarItem.dispose();
      this._statusBarItem.hide();
    }
}
