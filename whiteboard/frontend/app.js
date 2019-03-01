import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './src/components/menu.jsx';
import ResizableCanvas from './src/components/resizablecanvas';
import CanvasResizer from './src/services/canvasresizer';
import JavaBridge from './src/services/javabridge';
import ToolManager from './src/services/toolmanager';
import ViewManager from './src/services/viewmanager';
import ShortcutManager from './src/services/shortcutmanager.js';

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
    this.viewManager = new ViewManager(this.canvas);
    this.toolManager = new ToolManager(this.viewManager);
    this.javaBridge = new JavaBridge(this.viewManager);

    ReactDOM.render(
      <Menu toolManager={this.toolManager}
        canvasResizer={this.canvasResizer}
        ref={menu => this.menu = menu} />,
      document.getElementById('menuContainer'), () => {
        this.shortcutManager = new ShortcutManager(this.menu);
      });
  }
}

window.whiteboard = new Whiteboard();
