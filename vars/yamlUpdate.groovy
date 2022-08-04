def call(Map args = [:]) {
//    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    echo ("Im in the groovy**************")
    def inputManifest = readYaml(file: args.inputManifest)
    def outputFile = args.outputFile

    inputManifest.ci.status = "IN_PROGRESS"
    inputManifest.component.each { item ->
        item.status = "NOT_START"
    }
    writeYaml(file: outputFile, data: inputManifest)

}