import ResizableCanvas from './src/components/resizablecanvas';
import CanvasResizer from './src/services/canvasresizer';
/**
 * Main Whiteboard app
 */
class Whiteboard {
  /**
   * initiates the Whiteboard
   */
  constructor() {
    this.canvas = new ResizableCanvas('board');
    this.canvasResizer = new CanvasResizer(this.canvas);
  }
}

window.whiteboard = new Whiteboard();
