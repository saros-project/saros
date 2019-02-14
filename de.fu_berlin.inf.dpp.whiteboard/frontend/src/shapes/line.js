import { fabric } from 'fabric';
import AttributeMixin from './attributesmixin';
import SVG from '../constants/SVG';

export default class Line extends AttributeMixin(fabric.Line) {

  $refreshAttrs() {
    //lines need to be completly recalculated because
    //fabricjs only adjusts top and left and not x1, y1, x2, y2
    this.$recalculate();
    return super.$refreshAttrs();
  }

  $getSVGName() {
    return SVG.LINE;
  }

  $setStart() {
    //setting start will cause the line to flicker while drawing,
    //the $update function will handle the job.
  }

  $update(x1, y1, x2, y2) {
    this.$set({
      [SVG.PROPERTIES.X1]: x1,
      [SVG.PROPERTIES.X2]: x2,
      [SVG.PROPERTIES.Y1]: y1,
      [SVG.PROPERTIES.Y2]: y2
    });
  }

  $recalculate() {
    let { top, left, width, height, scaleX, scaleY, x1, x2, y1, y2 } = this;
    //to define the direction of the line
    //top left to bottom right:
    if (x1 < x2 && y1 < y2 || x2 < x1 && y2 < y1)
      this.$update(left, top, left + width * scaleX, top + height * scaleY);
    //top right to bottom left
    else
      this.$update(left + width * scaleX, top, left, top + height * scaleY);
  }

  $fixScaling() {
    this.$recalculate();
    this.set({ scaleX: 1, scaleY: 1 });
    this.setCoords();
  }
}