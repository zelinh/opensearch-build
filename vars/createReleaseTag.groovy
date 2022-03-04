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
            dir (component) {
                sh 'pwd && ls'
                checkout([$class: 'GitSCM', branches: [[name: commitID ]],
                          userRemoteConfigs: [[url: repo]]])
                sh 'git status'
                if ( component == "OpenSearch" ) {
                    def tag_id = sh (
                            script: "git ls-remote --tags $repo $version | awk 'NR==1{print \$1}'",
                            returnStdout: true
                    ).trim()
                    if (tag_id == null)  {
                        sh "git tag $version"
                        sh "git push $push_url $version"
                    } else if (tag_id == commitID) {
                        echo "Tag $version has been created with identical commit ID. Skipping creating new tag for $component."
                    } else {
                        error "Tag $version already existed with a different commit ID. Please check this."
                    }
                } else {
                    def tag_id = sh (
                            script: "git ls-remote --tags $repo $version.0 | awk 'NR==1{print \$1}'",
                            returnStdout: true
                    ).trim()
                    echo "$tag_id"
                    if (tag_id == null) {
                        sh "git tag $version.0"
                        sh "git push $push_url $version.0"
                    } else if (tag_id == commitID) {
                        echo "Tag $version.0 has been created with identical commit ID. Skipping creating new tag for $component."
                    } else {
                        error "Tag $version.0 already existed with a different commit ID. Please check this."
                    }
                }

            }
            sh 'pwd'

        }

        def name="myownbuild"
        def commit_id="e19608bc0c17e249e5bab0182df6a5e2a9539f00"
        def ref="fix-cve"
        def repo='https://github.com/zelinh/opensearch-build.git'
        //def version = "1.2.3"
        def push_url = "https://$GITHUB_TOKEN@" + repo.minus('https://')

//        sh """
//                mkdir $component
//                cd $component
//                git init
//                git remote add origin $repo
//                git fetch --depth 1 origin $commitID
//                git checkout FETCH_HEAD
//                if [ "$component" == "OpenSearch" ]; then
//                    if [[ -n \$(git ls-remote --tags $repo $version) ]]; then
//                        tag_id=\$(git ls-remote --tags $repo $version | awk 'NR==1{print \$1}')
//                        if [[ \${tag_id} != $commitID ]]; then
//                            echo "Tag $version already existed with a different commit ID. Please check this."
//                            exit 1
//                        else
//                            echo "Tag $version has been created with identical commit ID. Skipping creating new tag for $component."
//                        fi
//                    else
//                        git tag $version
//                    fi
//                else
//                    if [[ -n \$(git ls-remote --tags $repo $version.0) ]]; then
//                        tag_id=\$(git ls-remote --tags $repo $version.0 | awk 'NR==1{print \$1}')
//                        if [[ \${tag_id} != $commitID ]]; then
//                            echo "Tag $version.0 already existed with a different commit ID. Please check this."
//                            exit 1
//                        else
//                            echo "Tag $version.0 has been created with identical commit ID. Skipping creating new tag for $component."
//                        fi
//                    else
//                        git tag $version.0
//                    fi
//                fi
//                git push $push_url --tags
//                cd ..
//            """

    }

}
