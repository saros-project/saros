import React from 'react';
import P from 'prop-types';

/**
 * This class represents a node in the file tree, it is rendered in the start session wizard, when the
 * user is choosing the files to share with other users.
 * A TreeNode is represented by the arrow (to open/close the sub-tree), a checkbox to select the subtree
 * an icon according to the type and finally the file/folder name
 */
export default class TreeNode extends React.Component {
  static propTypes = {
    /**
     * the actual node to be rendered, the contents of this object are actually received from Java
     * each node should contain following attributes:
     * label: the name of the file/folder
     * type: "FOLDER" | "FILE"
     * members: the children of this node, or all files under this directory, is basically an array of nodes
     */
    node: P.object,
    /**
     * this string used to identify this node, it has the form:
     * for example: 0-1-4 which means: first project, second folder, 5th file or folder
     * this identifier is created from the identifier of the parent + "-" + index in parent
     */
    identifier: P.string,
    /**
     * how deep should the Tree render initially. WARNING: rendering really deep is not efficient
     */
    initialDepth: P.number,
    /**
     * depth of the current node
     */
    depth: P.number,
    /**
     * boolean representing if the parent is already checked, if true, then this element is automatically checked
     */
    parentIsChecked: P.bool,
    /**
     * a Hash map containing all the selected items as keys, the values are always "true"
     */
    checkedItems: P.object,
    /**
     * the parent of the current TreeNode, which can be a TreeNode or a RootNode
     */
    parent: P.object
  }

  constructor(props) {
    super(props);
    let { isChecked, isOpen, numChildrenChecked, anyChildChecked } = getInitialState(props);
    this.state = {
      // whether the sub-tree is collapsed or not
      isOpen,
      // whether this item is checked and selected for sharing or not
      isChecked,
      // a counter representing the number of selected children, when this number reaches the total number
      // of children, then this node should also be selected (since all children are already selected)
      numChildrenChecked,
      // whether any item in the whole subtree is selected, used to highlight elements that have children selected,
      // but are not selected themselves, this is beneficial to the user, it will mark the folder or project
      // as partly selected, so any accidental selections will be very clear
      anyChildChecked
    };
    // references to children TreeNodes
    this.children = []
  }

  render() {
    return (
      <div className='tree-item'>
        {/* the element itself */}
        <div className='tree-item-line'>
          <span className={this.getArrowClasses()} onClick={this.toggleOpen}>{this.getArrow()}</span>
          <span onClick={this.toggleIsChecked}>
            <span className={this.getCheckboxClasses()}>{this.getCheckboxContent()}</span>
            <div className='tree-item-text'>{this.props.node.label}</div>
          </span>
        </div>
        {/* the children TreeNodes */}
        {this.state.isOpen && this.hasMembers() &&
          <div className='tree-item-children'>
            <div className='tree-item-vertical-hierarchy-line'></div>
            {this.getMembers().map((member, i) =>
              <TreeNode key={i + member.label} ref={child => this.addChildRef(child, i)}
                node={member} identifier={this.props.identifier + '-' + i} depth={this.props.depth + 1}
                parentIsChecked={this.state.isChecked} initialDepth={this.props.initialDepth}
                checkedItems={this.props.checkedItems} parent={this} />
            )}
          </div>
        }
      </div>
    );
  }


  /**
   * @returns {object[]} the contents of the descending TreeNodes
   */
  getMembers() {
    return this.props.node.members || [];
  }

  /**
   * @returns {boolean} true if there are children to render, false otherwise
   */
  hasMembers() {
    return this.getMembers().length > 0;
  }

  /**
   * saves the reference of the child TreeNode
   * @param {TreeNode} child
   * @param {number} index
   */
  addChildRef(child, index) {
    this.children[index] = child;
  }

  /**
   * this function is called when the user clicks on the checkbox of the TreeNode to select/deselect the item
   */
  toggleIsChecked = () => {
    let isChecked = !this.state.isChecked;
    // select or deselect all elements in the subtree
    this.updateSelfAndChildren(isChecked);
    // tell parent that the state has updated
    this.props.parent.childStateUpdated(isChecked);
  }

  /**
   * applies the change of the state to this TreeNode and all of its descendants
   * @param {boolean} isChecked
   */
  updateSelfAndChildren(isChecked) {
    // if this item is checked, then numChildrenChecked should be equal to the number of children
    let numChildrenChecked = isChecked && this.hasMembers() ? this.getMembers().length : 0;
    this.setState({ isChecked, numChildrenChecked, anyChildChecked: isChecked });
    // if this.state.isOpen is true, this means that the children are rendered and we have the references
    // to the children in the children array
    if (this.state.isOpen) {
      for (let child of this.children)
        child.updateSelfAndChildren(isChecked);
    } else {
      // when isOpen is false, the children components do not exist
      // we clear the values of all of the children from "checkedItems"
      // since this function is called when the whole state of the subtree is changed, we can ignore
      // the old state of the nodes and delete them
      // when the children are recreated again, they will inherit their "isChecked" state from their parent.
      clearAllChildren(this.props);
    }
  }

  /**
   * this function call propagates from children components to their parents, it updates the state of the parent
   * according to the state of the children
   * @param {boolean} childIsChecked
   */
  childStateUpdated(childIsChecked) {
    // increment/decrement numChildrenChecked
    let numChildrenChecked = this.state.numChildrenChecked;
    numChildrenChecked += childIsChecked ? 1 : -1;

    // if numChildrenChecked equals the total number of children, that means this item should also be checked
    // since all children are already selected
    let isChecked = numChildrenChecked === this.getMembers().length;
    let stateHasChanged = isChecked !== this.state.isChecked;

    this.setState({ isChecked, numChildrenChecked });
    this.subtreeStateUpdated();
    if (stateHasChanged) {
      // propagate state change to parent
      this.props.parent.childStateUpdated(isChecked);
    } else {
      // we still want all parents to know if they have any child selected
      // this is done in the subtreeStateUpdated() function call
      let parent = this.props.parent;
      while (parent) {
        parent.subtreeStateUpdated();
        parent = parent.props.parent
      }
    }
  }

  /**
   * since the value of the state attribute "anyChildChecked" is totally dependant on the state of the whole subtree
   * and is not dynamic, it requires manual updating, this function does that
   */
  subtreeStateUpdated() {
    const { identifier, checkedItems } = this.props;
    const anyChildChecked = hasAnyChild(checkedItems, identifier);
    this.setState({ anyChildChecked });
  }

  /**
   * intercepts all setState calls to save the state of selected items to the global hash of checked items
   * @param {any} obj
   */
  setState(obj) {
    if (obj.hasOwnProperty('isChecked')) {
      const { identifier, checkedItems } = this.props;
      if (obj.isChecked)
        checkedItems[identifier] = true;
      else
        delete checkedItems[identifier];
    }
    super.setState(obj);
  }

  /**
   * this function is called when the user clicks on the arrow to open/close the subtree
   */
  toggleOpen = () => {
    let isOpen = !this.state.isOpen;
    this.setState({ isOpen });
    if (!isOpen && this.state.isChecked) {
      // if the the sub-tree is closed and this item is checked, this means all of the sub-tree items
      // are also checked, so we delete the entries of the sub-tree in the checkedItems hash map
      // when the children are rendered again, the will inherit isChecked from the parent TreeNode
      clearAllChildren(this.props);
    }
  }


  getCheckboxClasses() {
    return 'tree-item-checkbox ' + (
      (!this.state.isChecked && this.state.anyChildChecked) ? 'checkbox-highlighted' : ''
    );
  }

  getArrowClasses() {
    return 'tree-item-arrow ' + (this.hasMembers() ? 'tree-item-fill-background' : '');
  }

  /**
   * check {@link https://www.key-shortcut.com/en/writing-systems/35-symbols/arrows/} to see
   * the representation of each of these unicode chars
   */
  getArrow() {
    return this.getMembers().length > 0 ? (this.state.isOpen ? '\u25E2' : '\u25B7') : '\u00A0';
  }

  getCheckboxContent() {
    return this.state.isChecked ? '\u2714' : '\u00A0';
  }

}
/**
 * computes the values which are used to setup the tree node
 * @param {{checkedItems: object, identifier: string, node: any, initialDepth: number, depth: number, parentIsChecked: boolean}} props
 */
function getInitialState({ checkedItems, identifier, node, initialDepth, depth = 0, parentIsChecked = false }) {
  // the node is checked if the parent is already checked or there is an entry in the hash map for this node
  let isChecked = parentIsChecked || !!checkedItems[identifier];

  // make sure we save the state back to the hash map, this is essential for the correctness of
  // subtreeStateUpdated()
  if (isChecked)
    checkedItems[identifier] = true;

  // if the item is checked, take the length of children, if not, search for them in the map of checked items
  let numChildrenChecked = isChecked ? (node.members || []).length : directChildren(checkedItems, identifier).length;

  let anyChildChecked = isChecked || numChildrenChecked > 0 || hasAnyChild(checkedItems, identifier);

  // if the item is already checked, no need to open it, less clutter on screen for the user
  // however, we want to open it in case any item is selected in the sub tree, so that users can quickly
  // find and change the selection
  let isOpen = !isChecked && (anyChildChecked || depth < initialDepth);

  return { isChecked, isOpen, numChildrenChecked, anyChildChecked };
}

/**
 * @param {object} map
 * @param {string} identifier
 * @returns {string[]} the identifiers of the selected children of the identifier given in the second paramter
 */
function directChildren(map, identifier) {
  // since identifiers have the format "index-index-index-index", we determine direct children by number dashes + 1
  let numDashes = getNumDashes(identifier);
  return Object.keys(map).filter(key =>
    map[key] && key.length > identifier.length && key.indexOf(identifier) === 0 && getNumDashes(key) === numDashes + 1
  );
}

/**
 * @param {object} map
 * @param {string} identifier
 * @returns {string[]}
 *  the identifiers of all selected children in the subtree of the identifier given in the paramter
 */
function allChildren(map, identifier) {
  let numDashes = getNumDashes(identifier);
  // deeper children have numDashes higher than this element
  return Object.keys(map).filter(key =>
    map[key] && key.length > identifier.length && key.indexOf(identifier) === 0 && getNumDashes(key) > numDashes
  );
}

/**
 * clears the entries of the of whole subtree given by the identifier
 * @param {any} props
 */
function clearAllChildren({ checkedItems, identifier }) {
  allChildren(checkedItems, identifier).forEach(id => delete checkedItems[id]);
}

/**
 * this function is almost identical to allChildren() except this function will stop when any child is found
 * @param {object} map
 * @param {string} identifier
 * @returns {boolean} whether the identifier given has any selected children in the map object
 */
function hasAnyChild(map, identifier) {
  let numDashes = getNumDashes(identifier);
  return Object.keys(map).some(key => map[key] && key.length > identifier.length &&
    key.indexOf(identifier) === 0 && getNumDashes(key) > numDashes
  );
}

function getNumDashes(string) {
  let dashes = 0;
  for (let c of string)
    if (c === '-')
      dashes++;
  return dashes;
}
