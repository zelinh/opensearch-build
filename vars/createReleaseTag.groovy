def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    //def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.buildManifest))
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: "opensearch-build/jenkins/release-tag/manifest.yml"))
    String opensearchCommitId = buildManifestObj.getCommitId("OpenSearch")
    echo "Commit ID for OpenSearch is $opensearchCommitId"

    def componentsName = buildManifestObj.getComponents()
    def componetsNumber = componentsName.size()
    def componets1 = componentsName.get(1)
    echo "There are $componetsNumber components in ths manifest"
    echo "The second component is called $componets1"
    def componets2 = componentsName.get(2)
    echo "The third component is called $componets2"
    def version = args.tagVersion
    sh """
        pwd
        ls $WORKSPACE/opensearch-build/jenkins/release-tag
    """
    withCredentials([usernamePassword(credentialsId: "release-tag-test-token", usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
        echo "Now doing the tag ******************************"
        for (component in componentsName) {
            echo "The component name is $component"
            def commitID = buildManifestObj.getCommitId(component)
            echo "The commit ID for $component is $commitID"
            def repo = buildManifestObj.getRepo(component)
            def push_url = "https://$GITHUB_TOKEN@" + repo.minus('https://')
            echo "The URL for $component is $repo"
            sh """
                echo "Tagging $component at $commitID ..."
                mkdir $component
                cd $component
                pwd
                git init
                git remote add origin $repo
                git fetch --depth 1 origin $commitID
                git checkout FETCH_HEAD
                if [ "$component" == "OpenSearch" ]; then
                    git tag $version
                else
                    git tag $version.0
                fi
                git push $push_url --tags
                cd ..
            """
        }

        def name="myownbuild"
        def commit_id="e19608bc0c17e249e5bab0182df6a5e2a9539f00"
        def ref="fix-cve"
        def repo='https://github.com/zelinh/opensearch-build.git'
        //def version = "1.2.3"
        def push_url = "https://$GITHUB_TOKEN@" + repo.minus('https://')

    }

}
