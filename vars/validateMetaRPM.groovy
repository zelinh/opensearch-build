def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.distManifest))
    def distFile = args.rpmDistribution

    // the context the meta data should be
    def name = buildManifestObj.build.name
    def version = buildManifestObj.build.version
    def architecture = buildManifestObj.build.architecture
    def distribution = buildManifestObj.build.distribution
    def Release = 1
    def group = "Application/Internet"
    def license = "Apache-2.0"
    def relocation = "(not relocatable)"

    sh ("rpm -qip $distFile >> metadata.txt")
    sh ("cat metadata.txt")
    def metadata =[:]
    new File("metadata.txt").eachLine{line->
        if(line.contains(':')){
            metadata[line.split(':')[0]]=line.split(':')[1]
        }
    }
    println metadata


//    withCredentials([usernamePassword(credentialsId: "${GITHUB_BOT_TOKEN_NAME}", usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
//        for (component in componentsName) {
//            def commitID = buildManifestObj.getCommitId(component)
//            def repo = buildManifestObj.getRepo(component)
//            def push_url = "https://$GITHUB_TOKEN@" + repo.minus('https://')
//            echo "Tagging $component at $commitID ..."
//
//            dir (component) {
//                checkout([$class: 'GitSCM', branches: [[name: commitID]],
//                          userRemoteConfigs: [[url: repo]]])
//                def tagVersion = "$version.0"
//                if (component == "OpenSearch" || component == "OpenSearch-Dashboards" || component == "functionalTestDashboards") {
//                    tagVersion = version
//                }
//                def tag_id = sh (
//                        script: "git ls-remote --tags $repo $tagVersion | awk 'NR==1{print \$1}'",
//                        returnStdout: true
//                ).trim()
//                if (tag_id == "") {
//                    echo "Creating $tagVersion tag for $component"
//                    sh "git tag $tagVersion"
//                    sh "git push $push_url $tagVersion"
//                } else if (tag_id == commitID) {
//                    echo "Tag $tagVersion has been created with identical commit ID. Skipping creating new tag for $component."
//                } else {
//                    error "Tag $tagVersion already existed in $component with a different commit ID. Please check this."
//                }
//            }
//        }
//    }
}
