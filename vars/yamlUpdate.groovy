def call(Map args = [:]) {
//    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    echo ("Im in the groovy**************")
    def inputManifest = readYaml(file: args.inputManifest)
    def outputFile = args.outputFile

    inputManifest.ci.name = "Temptempname"
    writeYaml(file: outputFile, data: inputManifest)

}