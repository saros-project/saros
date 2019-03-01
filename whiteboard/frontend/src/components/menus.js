import LOGIC from '../constants/logic.js';

import drawRectImg from '../../assets/images/draw_rect.png';
import drawCircleImg from '../../assets/images/draw_circle.png';
import drawLineImg from '../../assets/images/draw_line.png';
import drawEllipseImg from '../../assets/images/draw_ellipse.png';
import insertTextImg from '../../assets/images/insert_text.png';
import selectionImg from '../../assets/images/selection.png';
import toolsImg from '../../assets/images/tools_menu.png';
import freeDrawingImg from '../../assets/images/hand_drawing.png';
import canvasImg from '../../assets/images/canvas.png';
import canvasExpandImg from '../../assets/images/canvas_expand.png';
import canvasFitImg from '../../assets/images/canvas_fit.png';

/**
 * this file contains the values needed for rendering the menu,
 * any images should be imported directly in javascript because they will be converted
 * to base64 data urls with webpack to create the whiteboard SPA.
 */
const Menus = [
  //Tools menu
  {
    name: "Tools",
    imageSrc: toolsImg,
    onlyOneActive: true,
    contents: [{
        name: "Selection Mode",
        description: "Select, move, resize and delete elements",
        imageSrc: selectionImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.SELECTION
      },
      {
        name: "Free Drawing Mode",
        description: "Draw freely with your mouse!",
        imageSrc: freeDrawingImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.FREE_DRAWING
      },
      {
        name: "Draw Rectangle",
        description: "Draws a rectangle, draw by holding the mouse button down and moving the cursor around.",
        imageSrc: drawRectImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.DRAW_RECT
      },
      {
        name: "Draw Circle",
        description: "Draws a circle, draw by holding the mouse button down and moving the cursor around.",
        imageSrc: drawCircleImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.DRAW_CIRCLE
      },
      {
        name: "Draw Line",
        description: "Draws a line, draw by holding the mouse button down and moving the cursor around.",
        imageSrc: drawLineImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.DRAW_LINE
      },
      {
        name: "Draw Ellipse",
        description: "Draws an ellipse, draw by holding the mouse button down and moving the cursor around.",
        imageSrc: drawEllipseImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.DRAW_ELLIPSE
      },
      {
        name: "Insert Text",
        description: "Adds text to whiteboard, click on the desired location to input and insert text.",
        imageSrc: insertTextImg,
        toolLogic: LOGIC.TOOL,
        toolName: LOGIC.TOOLS.INSERT_TEXT
      }
    ]
  },
  // canvas menu
  {
    name: "Canvas",
    imageSrc: canvasImg,
    onlyOneActive: false,
    contents: [{
        name: "Expand Canvas",
        description: "makes the canvas bigger from the bottom and right sides",
        imageSrc: canvasExpandImg,
        toolLogic: LOGIC.CANVAS,
        toolName: LOGIC.CANVAS_TOOLS.EXPAND
      },
      {
        name: "Fit Canvas",
        description: "Scales the canvas so it fits all the objects",
        imageSrc: canvasFitImg,
        toolLogic: LOGIC.CANVAS,
        toolName: LOGIC.CANVAS_TOOLS.FIT
      }
    ]
  }
];

export default Menus;