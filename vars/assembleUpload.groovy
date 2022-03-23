
void call(Map args = [:]) {

    lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))

    assembleManifest(args)
    echo "Finish assemble manifest*******"
    sh 'ls dist/opensearch/'
    uploadArtifacts(args)
    echo "Finish Uploading artifacts*******"
}