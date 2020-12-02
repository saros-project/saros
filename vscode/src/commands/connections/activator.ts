import {commands} from 'vscode';
import {
  ConnectRequest,
  DisconnectRequest,
  ConnectionStateRequest,
  Saros,
} from '../../lsp';
import {showMessage} from '../../utils';

/**
 * Registers all commands of the connection module.
 *
 * @export
 * @param {SarosExtension} saros - The instance of Saros
 */
export function activateConnections(saros: Saros) {
  commands.registerCommand('saros.connection.connect', async () => {
    await saros.onReady();
    const result =
      await saros.client.sendRequest(ConnectRequest.type, null);
    showMessage(result, 'Connected successfully!', 'Couldn\'t connect.');
  });

  commands.registerCommand('saros.connection.disconnect', async () => {
    await saros.onReady();
    const result =
      await saros.client.sendRequest(DisconnectRequest.type, null);
    showMessage(result, 'Disconnected successfully!');
  });

  commands.registerCommand('saros.connection.status', async () => {
    await saros.onReady();
    const result =
      await saros.client.sendRequest(ConnectionStateRequest.type, null);
    showMessage(result,
      result.result ? 'Saros is connected!' : 'Saros is disconnected!');
  });
}
