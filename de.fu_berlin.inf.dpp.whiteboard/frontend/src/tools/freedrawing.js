import SVG from "../constants/SVG";
import ViewManager from "../services/viewmanager"; // eslint-disable-line

/**
 * allows drawing using the mouse to simulate hand drawing,
 * setting the attribute isDrawingMode of the fabricjs canvas to true
 * enables the native drawing mode of the library,
 * this class takes the created drawings or paths and replaces
 * them with views that can be sent through java and SXE to other users
 */
export default class FreeDrawing {
  /**
   * defines the needed properties for path object creation
   * @param {ViewManager} viewManager
   * used to create and send the objects
   */
  constructor(viewManager) {
    this.viewManager = viewManager;
    this.canvas = viewManager.getCanvas();
    this.overwritePath = this.overwritePath.bind(this);
  }

  /**
   * converts the path in the event to an SXE path
   * @param {Event} event
   * fabricjs event which contains the path
   */
  overwritePath(event) {
    // get needed parameters for the new path
    let pathObj = event.path;
    let { top, left } = pathObj;
    // fabricjs has its own internal data structure to represent path objects,
    // however, we need to extract the SVG command that draws that path because
    // the old whiteboard uses SVG, otherwise it would crash
    // the command of the path is saved into the "d" parameter of the SVG path object
    let pathStr = pathObj.toSVG().match(/d="([^"]+)"/)[1];
    this.canvas.remove(pathObj);
    // creates new path
    let newPath = this.viewManager.createView(SVG.PATH, pathStr);
    newPath.$set({
      [SVG.PROPERTIES.D]: pathStr,
      [SVG.PROPERTIES.X]: left,
      [SVG.PROPERTIES.Y]: top
    });
    this.canvas.$fitObject(newPath);
    this.viewManager.registerAndSend(newPath);
  }

  /**
   * enables drawing and defines the listener
   */
  mount() {
    this.canvas.set('isDrawingMode', true);
    this.canvas.on("path:created", this.overwritePath);
  }

  /**
   * disables drawing and removes the listener
   */
  unmount() {
    this.canvas.set('isDrawingMode', false);
    this.canvas.off("path:created", this.overwritePath);
  }
}
