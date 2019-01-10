import { fabric } from 'fabric';
import AttributeMixin from './attributesmixin';
import SVG from '../constants/SVG';

export default class Rectangle extends AttributeMixin(fabric.Rect) {

  $getSVGName() {
    return SVG.RECT;
  }

  $setStart(x, y) {
    this.$set({
      [SVG.PROPERTIES.X]: x,
      [SVG.PROPERTIES.X]: y,
    });
  }

  $update(x, y, x2, y2) {
    this.$set({
      [SVG.PROPERTIES.X]: Math.min(x, x2),
      [SVG.PROPERTIES.Y]: Math.min(y, y2),
      [SVG.PROPERTIES.WIDTH]: Math.abs(x - x2),
      [SVG.PROPERTIES.HEIGHT]: Math.abs(y - y2)
    });
  }

  $fixScaling() {
    let { top, left, width, height, scaleX, scaleY } = this;
    this.set({ scaleX: 1, scaleY: 1 });
    this.$update(left, top, left + width * scaleX, top + height * scaleY);
  }
}