import React from 'react';
import P from 'prop-types';
import TreeNode from './TreeNode';
import './style.css'


export default class TreeRoot extends React.Component {
  static propTypes = {
    /**
     * the actual nodes to be rendered, the contents of this object are actually received from Java
     */
    roots: P.arrayOf(P.object),
    /**
     * how deep should the Tree render initially. WARNING: rendering really deep is not efficient
     */
    initialDepth: P.number,
    /**
     * array of checked items, the values are used to populate the checked file tree initially
     * this is beneficial when the user changes the step of the wizard and returns back, selected items are not lost
     */
    checkedKeys: P.arrayOf(P.string),
    /**
     * a callback which would be called every time when the checked items have changed
     */
    onKeysChange: P.func
  }

  constructor(props) {
    super(props);
    this.checkedItems = {};
    // populate the hash map with already checked items
    this.props.checkedKeys.forEach(identifier => this.checkedItems[identifier] = true);
  }

  render() {
    return (
      <div className='folder-tree' onClick={this.click}>
        {this.props.roots.map((root, i) =>
          <TreeNode key={root.label} node={root}
          identifier={i.toString()} checkedItems={this.checkedItems}
          initialDepth={this.props.initialDepth} parent={this} />)}
      </div>
    );
  }

  /**
   * this function gets called when any child is the sub tree is updated
   */
  subtreeStateUpdated() {
    this.keysChanged();
  }

  /**
   * this function gets called when the direct children of this root are updated
   */
  childStateUpdated() {
    this.keysChanged();
  }

  /**
   * if there is callback function, call it with the new keys
   */
  keysChanged() {
    if (this.props.onKeysChange) {
      this.props.onKeysChange(this.getKeys())
    }
  }

  /**
   * @returns {string[]} currently selected items, items have the form "index-index-index..."
   */
  getKeys() {
    return Object.keys(this.checkedItems);
  }
}
