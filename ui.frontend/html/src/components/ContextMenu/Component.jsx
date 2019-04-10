import React from 'react';

export default class ContextMenu extends React.Component {
  constructor(...args) {
    super(...args);
    this.root = React.createRef();
    this.state = {
      isHidden: true,
      content: null
    };
  }

  /**
   * @returns {boolean} true if the menu is hidden, false otherwise
   */
  isHidden() {
    return this.state.isHidden;
  }

  /**
   * hides the context menu
   */
  hide() {
    if (!this.isHidden())
      this.setState({ isHidden: true });
  }

  /**
   * shows the content menu
   * @param {MouseEvent} event used to set the position of the menu,
   * if not given, the menu stays at the same position
   */
  show(event) {
    if (event) {
      let node = this.root.current;
      node.style.top = event.clientY + 'px';
      node.style.left = event.clientX + 'px';
    }
    this.setState({ isHidden: false });
  }

  /**
   * set the content of the context menu, this function is only called from the ContextMenuController
   * @param {React.Component} content
   */
  setContent(content) {
    this.setState({ content });
  }

  /**
   * @returns {string} className for the contextMenu, the classes control the visibility of the menu
   */
  rootClasses() {
    if (this.isHidden() || !this.state.content)
      return "contextMenuHidden"
    return "";
  }

  render() {
    return (
      <div id="contextMenu" ref={this.root} className={this.rootClasses()}>
        {this.state.content}
      </div>
    );
  }

  componentDidUpdate() {
    // when the menu updates, new elements are added
    // on each update, we want to focus the first element
    // so that users can directly move with arrow keys for example
    let node = this.root.current.querySelector('.dropdown-item');
    if (node)
      node.focus();
  }
}
