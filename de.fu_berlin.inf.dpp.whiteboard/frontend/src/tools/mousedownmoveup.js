/**
 * Groups the creation of event listeners and the binding to event handlers.
 * Creates the Listeners for mouse down, mouse move, mouse up on the Fabric.js canvas element.
 * Note that this class uses the events from Fabric.js canvas element,
 * which are very similar to standard events.
 */
export default class MouseDownMoveUpListeners {
  /**
   * creates the mouse down/move/up listeners on the canvas (given in the first parameter)
   * with the event handlers (given in the second parameter)
   *
   * possible event names: "onMouseDown", "onMouseMove", "onMouseUp"
   * @param {fabric.Canvas} canvas
   * the canvas object on which the event listeners should be defined
   *
   * @param {
     {
       "onMouseDown": (event) => any,
       "onMouseMove": (event) => any,
       "onMouseUp": (event) => any
     }
   }
   * eventHandlers
   *  an object containing all event handlers for mouse down / move / up
   */
  constructor(canvas, eventHandlers) {
    this.canvas = canvas;
    this.eventHandlers = eventHandlers || {};
    // define all missing functions
    ["onMouseDown", "onMouseMove", "onMouseUp"].forEach(event => {
      if (typeof this.eventHandlers[event] !== "function")
        this.eventHandlers[event] = function() {};
    });
    //binding to make "this" available in the function
    this.onmousedown = this.onmousedown.bind(this);
  }

  /**
   * will be called when the mouse button is pressed down, which then executes
   *  the mouse down handler given to the constructor and adds the mouse move and mouse up listeners
   * @param {Event} e
   * MouseDown Event
   */
  onmousedown(e) {
    // call the MouseDown event handler
    this.eventHandlers.onMouseDown(e);

    // when moving, call the MouseMove event handler
    let onmousemove = (e) => this.eventHandlers.onMouseMove(e);

    // When mouse button is released, remove all event listeners
    let onmouseup = (e) => {
      this.canvas.off('mouse:move', onmousemove);
      this.canvas.off('mouse:up', onmouseup);
      // call the MouseUp event handler
      this.eventHandlers.onMouseUp(e);
    };
    // attach the handlers to the canvas
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
