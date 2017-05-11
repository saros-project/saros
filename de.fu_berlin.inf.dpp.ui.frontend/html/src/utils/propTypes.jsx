import P from 'prop-types'

export const Account = P.shape({
  username: P.string,
  password: P.string,
  domain: P.string
})

export const Contact = P.shape({
  displayName: P.string.isRequired,
  presence: P.string,
  addition: P.string,
  jid: P.string
})
