import { inject, observer } from 'mobx-react'
import React from 'react'
import cn from 'classnames'
@inject('configurationUI', 'api')
@observer
export default class ColorStep extends React.Component {
  constructor (props) {
    super()
    this.colors = props.api.getUserColorSet()
  }

  onClickColor = (e) => {
    const color = e.target.dataset.name
    this.props.configurationUI.changeWizardData('color', 'selected', color)
  }

  render () {
    const { color } = this.props.configurationUI.data
    return (
      <div className='container color-step'>
        <div className='row color-container'>
          <span>
            {Object.keys(this.colors).map(col => (
              <div
                key={col}
                data-name={col}
                style={{ backgroundColor: this.colors[col] }}
                className={cn('color-box', { selected: color.selected === col })}
                onClick={this.onClickColor}
              />
            ))}
          </span>
        </div>
      </div>
    )
  }
}
