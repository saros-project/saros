// default config for fabricjs objects
const FABRIC_DEFAULTS = {
  //applies to all objects
  GENERAL: {
    //calculates dimensions from top left corner
    originX: 'left',
    originY: 'top',
    //fill all shapes with white and outline them with black
    fill: 'rgb(255,255,255)',
    stroke: 'rgb(0,0,0)',
    strokeWidth: 1,
    //by default objects are not selectable, this is changed with the selection tool
    selectable: false,
    hoverCursor: 'default',
    //rotation is currently not implemented
    lockRotation: true,
    hasRotatingPoint: false,
    // better visibility of the selected object
    transparentCorners: false,
    hasBorders: true
  },
  //applies to IText objects
  TEXT_DEFAULTS: {
    fontSize: 20,
    fontFamily: 'Times New Roman',
    fill: 'rgb(0,0,0)',
    stroke: null,
    hoverCursor: 'default'
  },
  //applies to path objects (same objects created by free drawing)
  PATH_DEFAULTS: {
    //resizing paths is too complex, disable it for now
    lockScalingX: true,
    lockScalingY: true,
    fill: null,
  }
};
Object.freeze(FABRIC_DEFAULTS);
export default FABRIC_DEFAULTS;
