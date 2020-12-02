import {ExtensionContext} from 'vscode';

export namespace icons {
  /**
   * Gets the icon that indicates Saros support of an user.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getSarosSupportIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/obj16/contact_saros_obj.png');
  };

  /**
   * Gets the icon that indicates that an user is online.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getIsOnlineIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/obj16/contact_obj.png');
  };

  /**
   * Gets the icon that indicates that an user is offline.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getIsOfflinetIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/obj16/contact_offline_obj.png');
  };

  /**
   * Gets the icon that indicates the ability to add an account.
   *
   * @export
   * @param {ExtensionContext} context The context of the extension
   * @return {string} Absolute path to the icon
   */
  export const getAddAccountIcon = (context: ExtensionContext) => {
    return context.asAbsolutePath('/media/btn/addaccount.png');
  };
}
