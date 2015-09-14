# Install NPM
To run scripts with NPM you have to install NodeJS and NPM.
For more information about installing NodeJS and NPM see http://nodejs.org.
Before you are able to run the build tasks you have to navigate to
`de.fu_berlin.inf.dpp.ui.frontend/html/` and run `npm install` to download
and install dependencies.

# Development workflow
If you make any changes in a `.js` or `.jade` file you have to build the
JavaScript application in order to see the changes in action. The build
artifacts which are available to the Saros Java application are present in
`de.fu_berlin.inf.dpp.ui.frontend/html/dist/`.

# Build with NPM
Navigate to `de.fu_berlin.inf.dpp.ui.frontend/html/` and run `npm run build`.

## Tasks of the build process
 - compile JADE-templates to plain old JavaScript functions
(for more information about JADE visit http://jade-lang.com/)
 - creating a `bundle.js` which contains all internal JavaScript files,
external JavaScript frameworks and the template functions
 - copying all necessary resources (JavaScript, CSS, images, fonts)
to  `de.fu_berlin.inf.dpp.ui.frontend/html/dist/`

For the bundling of the JavaScript components Browserify is used, see
http://browserify.org/ for more information.

## Unit testing
To run unit tests navigate to `de.fu_berlin.inf.dpp.ui.frontend/html/` and run
`npm run test`. The tests are specified in
`de.fu_berlin.inf.dpp.ui.frontend/html/test`. For more information about the
test framework see http://mochajs.org/.

## Code auto-formattig
To auto-format the JavaScript code navigate to
`de.fu_berlin.inf.dpp.ui.frontend/html/` and run `npm run format`. The rules
for the auto-formatting can be defined in
`de.fu_berlin.inf.dpp.ui.frontend/html/.jscsrc`. For more information about
the configuration see http://jscs.info/.