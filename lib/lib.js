const {JavaCaller} = require('java-caller')
const fs = require('fs')
const path = require('path')
const puppeteer = require('puppeteer')
const chalk = require('chalk')
const inquirer = require('inquirer')
const {parse} = require('node-html-parser')
const cliProgress = require('cli-progress');


async function tables() {
    const java = new JavaCaller({
        jar: 'overview-tables.jar', // CLASSPATH referencing the package embedded jar files
        mainClass: 'org.dxworks.overviewtables.MainKt',// Main class to call, must be available from CLASSPATH,
        rootPath: __dirname,
        minimumJavaVersion: 11,
        output: 'console'
    })

    const args = [...process.argv]
    let index = args.indexOf('tables') //if it is called from dxw cli
    if (index === -1)
        index = 1
    args.splice(0, index + 1)
    const {status} = await java.run(args, {cwd: process.cwd()})
    process.exitCode = status
}

async function tablePics(dxFolder) {
    if (!fs.existsSync(path.resolve(dxFolder, 'settings.txt'))) {
        console.log(`${path.normalize(dxFolder)} is not a dx home folder!`)
        process.exit(-1)
    }
    console.log(`Dx Project is ${chalk.cyan(path.resolve(dxFolder))}`)

    const presentationFolder = path.resolve(dxFolder, 'presentation')
    if (!fs.existsSync(presentationFolder) || !fs.statSync(presentationFolder).isDirectory()) {
        console.log(`Presentation folder ${path.normalize(presentationFolder)} does not exist, please first run the ${chalk.yellow('tables')} command!`)
        process.exit(-1)
    }

    const htmlFolders = fs.readdirSync(presentationFolder)
        .filter(f => fs.statSync(path.resolve(presentationFolder, f)).isDirectory())
        .filter(d => fs.existsSync(path.resolve(presentationFolder, d, 'stylesheet.css')))

    let inputFolder

    if(htmlFolders.length === 0) {
        console.log(`No Folder containing html export of the xlsx file was found. Please first export the excel workbook as a html page to the ${chalk.yellow('presentation')} folder and try again.`)
        process.exit(-1)
    }

    if(htmlFolders.length === 1)
        inputFolder = path.resolve(presentationFolder, htmlFolders[0])

    if (htmlFolders.length > 1) {
        const chosenFolder = (await inquirer
            .prompt([
                {
                    type: 'list',
                    name: 'htmlFolder',
                    message: 'Please choose which folder you would like to export',
                    choices: htmlFolders.map(it => path.basename(it)),
                    default: 0,
                }
            ])).htmlFolder
        inputFolder = path.resolve(presentationFolder, chosenFolder)
    }

    const outputFolder = path.resolve(presentationFolder, 'pictures')

    console.log(`Cleaning output folder ${chalk.cyan(outputFolder)}`)

    if (fs.existsSync(outputFolder)) {
        fs.rmSync(outputFolder, {recursive: true, force: true})
    }
    fs.mkdirSync(outputFolder)

    console.log('Preparing assets...')

    fs.appendFileSync(path.resolve(inputFolder, 'stylesheet.css'),
        'td {\n' +
        '  position: relative;\n' +
        '}\n' +
        'sup {\n' +
        '  position: absolute;\n' +
        '  top: 4px;\n' +
        '  left: 4px;\n' +
        '  font-size: 8pt;\n' +
        '}')

    console.log('Starting Puppeteer')
    const browser = await puppeteer.launch()
    const page = await browser.newPage()
    await page.setViewport({width: 5000, height: 2000})

    const progressBar = new cliProgress.SingleBar({}, cliProgress.Presets.shades_classic);
    progressBar.start(10, 0)
    for (let i = 1; i <= 10; i++) {

        progressBar.update(i)
        const fileNumber = i < 10 ? `0${i}` : `${i}`

        const file = `${inputFolder}/sheet0${fileNumber}.htm`

        const htmlContent = fs.readFileSync(file).toString()

        const root = parse(htmlContent)

        const scripts = root.getElementsByTagName('script')

        scripts[0]?.parentNode.removeChild(scripts[0])

        fs.writeFileSync(file, root.outerHTML)

        await page.goto(`file://${file}`, {waitUntil: 'networkidle2'})


        const table = await page.$('table')
        await table?.screenshot({path: `${outputFolder}/table-${i}.png`})
    }
    progressBar.stop()

    await browser.close()

    console.log('Done creating pictures of tables.')
    console.log(`Results are available at ${chalk.cyan(outputFolder)}`)

}

module.exports = {tables, tablePics}
