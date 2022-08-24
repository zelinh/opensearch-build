def call(Map args = [:]) {
    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    echo ("Im in the groovy**************")
    try {
        unstash "job_yml"
    } catch(Exception ex) {
        echo("No job.yml exists in stashed. Please make sure inputManifest parameter is passed.")
    }

    def inputManifest = args.inputManifest ?: "job.yml"
    def sourceyml = readYaml(file: inputManifest)
    def outputyml = args.outputyml ?: "job.yml"
    def components = args.componentName
    def componentsList = []
    def status = args.status
    echo("The status is $status")
    echo("Components is $components")
    if (!components.isEmpty()) {
        echo ("Components parameter is not null")
        for (component in components.split(" ")) {
            componentsList.add(component.trim())
        }
    } else {
        echo ("Components parameter is null")
        sourceyml.components.each { component ->
            componentsList.add(component.name)
        }
    }
    echo (componentsList.toString())

    if (args.stage == "START") {
        echo("Initiate the buildInfo yaml file.")
        sourceyml.build.status = "IN_PROGRESS"
        sourceyml.build.number = "${BUILD_NUMBER}"
        sourceyml.results = [:]


    } else if (args.stage == "COMPLETE") {
        sourceyml.components.each { component ->
            if (componentsList.contains(component.name)) {
                // Convert ref from branch to commit
//                dir(component.name) {
//                    checkout([$class           : 'GitSCM', branches: [[name: component.ref]],
//                              userRemoteConfigs: [[url: component.repository]]])
//                    def commitID = sh(
//                            script: "git rev-parse HEAD",
//                            returnStdout: true
//                    ).trim()
//                    component.ref = commitID
//                }
                sh ("wget https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.2.0/5905/linux/x64/tar/builds/opensearch/manifest.yml")
                def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: "$WORKSPACE/manifest.yml"))
                component.ref = buildManifestObj.getCommitId(component.name)
            }
        }
        sourceyml.build.status = status
    } else {
        stageField = args.stage
        echo("stage is $stageField")
        echo("status is $status")
        sourceyml.results.("$stageField".toString()) = "$status"
        sourceyml.results.duration = currentBuild.duration
        sourceyml.results.startTimestamp = currentBuild.startTimeInMillis
    }



//    if (args.stage == "START") {
//        echo("we are on the start stage.")
//        inputManifest.build.status = "IN_PROGRESS"
//        inputManifest.build.number = "${BUILD_NUMBER}"
//        inputManifest.results = [:]
//        echo("status is $status")
//        inputManifest.results.integ_test = status
//        inputManifest.results.bwc_test = status
//        inputManifest.results.x64_tar = status
//        inputManifest.results.arm64_tar = status
//        inputManifest.results.x64_rpm = status
//        inputManifest.results.arm64_rpm = status
//        inputManifest.components.each { component ->
//            if (componentsList.contains(component.name)) {
//                // Convert ref from branch to commit
//                dir (component.name) {
//                    checkout([$class: 'GitSCM', branches: [[name: component.ref]],
//                              userRemoteConfigs: [[url: component.repository]]])
//                    def commitID = sh (
//                            script: "git rev-parse HEAD",
//                            returnStdout: true
//                    ).trim()
//                    component.ref = commitID
//                }
//            }
//        }
//    }
//        // x64_tar; x64_rpm; arm_tar; arm_rpm
//    else if (args.stage == "x64_tar" || args.stage == "x64_rpm" || args.stage == "arm64_tar" || args.stage == "arm64_rpm") {
//        stageField = args.stage
//        echo("stage is $stageField")
//        echo("status is $status")
//        inputManifest.results.("$stageField".toString()) = "$status"
//    }
//    else if (args.stage == "integ_test" || args.stage == "bwc_test") {
//        stageField = args.stage
//        echo("stage is $stageField")
//        echo("status is $status")
//        inputManifest.results.("$stageField".toString()) = "$status"
//    }
//    else if (args.stage == "COMPLETE") {
//        inputManifest.build.status = status
//        inputManifest.results.duration = currentBuild.duration
//        inputManifest.results.startTimestamp = currentBuild.startTimeInMillis
//    }
    writeYaml(file: outputyml, data: sourceyml, overwrite: true)
//    sh("yq -i $outputFile") //reformat the yaml
    sh ("cat $outputyml")
    stash includes: "job.yml", name: "job_yml"
}

void updateCommit() {
    def distManifest = "https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.2.0/5905/linux/x64/tar/dist/opensearch/manifest.yml"
}