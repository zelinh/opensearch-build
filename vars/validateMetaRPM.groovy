def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def buildManifestObj = lib.jenkins.BuildManifest.new(readYaml(file: args.distManifest))
    def distFile = args.rpmDistribution

    if (buildManifestObj.build.distribution != 'rpm') {
        error("Invalid distribution manifest. Please input the correct one.")
    }
    // the context the meta data should be
    def refMap = [:]
    refMap['Name'] = buildManifestObj.build.getFilename()   //opensearch; opensearch-dashboards
    refMap['Version'] = buildManifestObj.build.version        //1.3.0
    refMap['Architecture'] = buildManifestObj.build.architecture
    //refMap['Distribution'] = buildManifestObj.build.distribution
    //refMap['Release'] = 1
    //refMap['Platform'] = "linux" //Hard code the platform since it's assumed always linux for rpm
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
    def refFileName = [refMap["Name"], refMap["Version"], "linux", refMap["Architecture"]].join("-")
    assert distFileName == refFileName
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

    //Validation for the installation
    //install the rpm distribution via yum
    println("Start installation**************************************")
    sh "sudo yum install -y $distFile"
    println("RPM distribution is installed with yum.")

    //Check certs in /etc/opensearch/
    println("Check if the certs are existed.")
    sh ('[[ -d /etc/opensearch ]] && echo "/etc/opensearch directory exists"' +
            '|| (echo "/etc/opensearch does not exist" && exit 1)')
    def certs = sh (
            script: "ls /etc/opensearch",
            returnStdout: true
    ).trim()
    def requiredCerts = ["esnode-key.pem", "kirk.pem", "esnode.pem", "kirk-key.pem", "root-ca.pem"]
    requiredCerts.each {
        if (certs.contains(it)){
            println("$it is found existed")
        } else {
            error("Error finding $it certificate.")
        }
    }

    //Check the install_demo_configuration.log
//    sh ("cat /var/log/opensearch/install_demo_configuration.log")
//    sh ("cd /var/log/opensearch/ && ls")
    //def install_demo_configuration_log = readFile("/var/log/opensearch/install_demo_configuration.log")
    println("Checking the demo log**************")
//    println(fileExists('/var/log/opensearch/install_demo_configuration.log'))
    sh ('[[ -f /var/log/opensearch/install_demo_configuration.log ]] && echo "install_demo_configuration.log exists" ' +
            '|| (echo "install_demo_configuration.log does not exist" && exit 1)')
    def install_demo_configuration_log = sh (
            script: "cat /var/log/opensearch/install_demo_configuration.log",
            returnStdout: true
    ).trim()
    if (install_demo_configuration_log.contains("Success")) {
        println("install_demo_configuration.log validation succeed.!!!!!!")
    } else {
        println("install_demo_configuration.log failed.")
    }

    //Start the installed OpenSearch
    sh ("sudo systemctl restart opensearch")
    sleep 30    //wait for 30 secs for opensearch to start
    def running_status = sh (
            script: "sudo systemctl status $refMap['Name']",
            returnStdout: true
    ).trim()
    def active_status_message = "Active: active (running)"
    if (running_status.contains(active_status_message)) {
        println("Installed OpenSearch is actively running!")
    } else {
        error("Something went run! Installed OpenSearch is not actively running.")
    }

    //Check the starting cluster
    def cluster_info = sh (
            script:  "curl https://localhost:9200 -u admin:admin --insecure",
            returnStdout: true
    ).trim().replaceAll("\"", "").replaceAll(",", "")
    println("Cluster info is: " + cluster_info)
    for (line in cluster_info.split("\n")) {
        def key = line.split(":")[0].trim()
        if (key == "cluster_name") {
            assert line.split(":")[1].trim() == refMap['Name']
            println("Cluster name is validated.")
        } else if (key == "number") {
            assert line.split(":")[1].trim() == refMap['Version']
            println("Cluster version is validated.")
        } else if (key == "build_type") {
            assert line.split(":")[1].trim() == 'rpm'
            println("Cluster type is validated as rpm.")
        }
    }
    println("Cluster information is validated.")

    //Cluster status validation
    def cluster_status = sh (
            script:  "curl https://localhost:9200 -u admin:admin --insecure",
            returnStdout: true
    ).trim().replaceAll("\"", "").replaceAll(",", "")
    println("Cluster status is: " + cluster_status)
}
