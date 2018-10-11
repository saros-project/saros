import invariant from 'invariant'

export function onlineFirst (a, b) {
  const on = x => x.presence === 'Online'
  if (on(a) && !on(b)) {
    return -1
  } else if (on(b) && !on(a)) {
    return 1
  }
  return a.displayName.localeCompare(b.displayName)
}

export function getJid ({ username, domain }) {
  return `${username}@${domain}`
}

export function firstCharToLowerCase (str) {
  invariant(str.length >= 1, 'There is no first char')
  return str[0].toLowerCase() + str.slice(1)
}

export const noop = () => {}

/**
 * @param {Object} tree - tree to traverse over
 * @param {string} keyPrefix - the tree path of the current child e.g '0-1-2-3'
 * @param {Function} onChild - called when a child is being traversed over
 * @returns {void}
 */
export function traverse (tree, keyPrefix, onChild) {
  onChild(tree, keyPrefix)
  tree.members.forEach((child, i) => {
    traverse(child, `${keyPrefix}-${i}`, onChild)
  })
}
