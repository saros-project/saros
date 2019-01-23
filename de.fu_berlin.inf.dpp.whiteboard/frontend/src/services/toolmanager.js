import LOGIC from '../constants/logic';
import SVG from '../constants/SVG';
import DragDrawing from '../tools/dragdrawing';
import InsertText from '../tools/inserttext';
import Selection from '../tools/selection';
import FreeDrawing from '../tools/freedrawing';

/**
 * contains all the tools that can be found in the tools menu,
 * any tool that is added here should have a mount() and an unmount()
 * function which enables and disables the tool respectively.
 */
export default class ToolManager {
  constructor(viewManager) {
    const TOOLS = LOGIC.TOOLS;
    this.currentTool = TOOLS.NONE;
    this.tools = {
      [TOOLS.NONE]: { mount: () => { }, unmount: () => { } },
      [TOOLS.DRAW_RECT]: new DragDrawing(viewManager, SVG.RECT),
      [TOOLS.DRAW_CIRCLE]: new DragDrawing(viewManager, SVG.CIRCLE),
      [TOOLS.DRAW_ELLIPSE]: new DragDrawing(viewManager, SVG.ELLIPSE),
      [TOOLS.DRAW_LINE]: new DragDrawing(viewManager, SVG.LINE),
      [TOOLS.INSERT_TEXT]: new InsertText(viewManager),
      [TOOLS.SELECTION]: new Selection(viewManager),
      [TOOLS.FREE_DRAWING]: new FreeDrawing(viewManager)
    };
  }

  /**
   * changes the currently active tool
   * @param {string} newToolName
   */
  changeTool(newToolName) {
    if (this.currentTool == newToolName)
      return;
    this.tools[this.currentTool].unmount();
    this.currentTool = newToolName;
    this.tools[this.currentTool].mount();
  }
}
