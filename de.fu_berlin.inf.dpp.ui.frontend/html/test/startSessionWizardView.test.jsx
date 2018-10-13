import 'jsdom-global/register'
import {
  FakeSarosApi,
  itRendersWithoutExploding,
  wrapWithContextProvider
} from './utils'
import { mount } from 'enzyme'
import { spy } from 'sinon'
import React from 'react'
import StartSessionWizardView from '~/components/StartSessionWizardView'
import initStores from '~/stores'
import mockProjectTrees from './projectTrees.json'

describe('<StartSessionWizardView />', () => {
  // We wire our stores up with the fake saros api
  const fakeApi = new FakeSarosApi()
  // Doesn't matter what page we put here
  const stores = initStores('main-page', fakeApi)
  stores.mainUI = { doShowAddContactView: spy() }
  const sessionUI = stores.sessionUI
  stores.core.projectTrees = mockProjectTrees
  // create the Store context and Mount the component
  const wrap = mount(wrapWithContextProvider(<StartSessionWizardView />, stores))

  itRendersWithoutExploding(wrap.find('StartSessionWizardView'))

  describe('<ChooseFilesStep />', () => {
    const chooseFilesWrap = wrap.find('ChooseFilesStep')
    itRendersWithoutExploding(chooseFilesWrap)

    const files = [
      'afile',
      'somefile'
    ]

    const treeNodes = wrap.find('TreeNode')
    it('renders the tree', () => {
      expect(!!treeNodes.length).to.equal(true)
    })

    it('adds files to checked list if checkbox is clicked', () => {
      files.forEach(file => {
        // selecting by title alone will select the wrong element (a child of the actual treenode)
        const fileNode = treeNodes.findWhere(node => node.prop('title') === file && !!node.prop('eventKey'))
        fileNode.find('.rc-tree-checkbox').simulate('click')
        expect(sessionUI.checkedKeys.has(fileNode.prop('eventKey'))).to.equal(true)
      })
    })
  })
})
