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
        ls $WORKSPACE/builds/opensearch
    """
    withCredentials([usernamePassword(credentialsId: "release-tag-test-token", usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
        for (component in componentsName) {
            echo "The component name is $component"
            def commitID = buildManifestObj.getCommitId(component)
            echo "The commit ID for $component is $commitID"
            def repo = buildManifestObj.getRepo(component)
            echo "The URL for $component is $repo"

        }
        echo "Now doing the tag ******************************"
        def name="myownbuild"
        def commit_id="e19608bc0c17e249e5bab0182df6a5e2a9539f00"
        def ref="fix-cve"
        def repo='https://github.com/zelinh/opensearch-build.git'
        def version = "1.2.3"
        def push_url = "https://$GITHUB_TOKEN@github.com/zelinh/opensearch-build.git"
        sh """
            echo "Lets dooooo this"
            echo "Tagging $name at $commit_id ..."
            mkdir $name
            cd $name
            pwd
            git init
            git remote add origin $repo
            git fetch --depth 1 origin $commit_id
            git checkout FETCH_HEAD
            git tag $version.0
            git push push_url --tags
            cd ..
        """
    }

}