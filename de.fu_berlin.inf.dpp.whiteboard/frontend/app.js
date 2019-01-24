import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './src/components/menu.jsx';
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
    ReactDOM.render(
      <Menu toolManager={this.toolManager}
      canvasResizer={this.canvasResizer}/> ,
      document.getElementById('menuContainer'));
  }
}

window.whiteboard = new Whiteboard();
