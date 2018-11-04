import MouseDownMoveUpListeners from './mousedownmoveup';
import ViewManager from '../services/viewmanager'; // eslint-disable-line
/**
 * allows the creation of fabricjs objects by dragging across the canvas,
 * objects created with this can be defined by 4 numbers:
 * top, left, bottom, right
 * each created object can use these 4 values to define its actual parameters,
 * for example a circle will convert these values to centerX, centerY and radius.
 *
 * NOTE: all objects created by this class should have these functions:
 * $setStart(startX, startY)
 * $update(x1, y1, x2, y2)
 */
export default class DragDrawing extends MouseDownMoveUpListeners {
  /**
   * defines the listeners needed to allow drawing objects.
   * @param {ViewManager} viewManager
   * used to create and send the shapes
   * @param {String} svgName
   * the name of the shape to be created
   */
  constructor(viewManager, svgName) {
    let canvas = viewManager.getCanvas();
    let startX, startY, shape, hasMoved;
    super(canvas, {
      onMouseDown: (event) => {
        //convert mouse coordinates on the page to coordianates on the canvas
        let pointer = canvas.getPointer(event.e);
        //save start coordinates
        startX = pointer.x;
        startY = pointer.y;
        hasMoved = false;
        //create the actual view and set start parameters
        shape = viewManager.createView(svgName);
        shape.$setStart(startX, startY);
      },
      onMouseMove: (event) => {
        hasMoved = true;
        let pointer = canvas.getPointer(event.e);
        // give paramters to the view which then will be resized as needed
        shape.$update(startX, startY, pointer.x, pointer.y);
        canvas.renderAll();
      },
      onMouseUp: () => {
        // we dont want views with no height or width to stay on the canvas
        if (!hasMoved) {
          return canvas.remove(shape);
        }
        canvas.$fitObject(shape);
        viewManager.registerAndSend(shape);
      }
    });
  }
}
