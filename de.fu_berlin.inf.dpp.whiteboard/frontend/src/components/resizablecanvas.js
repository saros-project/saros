import { fabric } from 'fabric';
import background from '../../assets/images/grad.png';
import DEFAULTS from '../constants/fabricconfig';

// default expand value for the canvas (in pixels)
const EXPAND_VALUE = 500;
/**
 * extended fabricjs canvas which can be easily resized and is responsive to window size.
 * NOTE: all defined functions start with '$' to separate them from native fabricjs functions
 */
export default class ResizableCanvas extends fabric.Canvas {
  /**
   * sets up the canvas and its resize listeners as well as the
   * default configurations for its objects
   * @param {string} htmlID
   * the id of the html canvas element, out of which a fabricjs
   * canvas element will be created
   */
  constructor(htmlID) {
    super(htmlID, {
      //disable selection of multiple objects
      selection: false,
    });

    //add background
    this.setBackgroundColor({ source: background, repeat: 'repeat' });

    //sets up default configurations for different fabricjs object types
    Object.assign(fabric.Object.prototype, DEFAULTS.GENERAL);
    Object.assign(fabric.IText.prototype, DEFAULTS.TEXT_DEFAULTS);
    Object.assign(fabric.Path.prototype, DEFAULTS.PATH_DEFAULTS);

    // add resize listeners
    window.addEventListener("resize", () => this.$fit());
    window.addEventListener("load", () => this.$fit());
    this.on("object:modified", (e) => this.$fitObject(e.target));
  }

  /**
   * resizes the canvas to window size or to fit all elements in it
   */
  $fit() {
    // this is used because of the very old and unpredictable browser in eclipse
    let windowWidth = window.innerWidth || document.documentElement.clientWidth ||
      document.body.clientWidth;
    let windowHeight = window.innerHeight || document.documentElement.clientHeight ||
      document.body.clientHeight;

    let newWidth = Math.max(windowWidth, max(rightMostPoint, this.getObjects()));
    let newHeight = Math.max(windowHeight, max(bottomMostPoint, this.getObjects()));

    if (newWidth !== this.width)
      this.setWidth(newWidth);
    if (newHeight !== this.height)
      this.setHeight(newHeight);
  }

  /**
   * resizes the canvas (if needed) to fit the view
   * @param {fabric.Object} view
   * the view which the canvas will try to fit to.
   */
  $fitObject(view) {
    let right = rightMostPoint(view);
    if (right > this.width)
      this.setWidth(right + EXPAND_VALUE);

    let bottom = bottomMostPoint(view);
    if (bottom > this.height)
      this.setHeight(bottom + EXPAND_VALUE);
  }

  /**
   * extends the canvas size by the given value in the paramter
   * @param {number} value
   */
  $expand(value = EXPAND_VALUE) {
    this.setWidth(this.width + value);
    this.setHeight(this.height + value);
  }

  /**
   * enables or disables the selection mode on the canvas and its elements
   * @param {boolean} state
   */
  $setSelectionEnabled(state) {
    let newAttributes = {
      selectable: state,
      hoverCursor: state ? 'move' : 'default'
    };
    this.forEachObject(obj => obj.set(newAttributes));
    this.renderAll();
  }
}

//Utils:
/**
 * calculates the max values of the given objects in the array using a custom value function
 * @param {(obj: Object) => number} valueFunction
 * used to calculate the value of each object
 * @param {Object[]} array
 * array of objects
 * @returns {number}
 */
function max(valueFunction, array = []) {
  return Math.max(...array.map(object => valueFunction(object)));
}

/**
 * sums the left and width coordinates to find the furthest point to the right
 * @param {Object} obj
 * target object
 * @return {number}
 * the furthest point to the right of the object
 */
function rightMostPoint(obj) {
  return sumAttributes(obj, ["width", "left"]);
}

/**
 * sums the top and height coordinates to find the furthest point to the bottom
 * @param {Object} obj
 * target object
 * @return {number}
 * the furthest point to the bottom of the object
 */
function bottomMostPoint(obj) {
  return sumAttributes(obj, ["height", "top"]);
}

/**
 * sums the values of the given attributes in the object
 * @param {Object} obj
 * @param {string[]} attributes
 * @return {number}
 */
function sumAttributes(obj = {}, attributes = []) {
  return attributes.reduce((acc, attr) => acc + (obj[attr] || 0), 0);
}
