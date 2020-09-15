import {QuickPickItem} from '../types';
import {window} from 'vscode';
import {SarosResponse} from '../lsp';

export type TextFunc<T> = (item: T) => string;

/**
 * Creates quickpick items that wrap the pickable objects.
 *
 * @export
 * @template T
 * @param {T[]} items The pickable items
 * @param {TextFunc<T>} labelFunc Function that extracts the label
 * @param {TextFunc<T>} [detailFunc] Function that extracts the details
 * @return {QuickPickItem<T>[]} Pickable quickpick items
 */
export function mapToQuickPickItems<T>(
    items: T[], labelFunc: TextFunc<T>, detailFunc?: TextFunc<T>,
): QuickPickItem<T>[] {
  return items.map((item) => {
    return {
      label: labelFunc(item),
      detail: detailFunc ? detailFunc(item) : undefined,
      item: item,
    };
  });
}

/**
 * Shows a success or error message depending on the response.
 *
 * @export
 * @param {SarosResponse} response The response
 * @param {string} successMessage The message to be shown on success
 * @param {string} [errorMessage] The message to be shown on failure
 */
export function showMessage(
    response: SarosResponse, successMessage: string, errorMessage?: string,
) {
  if (response.success) {
    window.showInformationMessage(successMessage);
  } else {
    window.showErrorMessage(response.error || errorMessage || 'Unknown Error');
  }
}
