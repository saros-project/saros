const NUMERICAL_ATTRIBUTES = new Set(["x", "x1", "x2", "y", "y1", "y2", "rx", "ry",
  "cx", "cy", "r", "width", "height", "fontSize"]);

const COLOR_ATTRIBUTES = new Set(["color", "fill"]);

const SVG = {
  RECT: "rect",
  CIRCLE: "circle",
  LINE: "line",
  ELLIPSE: "ellipse",
  TEXT: "text",
  POLYLINE: "polyline",
  PATH: "path",
  PROPERTIES: {
    X: "x",
    Y: "y",
    R: "r",
    D: "d",
    X1: "x1",
    X2: "x2",
    Y1: "y1",
    Y2: "y2",
    RX: "rx",
    RY: "ry",
    CX: "cx",
    CY: "cy",
    WIDTH: "width",
    HEIGHT: "height",
    TEXT: "text",
    COLOR: "color",
    FILL: "fill",
    FONT_SIZE: "fontSize"
  },
  /**
   * returns whether the attribute value should be a number
   * @param {string} name name of the attribute
   */
  isNumerical(name) {
    return NUMERICAL_ATTRIBUTES.has(name);
  },
  /**
   * returns whether the attribute describes a color property
   * values of color properties are usually a string with the format: "rgb(r,g,b)"
   * @param {string} name name of the attribute
   */
  isColor(name) {
    return COLOR_ATTRIBUTES.has(name);
  }
};

Object.freeze(SVG);
export default SVG;
