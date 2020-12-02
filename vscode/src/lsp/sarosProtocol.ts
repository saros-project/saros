import {
  NotificationType,
  RequestType,
} from 'vscode-languageclient';

/**
 * Generic response that indicates success or failure.
 *
 * @export
 * @interface SarosResponse
 */
export interface SarosResponse {
  success: boolean;
  error: string;
}

/**
 * Response that indicates success or failure and
 * contains a payload.
 *
 * @export
 * @interface SarosResultResponse
 * @extends {SarosResponse}
 * @template T The payload of the response, ie. actual return
 * value
 */
export interface SarosResultResponse<T> extends SarosResponse {
  result: T;
}

/**
 * Contains data about an account.
 *
 * @export
 * @interface AccountDto
 */
export interface AccountDto {
  username: string;
  domain: string;
  password: string;
  server: string;
  port: number;
  useTLS: boolean;
  useSASL: boolean;
  isDefault: boolean;
}

/**
 * Contains data about a contact.
 *
 * @export
 * @interface ContactDto
 */
export interface ContactDto {
  id: string;
  nickname: string;
  isOnline: boolean;
  hasSarosSupport: boolean;
  subscribed: boolean;
}

/**
 * Notification that informs the client about a
 * state change of the XMPP connection, ie. if
 * it's active or not.
 *
 * @export
 */
export namespace ConnectedStateNotification {
  export const type =
    new NotificationType<SarosResultResponse<boolean>, void>(
        'saros/connection/state',
    );
}

/**
 * Notification that informs the client about a
 * state change of a contact, eg. online status
 * or saros support.
 *
 * @export
 */
export namespace ContactStateNotification {
  export const type =
    new NotificationType<ContactDto, void>('saros/contact/state');
}

/**
 * Request to the server to add a new account for
 * connections to the XMPP server.
 *
 * @export
 */
export namespace AddAccountRequest {
  /**
   * Used to add an account to the account store.
   *
   * @export
   * @interface AddInput
   */
  export interface AddInput {
    password: string;
    server: string;
    port: number;
    useTLS: boolean;
    useSASL: boolean;
    isDefault: boolean;
  }

  export const type =
    new RequestType<AddInput, SarosResponse, void, unknown>(
        'saros/account/add',
    );
}

/**
 * Request to the server to update an existing account.
 *
 * @export
 */
export namespace UpdateAccountRequest {
  /**
   * Used to update an account in the account store.
   *
   * @export
   * @interface UpdateInput
   */
  export interface UpdateInput {
    password: string;
    server: string;
    port: number;
    useTLS: boolean;
    useSASL: boolean;
    isDefault: boolean;
  }

  export const type =
    new RequestType<UpdateInput, SarosResponse, void, unknown>(
        'saros/account/update',
    );
}

/**
 * Request to the server to remove an existing account.
 *
 * @export
 */
export namespace RemoveAccountRequest {
  /**
   * Used to remove an account from the account store.
   *
   * @export
   * @interface RemoveAccountInput
   */
  export interface RemoveInput {
    username: string;
    domain: string;
  }

  export const type =
    new RequestType<RemoveInput, SarosResponse, void, unknown>(
        'saros/account/remove',
    );
}

/**
 * Request to the server to set the currently active account.
 *
 * @export
 */
export namespace SetActiveAccountRequest {
  /**
   * Used to set an account active.
   *
   * @export
   * @interface SetActiveInput
   */
  export interface SetActiveInput {
    username: string;
    domain: string;
  }

  export const type =
    new RequestType<SetActiveInput, SarosResponse, void, unknown>(
        'saros/account/setActive',
    );
}

/**
 * Request to the server to get all saved accounts.
 *
 * @export
 */
export namespace GetAllAccountRequest {
  export const type =
    new RequestType<void, SarosResultResponse<AccountDto[]>, void, unknown>(
        'saros/account/getAll',
    );
}

/**
 * Request to the server to add a new contact.
 *
 * @export
 */
export namespace AddContactRequest {
  /**
   * Used to add a contact to the contact list.
   *
   * @export
   * @interface AddInput
   */
  export interface AddInput {
    id: string;
    nickname: string;
  }

  export const type =
    new RequestType<AddInput, SarosResponse, void, unknown>(
        'saros/contact/add',
    );
}

/**
 * Request to the server to remove an existing contact.
 *
 * @export
 */
export namespace RemoveContactRequest {
  /**
   * Used to remove a contact from the contact list.
   *
   * @export
   * @interface RemovetDto
   */
  export interface RemovetDto {
    id: string;
  }

  export const type =
    new RequestType<RemovetDto, SarosResponse, void, unknown>(
        'saros/contact/remove',
    );
}

/**
 * Request to the server to change the nickname of an
 * existing contact.
 *
 * @export
 */
export namespace RenameContactRequest {
 /**
  * Used to rename a contact on the contact list.
  *
  * @export
  * @interface RenameInput
  */
  export interface RenameInput {
    id: string;
    nickname: string;
  }

  export const type =
    new RequestType<RenameInput, SarosResponse, void, unknown>(
        'saros/contact/rename',
    );
}

/**
 * Request to the server to get all contacts
 * of the contact list.
 *
 * @export
 */
export namespace GetAllContactRequest {
  export const type =
    new RequestType<void, SarosResultResponse<ContactDto[]>, void, unknown>(
        'saros/contact/getAll',
    );
}

/**
 * Request to the server to connect to the XMPP
 * server with the currently active account.
 *
 * @export
 */
export namespace ConnectRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/connection/connect',
    );
}

/**
 * Request to the server to disconnect from the XMPP
 * server.
 *
 * @export
 */
export namespace DisconnectRequest {
  export const type =
    new RequestType<void, SarosResponse, void, unknown>(
        'saros/connection/disconnect',
    );
}

/**
 * Request to the server to get the current state
 * of the connection, ie. active or not.
 *
 * @export
 */
export namespace ConnectionStateRequest {
  export const type =
    new RequestType<void, SarosResultResponse<boolean>, void, unknown>(
        'saros/connection/state',
    );
}
