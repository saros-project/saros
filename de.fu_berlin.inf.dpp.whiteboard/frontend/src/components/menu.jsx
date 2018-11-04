import React from 'react';
import Menus from './menus';
import LOGIC from '../constants/logic';

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
    this.wrapper = document.getElementById("menuContainer");
    //start in miniMode because the windows in eclipse are small
    //this.switchMiniMode();
  }

  /**
   * changes a logic to a given value,
   * the switch can be expanded to add additional functionality.
   * @param {String} logicName
   * the name of the logic that will be changed
   * @param {String} logicValue
   * the value of the target logic
   */
  change(logicName, logicValue) {
    this.setState({ [logicName]: logicValue });
    switch (logicName) {
      case LOGIC.TOOL:
        return this.toolManager.changeTool(logicValue);
      case (LOGIC.CANVAS):
        return this.canvasResizer.execute(logicValue);
      default:
        throw "Unknown logic: " + logicName + " with value: " + logicValue;
    }
  }

  /**
   * the actual component, adding items to the menus.js file will automaticly show them
   */
  render() {
    return <div>
      {Menus.map((menu, i) =>
        <div className="dropDownMenu" key={i} onDoubleClick={() => this.switchMiniMode()}
        title="Double click to swtich mini mode">
          <div className="dropDownMenuContainer">
            <img draggable="false" src={menu.imageSrc} className="dropDownMenuImage" alt={menu.name}/>
            <span className="dropDownMenuText">{menu.name}</span>
          </div>
          <div className="dropDownMenuItemsContainer">
            {menu.contents.map((tool, j) =>
              <div onClick={() => this.change(tool.toolLogic, tool.toolName)} key={j}
                title={tool.description} className={"dropDownMenuItem " +
                  (menu.onlyOneActive && this.state[LOGIC.TOOL] == tool.toolName ? "dropDownMenuItemActive" : "")}>
                <img draggable="false" src={tool.imageSrc} className="dropDownMenuItemImage" alt={tool.name}/>
              </div>)}
          </div>
        </div>)}
    </div>;
  }

  switchMiniMode() {
    this.miniMode = !this.miniMode;
    if (this.miniMode)
      this.wrapper.className = "miniMode";
    else
      this.wrapper.className = "";
  }
  /**
   * sets mini tool for the menu,
   * is used  when right mouse button is clicked to force mini mode, but not permenantly
   * @param {Boolean} state
   */
  setMiniMode(state) {
    if (this.miniMode)
      return;
    if (state)
      this.wrapper.className = "miniMode";
    else
      this.wrapper.className = "";
  }
  /**
   * sets the position of the menu
   * @param {Number} x
   * @param {Number} y
   */
  setPosition(x = 0, y = 0) {
    this.wrapper.style.left = x + "px";
    this.wrapper.style.top = y + "px";
  }
}
