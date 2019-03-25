import { inject, observer } from 'mobx-react'
import React from 'react'
import TreeRoot from './FileTree/TreeRoot';

export default
@inject('core', 'sessionUI')
@observer
class ChooseFilesStep extends React.Component {
  render() {
    return (
      <TreeRoot
        roots={this.props.core.projectTrees.map(tree => tree.root)}
        checkedKeys={this.props.sessionUI.checkedKeys}
        onKeysChange={keys => this.props.sessionUI.setCheckedKeys(keys)}
        initialDepth={0}
      />
    )
  }
}
