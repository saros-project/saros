import { fabric } from 'fabric';
import AttributeMixin from './attributesmixin';
import SVG from '../constants/SVG';

export default class Path extends AttributeMixin(fabric.Path) {

  $getSVGName() {
    return SVG.PATH;
  }

  $fixScaling() {
    //not scalable!!
  }
}