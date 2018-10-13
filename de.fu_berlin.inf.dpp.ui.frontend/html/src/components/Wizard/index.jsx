import { action, computed, observable } from 'mobx'
import { observer } from 'mobx-react'
import React from 'react'
export { default as Step } from './Step'

@observer
export default class Wizard extends React.Component {
  @observable step = 0

  @computed
  get currentStepInstance () {
    return this.props.children[this.step]
  }

  @computed
  get hasNext () {
    return this.step < this.props.children.length - 1
  }

  @computed
  get hasPrev () {
    return this.step > 0
  }

  @action
  onClickNext = () => {
    if (this.hasNext) {
      this.step = this.step + 1
    } else {
      this.props.onFinish()
    }
  }

  @action
  onClickBack = () => {
    if (this.hasPrev) {
      this.step = this.step - 1
    }
  }

  render () {
    return React.cloneElement(
      this.currentStepInstance,
      {
        wizard: {
          onClickNext: this.onClickNext,
          hasNext: this.hasNext,
          onClickCancel: this.props.onClickCancel,
          onClickBack: this.onClickBack,
          hasPrev: this.hasPrev
        }
      }
    )
  }
}
