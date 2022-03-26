def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.distManifest))
    def distFile = args.rpmDistribution

    // the context the meta data should be
    def refMap = [:]
    refMap['Name'] = buildManifestObj.build.getFilename()
    refMap['Version'] = buildManifestObj.build.version
    refMap['Architecture'] = buildManifestObj.build.architecture
    //refMap['Distribution'] = buildManifestObj.build.distribution
    //refMap['Release'] = 1
    refMap['Platform'] = "linux" //Hard code the platform since it's assumed always linux for rpm
    refMap['Group'] = "Application/Internet"
    refMap['License'] = "Apache-2.0"
    refMap['Relocations'] = "(not relocatable)"
    refMap['Summary'] = "An open source distributed and RESTful search engine"
    refMap['URL'] = "https://opensearch.org/"
    refMap['Description'] = "OpenSearch makes it easy to ingest, search, visualize, and analyze your data.\n" +
            "For more information, see: https://opensearch.org/"

    println("Name convention for distribution file starts:")
    def distFileNameWithExtension = distFile.split('/').last()
    println("the File name is : $distFileNameWithExtension")        //opensearch-1.3.0-linux-x64.rpm
    if (!distFileNameWithExtension.endsWith(".rpm")) {
        error("This isn't a valid rpm distribution.")
    }
    def distFileName = distFileNameWithExtension.replace(".rpm", "")
    def fileNameMap = [:]
    fileNameMap['Name'] = distFileName.split('-')[0].trim()
    fileNameMap['Version'] = distFileName.split('-')[1].trim()
    fileNameMap['Platform'] = distFileName.split('-')[2].trim()
    fileNameMap['Architecture'] = distFileName.split('-')[3].trim()
    fileNameMap.each{ key, value ->
        assert refMap[key] == value
        println("File name for $key is validated.")
    }
    println("File name for the RPM distribution has been validated.")

    println("*******************************")
    println("Meta data validations start:")

    def metadata = sh (
            script: "rpm -qip $distFile",
            returnStdout: true
    ).trim()
    println("Meta data for the RPM distribution is: ")
    println(metadata)
    // Extract the meta data from the distribution to Map
    def metaMap = [:]
    def lines = metadata.split('\n')
    for (line in lines) {
        def key = line.split(':')[0].trim()
        if (key != 'Description') {
            metaMap[key] = line.split(':', 2)[1].trim()
        } else {
            metaMap[key] = metadata.split(line)[1].trim()
            break
        }
    }

    // Start validating
    refMap.each{ key, value ->
        if (key == "Architecture") {
            if (value == 'x64') {        //up to change if naming confirmed in the distribution manifest
                assert metaMap[key] == 'x86_64'
            } else {
                assert metaMap[key] == 'aarch64'
            }
        } else {
            assert metaMap[key] == value
        }
        println("Meta data for $key is validated")
    }
    println("Validation for meta data of RPM distribution completed.")

}
