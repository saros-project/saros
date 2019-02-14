import ViewManager from '../services/viewmanager'; // eslint-disable-line
import SXE from '../constants/SXE';
/**
 * handles the manipulation of elements after being created,
 * current supported operations:
 * resizing, moving, and deleting.
 */
export default class Selection {
  /**
   * defines the needed properties for the implemented operations
   * @param {ViewManager} viewManager
   */
  constructor(viewManager) {
    this.viewManager = viewManager;
    this.canvas = viewManager.getCanvas();
    this.applyAndSend = this.applyAndSend.bind(this);
    this.deleteAndSend = this.deleteAndSend.bind(this);
  }

  /**
   * deletes the selected object (if any) and sends deletion event to other users
   * @param {Event} e
   * keyboard event on the document
   */
  deleteAndSend(e) {
    if (e.key == "Delete" || e.key == "Backspace") {
      let view = this.canvas.getActiveObject();
      if (view && !view.isEditing) {
        this.canvas.discardActiveObject();
        this.canvas.remove(view);
        this.viewManager.send(SXE.ACTIONS.REMOVE, view);
      }
    }
  }

  /**
   * send the changes of the modified object to other users
   * @param {Event} e
   * canvas object:modified event
   */
  applyAndSend(e) {
    let view = e.target;
    // fabric js scales the object using the scaleX and scaleY attributes,
    // which dont change the actual size values but its a kind of post-processing-scaling
    // this should convert scaling to actual dimensions.
    if(view.scaleX !== 1 || view.scaleY !== 1)
      view.$fixScaling();
    //update coordinates for the new scale
    view.setCoords();
    this.canvas.$fitObject(view);
    //send updated object to other saros users
    this.viewManager.send(SXE.ACTIONS.SET, view);
  }

  /**
   * enables selection mode on the canvas and its objects
   * and creates the listeners to send modified objects
   */
  mount() {
    this.canvas.$setSelectionEnabled(true);
    this.canvas.on("object:modified", this.applyAndSend);
    document.addEventListener("keydown", this.deleteAndSend);
  }

  /**
   * disables selection mode on the canvas and removes the listeners,
   * also discards any selected object
   */
  unmount() {
    this.canvas.$setSelectionEnabled(false);
    this.canvas.off("object:modified", this.applyAndSend);
    document.removeEventListener("keydown", this.deleteAndSend);
    // clear selection (if any)
    this.canvas.discardActiveObject();
    this.canvas.renderAll();
  }
}
