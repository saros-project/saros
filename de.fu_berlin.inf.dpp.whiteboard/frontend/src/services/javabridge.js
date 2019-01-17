import SVG from '../constants/SVG';
import SXE from '../constants/SXE';
/**
 * responsible for executing SXE commands and messages coming from java
 */
export default class JavaBridge {
  /**
   * creates the API which java uses to communicate with the html whiteboard
   * @param {ViewManager} viewManager
   */
  constructor(viewManager) {
    this.viewManager = viewManager;
    this.canvas = viewManager.getCanvas();

    //updates the whiteboard when a message is received
    window.message = this.message = (sxeMessage) => {
      let records = sxeMessage.records || [];
      if (sxeMessage.messageType === SXE.MESSAGE_TYPES.STATE) {
        //we ignore root element, all html whiteboard objects will be added to root element
        records.shift();
      }
      records.forEach(record => this.executeRecord(record));
      this.canvas.renderAll();
    };
    //sets the state of the whiteboard at the beginning of the session using data from Java Model
    window.setState = (state) => {
      //convert recursive data structure to linear structure
      let records = flatten(state);
      //ignore root element
      records.shift();
      //reset
      this.viewManager.clear();
      // apply
      records.forEach(record => this.createNew(record));
      this.canvas.renderAll();
    };
  }

  /**
   * executes a given record
   * @param {{type: string, valuePairs: Object}} record
   */
  executeRecord(record) {
    let action = record.type.toLowerCase();
    let props = record.valuePairs;
    switch (action) {
      case SXE.ACTIONS.NEW:
        return this.createNew(props);
      case SXE.ACTIONS.SET:
        return this.set(props);
      //the current java implementation of the SXE protocol uses "SET":visible=false instead of "REMOVE"
      //because its faster (while synchronizing), that why it is not possible to receive a "REMOVE" action
      case SXE.ACTIONS.REMOVE:
        return;
    }
  }

  /**
   * executes a "NEW" record
   * @param {{visible: boolean, rid: string, name: string, parent?:string, chdata?:string}} recordProps
   */
  createNew(recordProps) {
    // the value HAS to be false to ignore creation
    // if the value is undefined or not given, proceed normally
    if (recordProps.visible === false)
      return;

    switch (recordProps.type) {
      case SXE.TYPES.ELEMENT: {
        let { rid, name } = recordProps;
        let view = this.viewManager.createView(name);
        if (!view)
          return;
        view.rid = rid;
        this.viewManager.register(view);
        return view;
      }
      case SXE.TYPES.ATTR: {
        let { parent, chdata, rid, name } = recordProps;
        let view;
        if (name === SVG.PROPERTIES.D) {
          //path objects cannot be correctly displayed without the "command" or "d" parameter
          //we have to re-create the path because fabricjs doesn't respond to dynamic changes to the path
          let old = this.viewManager.getView(parent);
          this.viewManager.deleteView(parent);
          //create the correct path
          view = this.viewManager.createView(SVG.PATH, chdata);
          //copy all attributes
          view.rid = old.rid;
          for (let attr of Object.values(old.$attrs))
            view.$createAttr(attr.name, attr.chdata, attr.rid);
          this.viewManager.register(view);
        } else {
          view = this.viewManager.getView(parent);
        }
        if (!view)
          return;
        let attr = view.$createAttr(name, chdata, rid);
        attr.isDirty = false;
        this.canvas.$fitObject(view);
        return attr;
      }
    }
  }

  /**
   * applies a "SET" record
   * @param {Object} recordProps
   */
  set(recordProps) {
    let { chdata, visible, target } = recordProps;
    // set visible="false" is a workaround to delete elements
    if (visible === "false") {
      this.viewManager.deleteView(target);
    } else {
      let view = this.viewManager.getViewWithAttr(target);
      if (!view)
        return;
      view.$setAttrWithID(target, chdata);
      this.canvas.$fitObject(view);
      return view;
    }
  }
}


//Util:
function flatten (obj, parentID) {
  let { children, attributes, rid } = obj;
  delete obj.children;
  delete obj.attributes;
  obj.type = obj.type.toLowerCase();
  obj.parent = parentID;
  let arr = [obj];
  if (attributes && attributes.length) {
    let flattenAttributes = attributes.map(attr => flatten(attr, rid));
    arr = arr.concat(...flattenAttributes);
  }
  if (children && children.length) {
    let flattenChildren = children.map(child => flatten(child, rid));
    arr = arr.concat(...flattenChildren);
  }
  return arr;
}
