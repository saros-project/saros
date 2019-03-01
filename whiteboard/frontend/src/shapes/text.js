import AttributeMixin from './attributesmixin';
import { fabric } from 'fabric';
import SVG from '../constants/SVG';

export default class Text extends AttributeMixin(fabric.IText) {
  /**
   * the constructor is needed to have a default value for the text;
   * passing null to the IText constructor will throw an exception
   * @param {String} text
   * @param {Object} options
   */
  constructor(text = "", options = {}) {
    super(text, options);
  }

  _$svgToFabric(name, chdata) {
    //we replace color with fille because
    //color will only change the outline of the text.
    if (name === SVG.PROPERTIES.COLOR)
      return ["fill", "rgb(" + chdata+ ")"];
    else
      return super._$svgToFabric(name, chdata);
  }

  $getSVGName() {
    return SVG.TEXT;
  }


  $fixScaling() {
    let avg = (this.scaleX + this.scaleY) / 2;
    let newFontSize = Math.floor(this.fontSize * avg);
    this.set({ scaleX: 1, scaleY: 1 });
    this.$set({
      [SVG.PROPERTIES.FONT_SIZE]: newFontSize
    });
  }
}