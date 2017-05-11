const { resolve } = require('path')

const ExtractTextPlugin = require('extract-text-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const webpack = require('webpack')

module.exports = (env = {}) => {
  const isProd = !!env.production

  const extractCss = new ExtractTextPlugin({
    filename: 'css/[name].css',
    disable: !isProd
  })

  return {
    context: resolve(__dirname, 'src'),
    entry: [
      './index.jsx'
    ],
    output: {
      path: resolve(__dirname, 'dist'),
      filename: 'js/bundle.js'
    },
    devtool: 'source-map',
    module: {
      loaders: [
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          loader: 'babel-loader',
          query: {
            presets: ['react', 'es2015', 'stage-0'],
            plugins: ['transform-decorators-legacy']
          }
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
        { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'file-loader' },
        { test: /\.(woff|woff2)$/, loader: 'url-loader?prefix=font/&limit=5000' },
        { test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader?limit=10000&mimetype=application/octet-stream' },
        { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url-loader?limit=10000&mimetype=image/svg+xml' }
      ]
    },
    plugins: [
      extractCss,
      // see https://github.com/jantimon/html-webpack-plugin
      // EJS template engine is used by default
      new HtmlWebpackPlugin({
        filename: 'main-page.html',
        template: 'template.ejs'
      })
      // Optimizing for Production
    ].concat(isProd ? [
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify('production')
      }),
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
      // This is why we can just import 'Utils'
      alias: {
        Utils: resolve(__dirname, 'src/utils/')
      }
    }
  }
}
