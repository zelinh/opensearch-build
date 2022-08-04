def call(Map args = [:]) {
//    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    echo ("Im in the groovy**************")
    def inputManifest = readYaml(file: args.inputManifest)
    def outputFile = args.outputFile

    if (args.stage == "START") {
        inputManifest.ci.status = "IN_PROGRESS"
        inputManifest.components.each { component ->
            component.status = "NOT_START"
        }
    } else if (args.stage == "IN_PROGRESS") {
        inputManifest.components.each { component ->
            component.status = "IN_PROGRESS"
        }
    } else if (args.stage == "COMPLETE") {
        inputManifest.ci.status = "COMPLETED"
        inputManifest.components.each { component ->
            component.status = "COMPLETED"
        }
    }
    writeYaml(file: outputFile, data: inputManifest, overwrite: true)
    sh("yq -i $outputFile")
}