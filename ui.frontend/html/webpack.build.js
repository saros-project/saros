const devConfig = require('./webpack.dev')
module.exports = {
  ...devConfig,
  devtool: false,
  mode: 'production',
  watch: false
}
