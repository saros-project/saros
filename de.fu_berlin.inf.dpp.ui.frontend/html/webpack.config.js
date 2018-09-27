const { resolve } = require('path')

// This plugin is used for bundling all css files being imported into one css bundle
// (Only in production mode)
const ExtractTextPlugin = require('extract-text-webpack-plugin')
// This plugin is used for generating the .html files, based on the template.ejs file
const HtmlWebpackPlugin = require('html-webpack-plugin')
const webpack = require('webpack')

// see https://github.com/jantimon/html-webpack-plugin
// EJS template engine is used by default
const createPage = (name) => {
  return new HtmlWebpackPlugin({
    filename: `${name}.html`,
    title: name,
    template: 'template.ejs'
  })
}

module.exports = (env = {}) => {
  // In production mode we do some extra code optimizations
  const isProd = !!env.production

  const extractCss = new ExtractTextPlugin({
    filename: 'css/[name].[hash].css',
    disable: !isProd
  })

  return {
    context: resolve(__dirname, 'src'),
    entry: [
      // We need this polyfill so that native functions defined in JavaScript ES6 can be used
      'babel-polyfill/dist/polyfill.js',
      // The entry point of our application
      './index.jsx'
    ],
    output: {
      path: resolve(__dirname, 'dist'),
      // We need the [hash] for cache breaking
      filename: 'js/bundle.[hash].js'
    },
    devtool: 'source-map',
    module: {
      rules: [
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          // We use babel for transpiling our scripts into old ES5 javascript
          use: 'babel-loader'
        },
        // CSS loader as described in https://github.com/webpack-contrib/sass-loader
        // Modified to only work with plain css
        {
          test: /\.css$/,
          use: extractCss.extract({
            use: [
                { loader: 'css-loader' }
            ],
              // use style-loader in development
            fallback: 'style-loader'
          })
        },
        // Those loaders are for using the bootstrap node package
        { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, use: 'file-loader?name=assets/[name].[ext]' },
        { test: /\.(woff|woff2)$/, use: 'url-loader?prefix=font/&limit=5000&name=assets/[name].[ext]' },
        { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, use: 'url-loader?limit=10000&mimetype=application/octet-stream&name=assets/[name].[ext]' },
        { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, use: 'url-loader?limit=10000&mimetype=image/svg+xml&name=assets/[name].[ext]' }
      ]
    },
    plugins: [
      extractCss,
      // TODO there should be a more elegant way of telling the application which view to show
      createPage('main-page'),
      createPage('start-session-wizard'),
      createPage('configuration-page'),
      createPage('basic-widget-test')
    ]
    .concat(isProd ? [
      // In Production Mode the global variable process.env.NODE_ENV is set to production
      // Causing some code to be optimized away
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify('production')
      }),
      // This optimizes and minifies the javascript code
      new webpack.optimize.UglifyJsPlugin({
        compress: {
          warnings: false,
          drop_console: true
        },
        mangle: true
      })
    ] : []),
    resolve: {
      extensions: ['.js', '.jsx'],
      // This is why we can just import 'Utils' and 'Constants'
      alias: {
        '~': resolve(__dirname, 'src/'),
        Utils: resolve(__dirname, 'src/utils/'),
        Constants: resolve(__dirname, 'src/constants.jsx')
      }
    }
  }
}
