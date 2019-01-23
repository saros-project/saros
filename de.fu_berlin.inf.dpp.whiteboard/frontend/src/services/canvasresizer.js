import LOGIC from '../constants/logic';

export default class CanvasResizer {
  constructor(canvas) {
    this.canvas = canvas;
  }

  execute(action) {
    switch (action) {
      case LOGIC.CANVAS_TOOLS.EXPAND:
        this.canvas.$expand();
        return;
      case LOGIC.CANVAS_TOOLS.FIT:
        this.canvas.$fit();
        return;
    }
  }
}