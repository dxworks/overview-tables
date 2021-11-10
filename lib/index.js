const {tables} = require("./lib");
const {Command} = require("commander");

exports.tablesCommand = new Command()
    .name('tables')
    .description('Converts overview tables csv output to excel file')
    .allowUnknownOption()
    .action(tables)