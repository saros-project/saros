import React from 'react';
import LOGIC from '../constants/logic';
import Menus from './menus';

export default class Menu extends React.Component {
  /**
   * @param {Object} props
   * properties given to the constructor
   */
  constructor(props) {
    super(props);
    this.toolManager = props.toolManager;
    this.canvasResizer = props.canvasResizer;
    this.state = {};
    this.miniMode = false;
    this.contextMenuMode = false;
    this.wrapper = document.getElementById("menuContainer");
  }

  /**
   * changes a logic to a given value,
   * the switch can be expanded to add additional functionality.
   * @param {string} logicName
   * the name of the logic that will be changed
   * @param {string} logicValue
   * the value of the target logic
   */
  change(logicName, logicValue) {
    this.setState({ [logicName]: logicValue });
    switch (logicName) {
      case LOGIC.TOOL:
        return this.toolManager.changeTool(logicValue);
      case LOGIC.CANVAS:
        return this.canvasResizer.execute(logicValue);
      default:
        throw "Unknown logic: " + logicName + " with value: " + logicValue;
    }
  }

  /**
   * the actual component, adding items to the menus.js file will automatically show them
   */
  render() {
    return <div>
      {Menus.map((menu, i) =>
        <div className="dropDownMenu" key={i} onDoubleClick={() => this.switchMiniMode()}
          title="Double click to switch mini mode">
          <div className="dropDownMenuContainer">
            <img draggable="false" src={menu.imageSrc} className="dropDownMenuImage" alt={menu.name} />
            <span className="dropDownMenuText">{menu.name}</span>
          </div>
          <div className="dropDownMenuItemsContainer">
            {menu.contents.map((tool, j) =>
              <div onClick={() => this.change(tool.toolLogic, tool.toolName)} key={j}
                title={tool.description} className={"dropDownMenuItem " +
                  (menu.onlyOneActive && this.state[LOGIC.TOOL] == tool.toolName ? "dropDownMenuItemActive" : "")}>
                <img draggable="false" src={tool.imageSrc} className="dropDownMenuItemImage" alt={tool.name} />
              </div>)}
          </div>
        </div>)}
    </div>;
  }

  /**
   * enables or disables mini mode, mini mode makes the menu smaller and uses less screen space
   */
  switchMiniMode() {
    this.miniMode = !this.miniMode;
    this.updateWrapperCSS();
  }

  /**
   * sets the css classes on the menu wrapper according to the state of the menu
   */
  updateWrapperCSS() {
    let isMini = this.miniMode || this.contextMenuMode;
    this.wrapper.className = isMini ? "miniMode" : "";
  }

  /**
   * sets the context menu mode for the menu, it is used when right mouse button
   * is clicked to force mini mode, but not permanently.
   * @param {boolean} state
   */
  setContextMenuMode(state) {
    this.contextMenuMode = state;
    this.updateWrapperCSS();
  }
  /**
   * sets the position of the menu
   * @param {number} x
   * @param {number} y
   */
  setPosition(x = 0, y = 0) {
    this.wrapper.style.left = x + "px";
    this.wrapper.style.top = y + "px";
  }
}
