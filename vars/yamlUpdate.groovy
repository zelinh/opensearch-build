void call(Map args = [:]) {
    lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    def inputManifest = readYaml(file: args.inputManifest)
    def outputFile = args.outputFile

    inputManifest.ci.name = "Temptempname"
    writeYaml(file: outputFile, data: inputManifest)

}