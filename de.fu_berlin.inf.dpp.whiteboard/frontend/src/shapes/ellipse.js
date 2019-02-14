import { fabric } from 'fabric';
import AttributeMixin from './attributesmixin';
import SVG from '../constants/SVG';

export default class Ellipse extends AttributeMixin(fabric.Ellipse) {

  _$svgToFabric(name, chdata) {
    if (SVG.isNumerical(name))
      chdata = parseInt(chdata);

    switch (name) {
      case SVG.PROPERTIES.CX:
        return ["left", chdata - (this.rx || 0)];

      case SVG.PROPERTIES.CY:
        return ["top", chdata - (this.ry || 0)];

      case SVG.PROPERTIES.RX:
        if (!this.rx)
          this.set('left', this.left - chdata);
        return [name, chdata];

      case SVG.PROPERTIES.RY:
        if (!this.ry)
          this.set('top', this.top - chdata);
        return [name, chdata];

      default:
        return super._$svgToFabric(name, chdata);
    }
  }

  $getSVGName() {
    return SVG.ELLIPSE;
  }

  $setStart(cx, cy) {
    this.$set({
      [SVG.PROPERTIES.CX]: cx,
      [SVG.PROPERTIES.CY]: cy
    });
  }

  $update(x, y, x2, y2) {
    let rx = Math.abs(x - x2) / 2;
    let ry = Math.abs(y - y2) / 2;
    let cx = Math.min(x, x2) + rx;
    let cy = Math.min(y, y2) + ry;
    this.$set({
      [SVG.PROPERTIES.RY]: ry,
      [SVG.PROPERTIES.RX]: rx,
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