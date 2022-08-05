def call(Map args = [:]) {
//    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    echo ("Im in the groovy**************")
    def inputManifest = readYaml(file: args.inputManifest)
    def outputFile = args.outputFile
    def components = args.componentName
    def componentsList = []

    if (components != null) {
        componentsList = inputManifest.components
    } else {
        inputManifest.components.each { component ->
            echo (component)
            echo (component.name)
            echo (component.repository)
            //componentsList.add(component.name)
        }
    }
    //echo (componentsList.toString())
//    if (args.stage == "START") {
//        inputManifest.build.status = "IN_PROGRESS"
//        inputManifest.components.each { component ->
//            component.status = "NOT_START"
//        }
//    } else if (args.stage == "IN_PROGRESS") {
//        inputManifest.build.each { component ->
//            component.status = "IN_PROGRESS"
//        }
//    } else if (args.stage == "COMPLETE") {
//        inputManifest.build.status = "COMPLETED"
//        inputManifest.components.each { component ->
//            component.status = "COMPLETED"
//        }
//    }
//    writeYaml(file: outputFile, data: inputManifest, overwrite: true)
//    sh("yq -i $outputFile") //reformat the yaml
}