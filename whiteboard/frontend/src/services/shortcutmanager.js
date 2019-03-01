import Menu from "../components/menu"; // eslint-disable-line
/**
 * responsible for handling shortcuts like displaying a small menu next to the cursor when right mouse button
 * is clicked, this class can be extended with additional shortcuts for switching between the different tools
 */
export default class ShortcutManager {
  /**
   * creates the listeners for the different user interactions
   * @param {Menu} menu
   */
  constructor(menu) {
    this.contextMenuMode = false;
    this.menu = menu;
    this.overlay = document.getElementById("overlay");

    document.addEventListener("contextmenu", (e) => {
      // we do not want the default context menu to show up
      e.preventDefault();
      // if the right mouse button is pressed alone, enable the contextMenuMode
      if(!(e.button == 2 && e.buttons == 1))
        this.enableContextMenu(e);
    });
    document.addEventListener("click", (e) => {
      // disable contextMenuMode on any left click
      if (e.button == 0 && this.contextMenuMode)
        this.disableContextMenu();
    });
    document.addEventListener("keydown", (e) => {
      // disable contextMenuMode on any key
      if (this.contextMenuMode)
        this.disableContextMenu();
      // block key combinations, for example: (ctrl + A)
      if (e.key in blockedKeys)
        e.preventDefault();
    });
  }

  /**
   * enables the context menu mode
   * @param {MouseEvent} e
   */
  enableContextMenu(e) {
    this.contextMenuMode = true;
    this.menu.setContextMenuMode(true);
    this.overlay.style.display = "block";
    this.menu.setPosition(e.clientX, e.clientY);
  }

  /**
   * disables the context menu mode
   */
  disableContextMenu() {
    this.contextMenuMode = false;
    this.menu.setContextMenuMode(false);
    this.overlay.style.display = "none";
    this.menu.setPosition(0, 0);
  }
}

// expand this object for all blocked keys
const blockedKeys = {
  Escape: true,
  Ctrl: true,
  Alt: true
};
