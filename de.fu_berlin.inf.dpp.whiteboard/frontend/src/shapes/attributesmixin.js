import SVG from "../constants/SVG";

/**
 * adds the attributes management needed to make a fabricjs object easily transferable.
 * NOTE: all functions and attributes start with '$' to separate them from native fabricjs
 * functions and properties
 * @param {fabric.Object} superClass
 * the class which will be extended and have these attributes
 */
let AttributeMixin = (superClass) => class extends superClass {
  constructor(...params) {
    super(...params);
    //this objects attributes
    this.$attrs = {};
    //this object can be overwritten by inherited objects to define custom mapping of attributes
    this.$objectSpecificAttributes = {};
  }

  /**
   * returns the attribute with the given id
   * @param {string} id
   * id of the target attribute
   * @returns {Object}
   * attribute with the given id
   */
  $getAttr(id) {
    return this.$attrs[id];
  }

  /**
   * returns attribute's id of the attribute with the given name
   * @param {string} name
   * attribute's name
   * @returns {string}
   * attribute's id
   */
  $getAttrID(name) {
    for (let attr of Object.values(this.$attrs))
      if (attr.name == name)
        return attr.rid;
  }

  /**
   * returns array of attributes which can be directly given to java
   * to be sent to other users
   * @returns {{name: string, chdata: string, rid: string}[]}
   * array of this object's attributes
   */
  $getTransferableAttrs() {
    this.$refreshAttrs();
    let toSend = [];
    for (let attr of Object.values(this.$attrs))
      if (attr.isDirty) {
        toSend.push({ name: attr.name, chdata: attr.chdata.toString(), rid: attr.rid });
        delete attr.isDirty;
      }
    return toSend;
  }

  /**
   * returns the svg name of the current object,
   * must be overwritten be subclasses
   * @return {String}
   * svg name
   */
  $getSVGName() {
    throw "not implemented";
  }

  /**
   * converts scaling factors to actual dimensions that can be transferred
   */
  $fixScaling() {
    throw "not implemented";
  }

  /**
   * applies the key, value pairs to this object while also saving them to be sent
   * @param {Object} obj
   */
  $set(obj) {
    for (let [name, chdata] of Object.entries(obj))
      this.$createOrSetAttrByName(name, chdata);
  }

  /**
   * allows the creation or setting of attributes without knowing the id
   * @param {String} name
   * @param {String} chdata
   */
  $createOrSetAttrByName(name, chdata) {
    let rid = this.$getAttrID(name);
    if (rid)
      this.$setAttrWithID(rid, chdata);
    else
      this.$createAttr(name, chdata);
  }

  /**
   * sets the value of the given attribute id to the value of chdata
   * @param {String} rid
   * @param {String} chdata
   */
  $setAttrWithID(rid, chdata) {
    let attr = this.$attrs[rid];
    attr.chdata = chdata;
    attr.isDirty = true;
    super.set(...this._$svgToFabric(attr.name, chdata));
    this.setCoords();
    return attr;
  }

  /**
   * creates a new attribute attached to this view.
   * @param {String} name
   * @param {String} chdata
   * @param {String} rid
   */
  $createAttr(name, chdata, rid) {
    rid = rid || new Date().getTime().toString() + Math.floor(Math.random() * 100000);
    this.$attrs[rid] = { name, chdata, rid, isDirty: true };
    super.set(...this._$svgToFabric(name, chdata));
    this.setCoords();
    return this.$attrs[rid];
  }

  /**
   * converts the parameters from SVG to fabricjs.
   * this function can be overwritten by subclasses in case there are any calculations needed
   * @param {String} name
   * attribute's name
   * @param {String} chdata
   * attribute's value
   */
  _$svgToFabric(name, chdata) {
    //convert to numbers
    if (SVG.isNumerical(name))
      chdata = Math.floor(parseInt(chdata));

    //svg save colors in the form : r,g,b
    //fabricjs expects color like this: rgb(r,g,b)
    if (SVG.isColor(name))
      chdata = "rgb(" + chdata + ")";

    //convert the property's name
    name = svgToFabricAttributes[name] || name;
    return [name, chdata];
  }

  /**
   * checks if any values has changed in the fabricjs object and writes them
   * to the $attrs property to be sent later.
   */
  $refreshAttrs() {
    for (let attr of Object.values(this.$attrs)) {
      let newVal = this._$svgFromFabric(attr.name);
      if (newVal.toString() !== attr.chdata.toString()) {
        attr.chdata = newVal;
        attr.isDirty = true;
      }
    }
  }

  /**
   * returns the values of svg attributes from this fabricjs object
   * this function can be overwritten by subclasses in case there are any calculations needed
   * @param {String} name
   * svg property name
   * @return {Object}
   * the value of the property
   */
  _$svgFromFabric(name) {
    switch (name) {
      case SVG.PROPERTIES.X:
        return this.left;
      case SVG.PROPERTIES.Y:
        return this.top;
      case SVG.PROPERTIES.CX:
        return this.left + (this.rx || this.radius);
      case SVG.PROPERTIES.CY:
        return this.top + (this.ry || this.radius);
      case SVG.PROPERTIES.R:
        return this.radius;
      case SVG.PROPERTIES.COLOR:
        return this.stroke.slice(4, -1);
      case SVG.PROPERTIES.FILL:
        return this.fill.slice(4, -1);
      default:
        return this[name];
    }
  }
};

export default AttributeMixin;

const svgToFabricAttributes = {
  x: "left",
  cx: "left",
  y: "top",
  cy: "top",
  r: "radius",
  color: "stroke",
};
