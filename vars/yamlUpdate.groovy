def call(Map args = [:]) {
//    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    echo ("Im in the groovy**************")
    def inputManifest = readYaml(file: args.inputManifest)
    def outputFile = args.outputFile
    def components = args.componentName
    def componentsList = []
    echo("Components is $components")
    if (!components.isEmpty()) {
        echo ("Components parameter is not null")
        for (component in components.split(" ")) {
            componentsList.add(component.trim())
        }
    } else {
        echo ("Components parameter is null")
        inputManifest.components.each { component ->
            componentsList.add(component.name)
        }
    }
    echo (componentsList.toString())
    if (args.stage == "START") {
        inputManifest.build.status = "IN_PROGRESS"
        inputManifest.build.number = "${BUILD_NUMBER}"
        inputManifest.components.each { component ->
            if (componentsList.contains(component.name)) {
                component.status = "NOT_START"
                dir (component.name) {
                    checkout([$class: 'GitSCM', branches: [[name: component.ref]],
                              userRemoteConfigs: [[url: component.repository]]])
                    def commitID = sh (
                            script: "git rev-parse HEAD",
                            returnStdout: true
                    ).trim()
                    component.ref = commitID
                }
                component.x64_tar_status = "NOT_STARTED"
                component.x64_rpm_status = "NOT_STARTED"
                component.arm64_tar_status = "NOT_STARTED"
                component.arm64_rpm_status = "NOT_STARTED"
            }
        }
    }
        // x64_tar; x64_rpm; arm_tar; arm_rpm
    else if (args.stage == "IN_PROGRESS") {
        inputManifest.build.each { component ->
            component.status = "IN_PROGRESS"
        }
    }
//    else if (args.stage == "COMPLETE") {
//        inputManifest.build.status = "COMPLETED"
//        inputManifest.components.each { component ->
//            component.status = "COMPLETED"
//        }
//    }
    writeYaml(file: outputFile, data: inputManifest, overwrite: true)
    sh("yq -i $outputFile") //reformat the yaml
}