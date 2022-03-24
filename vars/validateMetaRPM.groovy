def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.distManifest))
    def distFile = args.rpmDistribution

    // the context the meta data should be
    def refMap = [:]
    def name = buildManifestObj.build.getFilename()
    def version = buildManifestObj.build.version
    def architecture = buildManifestObj.build.architecture
    def distribution = buildManifestObj.build.distribution
    def Release = 1
    def group = "Application/Internet"
    def license = "Apache-2.0"
    def relocations = "(not relocatable)"
    def summary = "An open source distributed and RESTful search engine"
    def url = "https://opensearch.org/"
    def description = "OpenSearch makes it easy to ingest, search, visualize, and analyze your data.\n" +
            "For more information, see: https://opensearch.org/"

    def metadata = sh (
            script: "rpm -qip $distFile",
            returnStdout: true
    ).trim()
    echo "Print meta data ***************"
    println(metadata)
    echo "split to map"
    def metaMap = [:]
    def lines = metadata.split('\n')
    for (line in lines) {
        println line
        def key = line.split(':')[0].trim()
        if (key != 'Description') {
            metaMap[key] = line.split(':')[1].trim()
        } else {
            println 'description*********'
            metaMap[key] = metadata.split(line)[1].trim()
            break
        }
    }
    println metaMap
    assert name == metaMap['Name']
    assert version == metaMap['Version']
    if (architecture == 'x64') {        //up to change if naming confirmed
        assert metaMap['Architecture'] == 'x86_64'
    } else {
        assert metaMap['Architecture'] == 'aarch64'
    }
    assert group == metaMap['Group']
    assert license == metaMap['License']
    assert relocations == metaMap['Relocations']
    assert summary == metaMap['Summary']
    assert url == metaMap['URL']
    assert description == metaMap['Description']
    println 'everything is goodooooooood ********************'



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
