import SVG from "../constants/SVG";
import ViewManager from "../services/viewmanager"; // eslint-disable-line

/**
 * inserts text at mouse position when user presses mouse key down
 */
export default class InsertText {
  /**
   * defines the text creation event handler
   * @param {ViewManager} viewManager
   * used to create and send the objects
   */
  constructor(viewManager) {
    this.canvas = viewManager.getCanvas();
    this.insertHandler = (event) => {
      //convert mouse coordinates on the page to coordinates on the canvas
      let pointer = this.canvas.getPointer(event.e);
      let x = pointer.x;
      let y = pointer.y;
      //get text from user
      let text = prompt("input text:", "Class");
      if (!text || text.trim().length === 0)
        return;
      text = text.trim();
      //create and send text object
      let view = viewManager.createView(SVG.TEXT);
      view.$set({
        [SVG.PROPERTIES.TEXT]: text,
        [SVG.PROPERTIES.X]: x,
        [SVG.PROPERTIES.Y]: y
      });
      this.canvas.$fitObject(view);
      this.canvas.renderAll();
      viewManager.registerAndSend(view);
    };
  }

  /**
   * creates the event listener
   */
  mount() {
    this.canvas.on("mouse:down", this.insertHandler);
  }

  /**
   * removes the event listener
   */
  unmount() {
    this.canvas.off("mouse:down", this.insertHandler);
  }
}
