import { fabric } from 'fabric';
import AttributeMixin from './attributesmixin';
import SVG from '../constants/SVG';

export default class Circle extends AttributeMixin(fabric.Circle) {

  _$svgToFabric(name, chdata) {
    if (SVG.isNumerical(name))
      chdata = parseInt(chdata);

    switch (name) {
      case SVG.PROPERTIES.CX:
        chdata = chdata - (this.radius || 0);
        return ["left", chdata];

      case SVG.PROPERTIES.CY:
        chdata = chdata - (this.radius || 0);
        return ["top", chdata];

      case SVG.PROPERTIES.R:
        if (!this.radius)
          this.set('left', this.left - chdata).set('top', this.top - chdata);
        return ["radius", chdata];

      default:
        return super._$svgToFabric(name, chdata);
    }
  }

  $getSVGName() {
    return SVG.CIRCLE;
  }

  $setStart(cx, cy) {
    this.$set({
      [SVG.PROPERTIES.CX]: cx,
      [SVG.PROPERTIES.CY]: cy
    });
  }

  $update(x, y, x2, y2) {
    let r = (Math.abs(x - x2) + Math.abs(y - y2)) / 4;
    let cx = Math.min(x, x2) + Math.abs(x - x2) / 2;
    let cy = Math.min(y, y2) + Math.abs(y - y2) / 2;
    this.$set({
      [SVG.PROPERTIES.R]: r,
      [SVG.PROPERTIES.CX]: cx,
      [SVG.PROPERTIES.CY]: cy
    });
  }

  $fixScaling() {
    let { top, left, width, height, scaleX, scaleY } = this;
    this.set({ scaleX: 1, scaleY: 1 });
    this.$update(left, top, left + width * scaleX, top + height * scaleY);
  }
}