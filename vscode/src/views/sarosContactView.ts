import {Disposable} from 'vscode-languageclient';
import {
  SarosClient,
  ContactDto,
  ContactStateNotification,
  Saros,
} from '../lsp';
import {messages} from './labels';
import {
  TreeDataProvider,
  ExtensionContext,
  EventEmitter,
  Event,
  TreeItem,
  TreeView,
  window,
} from 'vscode';
import {icons} from '../utils';
import {variables} from '../utils/variables';

/**
 * Provider for contacts of the accounts contact list.
 *
 * @export
 * @class SarosContactProvider
 * @implements {TreeDataProvider<ContactDto>}
 */
export class SarosContactProvider implements TreeDataProvider<ContactDto> {
  private _contacts: ContactDto[];

  /**
   * Creates an instance of SarosContactProvider.
   *
   * @param {SarosClient} client The Saros client
   * @param {ExtensionContext} _context The context of the extension
   * @memberof SarosContactProvider
   */
  constructor(client: SarosClient, private _context: ExtensionContext) {
    this._contacts = [];
    this.onDidChangeTreeData = this._onDidChangeTreeData.event;

    client.onNotification(ContactStateNotification.type,
        (contact: ContactDto) => {
          this.remove(contact);
          if (contact.subscribed) {
            this._contacts.push(contact);
          }

          this.refresh();
        });
  }

  /**
   * Removes the contact from the displayed list.
   *
   * @private
   * @param {ContactDto} contact The contact to remove
   * @memberof SarosContactProvider
   */
  private remove(contact: ContactDto): void {
    const contactIndex = this._contacts.findIndex((c) => c.id === contact.id);
    if (contactIndex >= 0) {
      this._contacts.splice(contactIndex, 1);
    }
  }

  /**
   * Refreshes the contact list.
   *
   * @memberof SarosContactProvider
   */
  refresh(): void {
    this._onDidChangeTreeData.fire(undefined);
  }

  /**
   * Clears the contact list.
   *
   * @memberof SarosContactProvider
   */
  clear(): void {
    this._contacts = [];
    this.refresh();
  }

  private _onDidChangeTreeData: EventEmitter<ContactDto | undefined> =
    new EventEmitter<ContactDto | undefined>();
  readonly onDidChangeTreeData: Event<ContactDto | undefined>;

  /**
   * Converts the contact to a tree item.
   *
   * @param {ContactDto} element Contact to convert
   * @return {(TreeItem | Thenable<TreeItem>)} The converted contact
   * @memberof SarosContactProvider
   */
  getTreeItem(element: ContactDto): TreeItem | Thenable<TreeItem> {
    const contactItem = new TreeItem(element.nickname);

    if (element.isOnline) {
      if (element.hasSarosSupport) {
        contactItem.iconPath = icons.getSarosSupportIcon(this._context);
      } else {
        contactItem.iconPath = icons.getIsOnlineIcon(this._context);
      }
    } else {
      contactItem.iconPath = icons.getIsOfflinetIcon(this._context);
    }

    contactItem.tooltip = element.id;
    contactItem.description = element.id;
    contactItem.contextValue = 'contact';

    return contactItem;
  }

  /**
   * Gets the children of a contact.
   *
   * @param {(ContactDto | undefined)} [element]
   * @return {ContactDto[]} A sorted list of contacts on root
   *  level and empty otherwise
   * @memberof SarosContactProvider
   */
  getChildren(element?: ContactDto | undefined): ContactDto[] {
    if (!element) {
      const sorted = this._contacts.sort((a: ContactDto, b: ContactDto) => {
        const valA = +a.hasSarosSupport + +a.isOnline;
        const valB = +b.hasSarosSupport + +b.isOnline;

        if (valA === valB) {
          return a.nickname > b.nickname ? 1 : -1;
        }

        return valA > valB ? -1 : 1;
      });

      return sorted;
    }

    return [];
  }
}

/**
 * View that displays the contacts of the accounts contact list.
 *
 * @export
 * @class SarosContactView
 * @implements {Disposable}
 */
export class SarosContactView implements Disposable {
  private _provider!: SarosContactProvider;
  private _view!: TreeView<ContactDto>;

  /**
   * Disposes all disposable resources.
   *
   * @memberof SarosAccountView
   */
  dispose(): void {
    this._view.dispose();
  }

  /**
   * Creates an instance of SarosContactView.
   *
   * @param {Saros} extension
   * @memberof SarosContactView
   */
  constructor(extension: Saros) {
    extension.client.onReady().then(() => {
      this._provider =
        new SarosContactProvider(extension.client, extension.context);
      this._view = window.createTreeView('saros-contacts',
          {treeDataProvider: this._provider});

      this._setOnline(false);

      extension.client.onConnectionChanged((isOnline: boolean) => {
        this._provider.refresh();
        this._setOnline(isOnline);
      });
    });
  }

  /**
   * Sets the online state.
   *
   * @private
   * @param {boolean} isOnline The online state
   * @memberof SarosContactView
   */
  private _setOnline(isOnline: boolean): void {
    if (!isOnline) {
      this._view.message = messages.NOT_CONNECTED;
      this._provider.clear();
    } else {
      this._view.message = '';
    }

    variables.setConnectionActive(isOnline);
  }
}
