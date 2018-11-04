/**
 * Groups the creation of event listeners and the binding to event handlers.
 * Creates the Listners for mouse down, mouse move, mouse up on the Fabric.js canvas element.
 * Note that this class uses the events from Fabric.js canvas element,
 * which are very similar to standard events.
 */
export default class MouseDownMoveUpListeners {
  /**
   * creates the mouse down/move/up listeners on the canvas (given in the first parameter)
   * with the event handlers (given in the second parameter)
   *
   * The eventHandlers object should have the following structure:
   * {
   *  eventName: function (canvasEvent) { ... },
   *  ...
   * }
   *
   * possible event names: "onMouseDown", "onMouseMove", "onMouseUp"
   * @param {fabric.Canvas} canvas
   * the canvas object on which the event listeners should be defined
   *
   * @param {Object} eventHandlers
   * contains all event handlers for mouse down / move / up
   */
  constructor(canvas, eventHandlers) {
    this.canvas = canvas;
    this.eventHandlers = eventHandlers || {};
    // define all missing functions
    ["onMouseDown", "onMouseMove", "onMouseUp"].forEach(event => {
      if (typeof this.eventHandlers[event] !== "function")
        this.eventHandlers[event] = function () {};
    });
    //binding to make "this" available in the function
    this.onmousedown = this.onmousedown.bind(this);
  }

  /**
   * Mouse down listener which executes the mouse down handler and adds the mouse move and mouse up listeners
   * @param {Event} e
   * MouseDown Event
   */
  onmousedown(e) {
    this.eventHandlers.onMouseDown(e);
    let onmousemove = (e) => this.eventHandlers.onMouseMove(e);
    let onmouseup = (e) => {
      this.canvas.off('mouse:move', onmousemove);
      this.canvas.off('mouse:up', onmouseup);

      this.eventHandlers.onMouseUp(e);
    };
    this.canvas.on('mouse:move', onmousemove);
    this.canvas.on('mouse:up', onmouseup);
  }

  mount() {
    this.canvas.on('mouse:down', this.onmousedown);
  }

  unmount() {
    this.canvas.off('mouse:down', this.onmousedown);
  }
}