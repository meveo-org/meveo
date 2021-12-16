var newman = require('newman');
var fs = require("fs");
const yargs = require('yargs');

const globalVarArray = [];
const globalVarArgs = yargs.argv.globalVar || [];

for (const v of globalVarArgs) {
    const split = v.split("=");
    globalVarArray.push({
        "key": split[0],
        "value": split[1],
        "enabled": true,
        "type": "text"
    })
}

const directory = yargs.argv.directory;
const envFile = yargs.argv.envFile;
const surefireReports = yargs.argv.surefireReports;

console.log("Environment file is set to : " + envFile + " (" + fs.existsSync(envFile) + ")\n");
console.log("Surefire report directory : " + surefireReports + "\n");

startTests(directory);

function startTests(collectionDir) {

    return new Promise( (resolve) => {
        fs.readdir("./" + collectionDir, function (errFile, files) {

            if (errFile) {
                console.log(errFile);
                throw errFile;
            }

            console.log("Starting tests placed in directory : " + collectionDir + "\n");
    
            const filesReducer = files.reduce(
                (oldPromise, file) => oldPromise.then(() => executeTest(collectionDir, file)),  // Execute tests one after the other
                Promise.resolve()                                                               // Initial value is a resolved promise             
            );
    
            filesReducer.then(
                () => resolve(),    // On success resolve
                () => resolve()      // On failure resolve
            );
        })
    });

}

function executeTest(baseDir, file) {

    const subDirOrFile = "./" + baseDir + "/" + file;

    try{

        if(fs.lstatSync(subDirOrFile).isDirectory()){
            return startTests(subDirOrFile);
        }

    }catch(e){
        console.log(e);
        process.exit(1);
    }

    const files = fs.readdirSync(baseDir);
    let dataFile = files.find(f => f.endsWith(".data.json"));
    if(dataFile){
        dataFile = baseDir + "/" + dataFile;
    }

    return new Promise((resolve) => {

        if(subDirOrFile.endsWith(".data.json")){
            resolve();
        }else{
            console.log("Execute postman collection : " + subDirOrFile + "\n");

            const testDirIndex = subDirOrFile.indexOf("tests/") + 6;
            const fileNameIndex = subDirOrFile.lastIndexOf("/");
            let testFileName = subDirOrFile.substring(testDirIndex, fileNameIndex);
            testFileName = testFileName.replace(/\//g,".")
    
            const testResultFile = surefireReports && (surefireReports + "/" + testFileName + ".xml");
            const options = {
                collection: require(subDirOrFile),
                insecure: true,
                reporters: ['cli', 'html', 'junit'],
                environment: envFile,
                iterationData: dataFile,
                globals: {
                    "id": "683ad527-9279-f62f-ff8d-663df9026ca8",
                    "name": "Postman Globals",
                    "values": globalVarArray,
                    "_postman_variable_scope": "globals"
                },
                bail: false,
                timeoutRequest: 30000
            }

            if(testResultFile) {
                options.reporter = {
                    junit: {
                        export: testResultFile
                    }
                };
            };

            newman.run(options, function (error, summary) {

                testResultFile && fs.readFile(testResultFile, "utf8", function (err, data) {
                    if (err) throw err;

                    console.log("Adjusting test suite " + testFileName + "\n");

                    const testSuitesRegex = new RegExp(`testsuites name="(.*)"`, "gm");
                    const testSuiteRegex = new RegExp(`testsuite name="(.*)"`,"gm");
    
                    let modifiedJunitFile = data.replace(testSuitesRegex, `testsuites name="${testFileName}"`)
                        .replace(testSuiteRegex, `testsuite name="${testFileName}.$1"`)
                        .replace(/testsuite (.*) classname=".*" (.*)/gm, "testsuite $1 $2")
                        .replace(/testcase (.*) classname=".*"/gm, "testcase $1");

                    while(modifiedJunitFile.includes("<error>")){
                        let startIndex = modifiedJunitFile.indexOf("<error>");
                        let endIndex = modifiedJunitFile.indexOf("</error>") + 8;
                        const errorTag = modifiedJunitFile.substring(startIndex, endIndex);
                        modifiedJunitFile = modifiedJunitFile.replace(errorTag, "");
                    }
                    fs.writeFile(testResultFile, modifiedJunitFile, () => {});
    
                });

                resolve();
            });
        }
    });
}