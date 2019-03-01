import SVG from '../constants/SVG';
import Rectangle from './rectangle';
import Circle from './cricle';
import Line from './line';
import Ellipse from './ellipse';
import Text from './text';
import Path from './path';

export default class ShapeFactory {
  create(type, ...params) {
    switch (type) {
      case SVG.RECT:
        return new Rectangle(...params);
      case SVG.CIRCLE:
        return new Circle(...params);
      case SVG.ELLIPSE:
        return new Ellipse(...params);
      case SVG.LINE:
        return new Line(...params);
      case SVG.TEXT:
        return new Text(...params);
      case SVG.PATH:
        return new Path(...params);
      default:
        return;
    }
  }
}