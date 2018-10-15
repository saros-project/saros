import 'jsdom-global/register'
import { itRendersWithoutExploding, shouldRender } from './utils'
import { mount } from 'enzyme'
import { spy } from 'sinon'
import React from 'react'
import Wizard, { Step } from '~/components/Wizard'

describe('<Wizard />', () => {
  const onFinish = spy()
  const wrap = mount(
    <Wizard onFinish={onFinish}>
      {[1, 2, 3].map(i => (
        <Step
          title={i}
          Component={
            () => <span className={`step-${i}`} />
          }
        />
      ))}
    </Wizard>
  )

  itRendersWithoutExploding(wrap)

  const nextPage = () => wrap.find('.btn-next').simulate('click')

  it('goes to nextPage when clicking Next', () => {
    shouldRender(wrap.find('.step-1'))
    nextPage()
    shouldRender(wrap.find('.step-2'))
    nextPage()
    shouldRender(wrap.find('.step-3'))
  })

  it('calls onFinish on last page', () => {
    shouldRender(wrap.find('.btn-finish'))
    wrap.find('.btn-finish').simulate('click')
    expect(onFinish.calledOnce).to.equal(true)
  })
})
