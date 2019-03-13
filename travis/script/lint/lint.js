const Path = require('path');

// process.argv[2] is the argument which the path to the folder of the project that needs linting
// it can be relative or absolute. if not given, use the current directory
let cwd = process.argv[2] || '.';
cwd = Path.isAbsolute(cwd) ? cwd : Path.join(__dirname, cwd);

// get the package.json from the target directory
const packageJson = require(Path.join(cwd, './package.json'));

let depObj = Object.keys(packageJson)
  // get all keys that describe dependencies
  .filter(name => name.match(/dependencies/i))
  // get the contents of the dependencies' objects
  .map(key => packageJson[key])
  // convert to one object with all dependencies
  .reduce((acc, cur) => Object.assign(acc, cur), {});

let lintDeps = Object.keys(depObj)
  // filter all linting dependencies' by name
  .filter(dep => dep.match(/lint|react/i))
  // convert to name1@version1 name2@version2 ...
  .map(name => name + '@' + depObj[name]).join(' ');


// install && lint!
const { execSync } = require('child_process');
const execConfig = { stdio: 'inherit', cwd };

let commandStr = `npm install ${lintDeps} --no-save --no-package-lock --no-audit`;
console.log('Running install command:\n', commandStr);
execSync(commandStr, execConfig);

console.log('Linting:');
execSync('npm run lint', execConfig);

process.exit(0);
