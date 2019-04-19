const nodeExternals = require('webpack-node-externals')
const devConfig = require('./webpack.dev')

//remove some config params from the dev config
delete devConfig.plugins;

// configure Enzyme
const Enzyme = require('enzyme');
const Adapter = require('enzyme-adapter-react-16');
Enzyme.configure({
  adapter: new Adapter()
});


// See https://www.npmjs.com/package/mocha-webpack
module.exports = {
  ...devConfig,
  target: 'node', // in order to ignore built-in modules like path, fs, etc.
  externals: [nodeExternals()], // in order to ignore all modules in node_modules folder
}
