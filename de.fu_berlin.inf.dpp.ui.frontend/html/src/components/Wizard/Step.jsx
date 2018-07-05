import React from 'react'
import { observable, action } from 'mobx'
import { observer } from 'mobx-react'
import P from 'prop-types'
import { noop } from 'Utils'
import './style.css'
import { Text } from 'react-localize'

@observer
export default class Step extends React.Component {
  static propTypes = {
    Component: P.node.isRequired,
    title: P.string,
    wizard: P.shape({
      hasNext: P.boolean,
      onClickNext: P.func,
    }),
  }

  static defaultProps = {
    onClickNext: noop,
    title: '',
  }

  @observable isValid = true
  @observable isPending = false

  setComponentRef = (ref) => {
    if (!ref || typeof ref.onClickNext !== 'function') return
    this.onClickNextCallback = ref.onClickNext.bind(ref)
  }

  onAsyncNext (ret) {
    this.isPending = true
    ret.then(() => {
      this.props.wizard.onClickNext()
    }).catch().then(() => {
      this.isPending = false
    })
  }

  @action.bound
  onClickNext () {
    if (!this.isValid) return
    let ret
    if (this.onClickNextCallback) {
      ret = this.onClickNextCallback()
    }
    if (ret instanceof Promise) {
      this.onAsyncNext(ret)
    } else {
      this.props.wizard.onClickNext()
    }
  }

  @action
  setIsValid (val) {
    this.isValid = val
  }

  render () {
    const { title, Component, wizard } = this.props
    const { hasNext } = wizard
    const btnClass = `btn btn-${hasNext ? 'default' : 'primary'}`
    return (
      <div className='wizard-step'>
        <nav className='header navbar navbar-default'>{title}</nav>
        <div className='wizard-body'>
          <Component ref={this.setComponentRef} setIsValid={this.setIsValid} />
        </div>
        <nav className='footer navbar navbar-default'>
          <button className='btn btn-default' onClick={wizard.onClickCancel}>
            <Text message='action.cancel' />
          </button>
          <button className={btnClass} onClick={this.onClickNext}>
            { hasNext ? 'Next' : 'Finish'}
          </button>
        </nav>
      </div>
    )
  }
}
