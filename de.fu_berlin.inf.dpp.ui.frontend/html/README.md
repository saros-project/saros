# Development workflow
If you make any changes in a `.js` or `.jade` file you have to build the
JavaScript application in order to see the changes in action.

# Build with Gulp
Navigate to `de.fu_berlin.inf.dpp.js/html/` and run `npm run build` to build
once or run `npm run build-watch` to start a watch-task which will build
everytime you touch a `.js` or `.jade` file in this project.

## Tasks of the build process
 - compile JADE-templates to plain old JavaScript functions (for more
 information about JADE visit http://jade-lang.com/)
 - creating a `bundle.js` which contains all internal JavaScript files, external
 JavaScript frameworks and the template functions

More tasks are planned for the furture, for example unit testing and code
linting.

# Install Gulp
Gulp depends on NodeJS and its package manager NPM.
For information about installing NodeJS see nodejs.org. When you have installed
NodeJS and NPM you can prepare the build process like follows:

1.  Navigate to "de.fu_berlin.inf.dpp.ui/resources/html/" and run "npm install"
    to install all node modules necessary for the specified Gulp tasks
    (dependencies are managed in package.json, tasks are specified in
    gulpfile.js). For more information about Gulp visit http://gulpjs.com/.
2.  You can build like described in the section "Build with Gulp"
