import JSON3 from 'json3';
import ShapeFactory from '../shapes/shapefactory';
import SXE from '../constants/SXE';

/**
 * manages all the views contained in the whiteboard, as well as sending actions to other users
 */
export default class ViewManager {
  /**
   * initliazes the view manager
   * @param {ResizableCanvas} canvas
   * the target canvas which will contain all the views
   */
  constructor(canvas) {
    this.canvas = canvas;
    this.factory = new ShapeFactory();
    this.views = {};
  }

  /**
   * returns canvas instance used by the view manager
   * @return {ResizableCanvas}
   * canvas instance
   */
  getCanvas() {
    return this.canvas;
  }

  /**
   * returns view with the given id
   * @param {String} id
   * @return {fabric.Object}
   *  view with the given id
   */
  getView(id) {
    return this.views[id];
  }

  /**
   * returns view that contains an attribute with the given id
   * @param {String} id
   * attribute id
   * @return {fabric.Object}
   *  parent of the attribute
   */
  getViewWithAttr(id) {
    return Object.values(this.views).filter(view => view.$getAttr(id)).pop();
  }

  /**
   * clears and resets everything
   */
  clear() {
    this.views = {};
    this.canvas.clear();
  }

  /**
   * creates a view and adds it to the canvas
   * @param {String} type
   * type of view to be created
   * @param {Array} params
   * the parameters of the view, will be given to its constructor
   * @return {fabric.Object}
   * the created view
   */
  createView(type, ...params) {
    let view = this.factory.create(type, ...params);
    if (view)
      this.canvas.add(view);
    return view;
  }

  /**
   * deletes view (if it exists)
   * @param {String} id
   * id of the view to be deleted
   * @return {fabric.Object}
   * the deleted view
   */
  deleteView(id) {
    let view = this.getView(id);
    if (!view)
      return;
    this.canvas.remove(view);
    delete this.views[view.rid];
    return view;
  }

  /**
   * register and sends a NEWLY created view
   * @param {fabric.Object} view
   * the newly created view
   */
  registerAndSend(view) {
    this.register(view);
    this.send(SXE.ACTIONS.NEW, view);
  }

  /**
   * registers a view, creates a random rid for the view if it doesnt have any.
   * @param {fabric.Object} view
   * the to be registered view
   * @return {fabric.Object}
   * the same view given in the parameter but after registration
   */
  register(view) {
    if (view.rid && this.views[view.rid]) {
      throw "trying to register a view that already exists, id: " +
      view.rid + ", type: " + this.views[view.rid].constructor.name;
    }
    view.rid = view.rid || new Date().getTime().toString() +
      Math.floor(Math.random() * 100000).toString();
    this.views[view.rid] = view;
    return view;
  }

  /**
   * sends an action to java, which will send it to other users
   * @param {String} action
   * action type
   * @param {fabric.Object} view
   * the view which contains the action's parameters.
   */
  send(action, view) {
    let browserAction = {
      type: action,
      id: view.rid
    };

    if (action == SXE.ACTIONS.NEW) {
      browserAction.elementType = view.$getSVGName();
      browserAction.attributes = view.$getTransferableAttrs();
    } else if (action == SXE.ACTIONS.SET) {
      browserAction.attributes = view.$getTransferableAttrs();
    } else if (action == SXE.ACTIONS.REMOVE) {
      //type and id are enough
    }
    if (window.__java_triggerAction)
      window.__java_triggerAction(JSON3.stringify(browserAction));
  }
}