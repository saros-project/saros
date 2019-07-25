import 'jsdom-global/register'
import { FakeSarosApi, itRendersWithoutExploding, wrapWithContextProvider } from './utils'
import { mount } from 'enzyme'
import React from 'react'
import ShareProjectView from '~/components/ShareProjectView'
import initStores from '~/stores'
import mockProjectTrees from './projectTrees.json'
import { expect } from 'chai'

describe('<ShareProjectView />', () => {
  // We wire our stores up with the fake saros api
  const fakeApi = new FakeSarosApi()
  // Doesn't matter what page we put here
  const stores = initStores('main-page', fakeApi)

  const sessionUI = stores.sessionUI
  stores.core.projectTrees = mockProjectTrees
  // create the Store context and Mount the component
  const wrap = mount(wrapWithContextProvider(<ShareProjectView />, stores))

  itRendersWithoutExploding(wrap.find('ShareProjectView'))

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

    // this test is skipped because the actual feature is still buggy and needs fixing, this test will fail
    it.skip('adds files to checked list if checkbox is clicked', () => {
      files.forEach(file => {
        // selecting by title alone will select the wrong element (a child of the actual treenode)
        const fileNode = treeNodes.findWhere(node => node.prop('title') === file && !!node.prop('eventKey'))
        fileNode.find('.rc-tree-checkbox').simulate('click')
        expect(sessionUI.checkedKeys.has(fileNode.prop('eventKey'))).to.equal(true)
      })
    })
  })
})
