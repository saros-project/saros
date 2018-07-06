const nodeExternals = require('webpack-node-externals')
const config = require('./webpack.config')()
const webpack = require('webpack')
const { resolve } = require('path')

// We need this for enzyme to work with react 15.5+
config.resolve.alias['react-addons-test-utils'] = 'react-dom/test-utils'

const include = [
  resolve(__dirname, 'src'),
  resolve(__dirname, 'test'),
  resolve(__dirname, 'node_modules'),
]

// See https://www.npmjs.com/package/mocha-webpack
module.exports = {
  target: 'node', // in order to ignore built-in modules like path, fs, etc.
  externals: [nodeExternals()], // in order to ignore all modules in node_modules folder
  module: {
    rules: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules|\.tmp/,
        // We use babel for transpiling our scripts into old ES5 javascript
        use: 'babel-loader',
      },
      {
        test: /\.css$/,
        use: 'null-loader',
      },
    ],
  },
  resolve: config.resolve,
  plugins: [
    new webpack.ProvidePlugin({
      expect: ['chai', 'expect'],
    }),
  ],
}
