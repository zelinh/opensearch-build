def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.buildManifest))

    String opensearchCommitId = buildManifestObj.getCommitId("OpenSearch")
    echo "Commit ID for OpenSearch is $opensearchCommitId"

    def componentsName = buildManifestObj.getComponets()
    def componetsNumber = componentsName.size()
    def componets1 = componentsName.get(1)
    echo "There are $componetsNumber components in ths manifest"
/*    echo "The second component is called $componets1"
    def componets2 = componentsName.get(2)
    echo "The third component is called $componets2"*/
    sh """
        pwd
        ls $WORKSPACE
    """
    for (component in componentsName) {
        echo "The component name is $component"
        def commitID = buildManifestObj.getCommitId(component)
        echo "The commit ID for $component is $commitID"
        def repo = buildManifestObj.getRepo(component)
        echo "The URL for $component is $repo"

    }
}