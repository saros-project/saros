var path = require('path');

module.exports = {
  mode: 'development',
  //devtool: "source-map",
  entry: [
    // We need this polyfill so that native functions defined in JavaScript ES5 can be used
    path.join(__dirname, 'node_modules/babel-polyfill/dist/polyfill.min.js'),
    path.join(__dirname, 'app.js')
  ],
  module: {
    rules: [{
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
      }, {
        test: /\.css$/,
        use: ['style-loader', 'css-loader']
      },
      {
        test: /\.(jpe?g|png|ttf|eot|svg|woff(2)?)(\?[a-z0-9=&.]+)?$/,
        use: 'base64-inline-loader'
      }
    ]
  },
  output: {
    path: __dirname + '/dist',
    filename: 'bundle.js'
  },
  resolve: {
    extensions: ['.js', '.jsx'],
    alias: {
      fabric: path.resolve(__dirname, './bower_components/fabric.js/dist/fabric.min.js')
    }
  }
}
