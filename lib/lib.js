const {JavaCaller} = require('java-caller');

async function tables(options) {
  const java = new JavaCaller({
    jar: 'overview-tables.jar', // CLASSPATH referencing the package embedded jar files
    mainClass: 'org.dxworks.overviewtables.MainKt',// Main class to call, must be available from CLASSPATH,
    rootPath: __dirname,
    minimumJavaVersion: 11,
    output: 'console'
  });

  const args = [...process.argv];
  let index = args.indexOf('tables'); //if it is called from dxw cli
  if(index === -1)
    index = 1
  args.splice(0,  index + 1);
  const {status} = await java.run(args, {cwd: process.cwd()});
  process.exitCode = status;
}

module.exports = {tables}
