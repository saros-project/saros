# Install Node.js and NPM
To run scripts with Yarn you have to install Node.js and NPM.
For more information about installing Node.js see http://nodejs.org.
Before you are able to run the build tasks you have to navigate to
`de.fu_berlin.inf.dpp.ui.frontend/html/` and run `npm install` to download
and install dependencies.

# Build with NPM
Navigate to `de.fu_berlin.inf.dpp.ui.frontend/html/` and run `npm run build`.

# Development workflow
If you make any changes to a file in the src folder you have to transpile the source code. For development run `npm run dev` and changes in the sources files are automatically detected and rebuilt. In order to build the production version run `npm run build`. The build artifacts which are available to the Saros Java application are present in
`de.fu_berlin.inf.dpp.ui.frontend/html/dist/`.



## Tasks of the build process
 - compile the JSX source code to JavaScript ES5
 - creating a `bundle.js` which contains all internal JavaScript files
and external JavaScript frameworks
 - copying all necessary resources (JavaScript, CSS, images, fonts)
to  `de.fu_berlin.inf.dpp.ui.frontend/html/dist/`

For the bundling of the JavaScript components Webpack is used, see
https://webpack.js.org/ for more information.

## Code linting
To see if your code fullfills the linting rules go to
`de.fu_berlin.inf.dpp.ui.frontend/html/` and run `npm run lint`.
In order to fix a majority of linting errors automatically use `npm run lint:fix`
The rules for linting are defined in
`de.fu_berlin.inf.dpp.ui.frontend/html/.eslintrc.yml`. For more information about
the configuration see http://eslint.org/
The JavaScript code uses the StandardJS linting rules
[![js-standard-style](https://cdn.rawgit.com/feross/standard/master/badge.svg)](https://github.com/feross/standard)
