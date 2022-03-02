def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    //def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.buildManifest))
    echo "Im in the BUILD MAINFEST GROOVY 1ST CHECK**********"
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: "opensearch-build/jenkins/release-tag/manifest.yml"))
    echo "Im in the BUILD MAINFEST GROOVY 2ND CHECK**********"
    String opensearchCommitId = buildManifestObj.getCommitId("OpenSearch")
    echo "Im in the BUILD MAINFEST GROOVY 3rd CHECK**********"
    echo "Commit ID for OpenSearch is $opensearchCommitId"

    def componentsName = buildManifestObj.getNames()
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
                    git ls-remote --tags $repo $version > tags_list
                    cat tags_list
                    if [[ -n tags_list ]]; then
                        if [[ cat tags_list | awk 'NR==1{print \\\$1}' != $commitID ]]; then
                            echo "Tag $version already existed with a different commit ID. Please check this." 
                            exit 1
                        else
                            echo "Tag $version has been created with correct commit ID. Skipping creating for $component."
                        fi
                    else
                        git tag $version
                    fi
                else
                    git ls-remote --tags $repo $version.0 > tags_list
                    cat tags_list
                    if [[ -n tags_list ]]; then
                        if [[ cat tags_list | awk 'NR==1{print \\\\\\\$1}' != $commitID ]]; then
                            echo "Tag $version.0 already existed with a different commit ID. Please check this." 
                            exit 1
                        else
                            echo "Tag $version.0 has been created with correct commit ID. Skipping creating for $component."
                        fi
                    else
                        git tag $version.0
                    fi
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
//        if [[ -n $tags_list ]]; then
//        if [[ $tags_list | awk 'NR==1{print \$1}' != $commitID ]]; then
//        echo "Tag $version.0 already existed with a different commit ID. Please check this."
//        exit 1
//        else
//        echo "Tag $version.0 has been created with correct commit ID. Skipping creating for $component."
//        fi
//        else
//        git tag $version.0
//        fi
    }

}
