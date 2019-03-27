import { inject, observer } from 'mobx-react'
import React from 'react'
import FileTree from './FileTree/FileTree';

export default
@inject('core', 'sessionUI')
@observer
class ChooseFilesStep extends React.Component {
  render() {
    return (
      <FileTree
        roots={this.props.core.projectTrees.map(tree => tree.root)}
        checkedKeys={this.props.sessionUI.checkedKeys}
        onKeysChange={keys => this.props.sessionUI.setCheckedKeys(keys)}
        initialDepth={0}
      />
    )
  }
}
