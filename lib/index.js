import { Command } from 'commander'
import { tables, tablePics } from './lib.js'

export const tablesCommand = new Command()
    .name('tables')
    .description('Converts overview tables csv output to excel file')
    .allowUnknownOption()
    .action(tables)

export const tablePicsCommand = new Command()
    .name('table-pics')
    .alias('tablepics')
    .description('Converts html output of xlsx file to pictures')
    .argument('[dx-folder]', 'The dx home folder (containing settings.txt file)', process.cwd())
    .allowUnknownOption()
    .action(tablePics)
