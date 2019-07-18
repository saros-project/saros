const { resolve } = require('path')
// This plugin is used for generating the .html files, based on the template.ejs file
const HtmlWebpackPlugin = require('html-webpack-plugin')
const { views } = require('./src/constants');

// see https://github.com/jantimon/html-webpack-plugin
// EJS template engine is used by default
const createPage = (name) => {
  return new HtmlWebpackPlugin({
    filename: `${name}.html`,
    title: name,
    template: 'template.ejs'
  })
}

module.exports = {
  context: resolve(__dirname, 'src'),
  entry: [
    // We need this polyfill so that native functions defined in JavaScript ES6 can be used
    'babel-polyfill/dist/polyfill.js',
    // The entry point of our application
    './index.jsx'
  ],
  output: {
    path: resolve(__dirname, 'dist'),
    filename: 'js/bundle.js'
  },
  devtool: 'source-map',
  mode: 'development',
  watch: true,
  module: {
    rules: [{
        test: /\.jsx?$/,
        exclude: /node_modules/,
        // We use babel for transpiling our scripts into old ES5 javascript
        use: 'babel-loader'
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.(jpe?g|png|ttf|eot|svg|woff(2)?)(\?[a-z0-9=&.]+)?$/,
        use: 'base64-inline-loader'
      }
    ]
  },
  plugins: [
    // TODO there should be a more elegant way of telling the application which view to show
    createPage(views.MAIN),
    createPage(views.ADD_CONTACT_PAGE),
    createPage(views.SHARE_PROJECT_PAGE),
    createPage(views.CONFIGURATION_PAGE),
    createPage(views.BASIC_WIDGET_TEST)
  ],
  resolve: {
    extensions: ['.js', '.jsx'],
    // This is why we can just import 'Utils' and 'Constants'
    alias: {
      '~': resolve(__dirname, 'src/'),
      Utils: resolve(__dirname, 'src/utils/'),
      Constants: resolve(__dirname, 'src/constants.js')
    }
  }
}
