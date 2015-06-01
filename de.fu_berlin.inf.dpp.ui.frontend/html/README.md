# Development workflow
If you make any changes in a `.js` or `.jade` file you have to build the
JavaScript application in order to see the changes in action.

# Build with NPM
Navigate to `de.fu_berlin.inf.dpp.js/html/` and run `npm run build`.

## Tasks of the build process
 - compile JADE-templates to plain old JavaScript functions (for more
 information about JADE visit http://jade-lang.com/)
 - creating a `bundle.js` which contains all internal JavaScript files, external
 JavaScript frameworks and the template functions

More tasks are planned for the furture, for example unit testing and code
linting.

# Install NPM
To run scripts with NPM you have to install NodeJS and NPM.
For more information about installing NodeJS and NPM see nodejs.org.