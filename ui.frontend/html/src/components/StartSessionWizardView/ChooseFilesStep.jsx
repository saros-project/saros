import './style.css'
import { inject, observer } from 'mobx-react'
import React from 'react'
import Tree, { TreeNode } from 'rc-tree'

export default
@inject(({ core, sessionUI }) => ({
  initialProjectTrees: core.projectTrees,
  setCheckedKeys: sessionUI.setCheckedKeys,
  checkedKeys: sessionUI.checkedKeys
}))
@observer
class ChooseFilesStep extends React.Component {
  render() {
    const { initialProjectTrees, checkedKeys, setCheckedKeys } = this.props
    // This renders once before the initialProjectTrees got injected
    // Thats why we need to render something that does not depend on
    // initialProjectTrees first (e.g. null)
    if (!initialProjectTrees) return null
    return (
      <Tree
        className='project-tree'
        showLine checkable defaultExpandAll
        selectable={false}
        checkedKeys={Array.from(checkedKeys)}
        onCheck={setCheckedKeys}
      >
        {initialProjectTrees.map(({ root }, i) => renderTreeNode(root, i))}
      </Tree>
    )
  }
}

function renderTreeNode(root, i) {
  return (
    <TreeNode title={root.label} key={i}>
      {!!root.members.length && root.members.map(renderTreeNode)}
    </TreeNode>
  )
}
