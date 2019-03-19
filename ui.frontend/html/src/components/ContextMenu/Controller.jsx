import FocusableItem from '../Focusable/FocusableItem';
import FocusableMenu from '../Focusable/FocusableMenu';
import React from 'react';
import { Text } from 'react-localize';


export default class ContextMenuController {
  constructor(core) {
    this.core = core;
    // reference the actual context menu component
    this.ctxMenu = null;
    // setup event listeners
    // disable original context menu
    document.addEventListener('contextmenu', e => e.preventDefault());
    // hide menu on any click
    document.addEventListener('click', () => this.ctxMenu.hide());
  }

  /**
   * set the reference to the actual context menu component, this is called after the menu has rendered
   * @param {React.Component} reference
   */
  setRef(reference) {
    this.ctxMenu = reference;
  }

  showSessionMemberMenu(jid, source, event) {
    //TODO: this menu should display on the contacts not on session members
    // it is used here for testing
    this.showContactMenu(jid, source, event);
  }

  /**
   * shows a menu when a contact has been right-clicked
   * @param {string} jid jid of the contact that has been clicked
   * @param {any} source the element which would recieve focus after the menu is closed
   * @param {MouseEvent} event used to set position of the menu
   */
  showContactMenu(jid, source, event) {
    this.ctxMenu.setContent(
      <this.Menu source={source}>
        {/* react does not support nested dropdowns, we change the contents again*/}
        <FocusableItem onClick={() => this.showProjects(jid, source)}>
          <Text message="action.workTogetherOn"></Text>&nbsp;&gt;
        </FocusableItem>
        {/* for debugging purposes */}
        <FocusableItem>{jid}</FocusableItem>
      </this.Menu>
    );
    this.ctxMenu.show(event);
  }

  /**
   * shows a list of projects to share with a contact
   * @param {string} jid jid of the contact, with which we want to share the project
   * @param {any} source the element which would recieve focus after the menu is closed
   */
  showProjects(jid, source) {
    this.ctxMenu.setContent(
      <this.Menu source={source}>
        {/* react does not support nested dropdowns, we change the contents back to the first menu*/}
        <FocusableItem onClick={() => this.showSessionMemberMenu(jid, source)}>
          &lt;&nbsp;<Text message="action.back"></Text>
        </FocusableItem>
        {this.core.projectTrees.map((project, i) =>
          <FocusableItem key={i}>{project.root.label}</FocusableItem>
        )}
      </this.Menu>
    );
    this.ctxMenu.show();
  }

  /**
   * private component which uses Focusable menu to render and closes the contextMenu on the escape key
   */
  Menu = ({source, children}) => {
    return (
      <FocusableMenu source={source} onKeyDownCapture={e => e.key === "Escape" && this.ctxMenu.hide()}>
       {children}
      </FocusableMenu>
    );
  }
}

/**
 * TODO:
 * * Session Member Menu:
 * - if self.isHost
 *   - if member.isSelf
 *     - change color
 *     - Grant Write Access (Deactivated/Depends on Permissions)
 *     - Restrict to Read Only Access (Deactivated/Depends on Permissions)
 *   - else
 *     - Grant Write Access (Deactivated/Depends on Permissions)
 *     - Restrict to Read Only Access (Deactivated/Depends on Permissions)
 *     - Remove from Session
 *     - Seperator
 *     - Follow Participant
 *     - Jump to Editing Position
 *     - Seperator
 *     - Open Chat
 *     - Send file
 * - else
 *   - if member.isSelf
 *     - Change Color
 *   - else
 *     - Follow Participant
 *     - Jump to Editing Position
 *     - Seperator
 *     - Open Chat
 *     - Send file
 *
 *
 * * Contact Menu:
 * - if self.isConnected && Contact.isOffline:
 *   - WorkTogther On
 *   - Open Chat
 *   - Send File (Deactivated)
 *   - Rename
 *   - Delete
 * - if self.isOffline:
 *   - Same as above but all are deactivated
 */
