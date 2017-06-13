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

export const noop = () => {}
