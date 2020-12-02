import * as vscode from 'vscode';

/**
 * Typed QuickPickItem version of vscode.
 *
 * @export
 * @class QuickPickItem
 * @implements {vscode.QuickPickItem}
 * @template T
 */
export class QuickPickItem<T> implements vscode.QuickPickItem {
    label: string;
    description?: string | undefined;
    detail?: string | undefined;
    picked?: boolean | undefined;
    alwaysShow?: boolean | undefined;
    item: T;

    /**
     * Creates an instance of QuickPickItem.
     *
     * @param {string} label Label of the item
     * @param {T} item Object the item wrappes
     * @memberof QuickPickItem
     */
    constructor(label: string, item: T) {
      this.label = label;
      this.item = item;
    }
}
