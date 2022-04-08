/**
 * This is a general function for RPM distribution validation.
 * @param Map args = [:]
 * args.bundleManifest: The Groovy Object of BundleManifest.
 * args.rpmDistribution: The location of the RPM distribution file.
 */
def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def bundleManifestObj = args.bundleManifestObj
    def distFile = args.rpmDistribution
    def name = bundleManifestObj.build.getFilename()   //opensearch
    def version = bundleManifestObj.build.version        //1.3.0
    def architecture = bundleManifestObj.build.architecture
    def plugin_names = bundleManifestObj.getNames();

    // This is a reference meta data which the distribution should be consistent with.
    def refMap = [:]
    refMap['Name'] = name
    refMap['Version'] = version
    refMap['Architecture'] = architecture
    refMap['Group'] = "Application/Internet"
    refMap['License'] = "Apache-2.0"
    refMap['Relocations'] = "(not relocatable)"
    refMap['URL'] = "https://opensearch.org/"
    // The context the meta data should be for OpenSearch
    refMap['Summary'] = "An open source distributed and RESTful search engine"
    refMap['Description'] = "OpenSearch makes it easy to ingest, search, visualize, and analyze your data.\n" +
            "For more information, see: https://opensearch.org/"

    rpmMetaValidation(
            rpmDistribution: distFile,
            refMap: refMap
    )

    //Validation for the installation
    //Install the rpm distribution via yum
    println("Start installation with yum.")
    sh ("sudo yum install -y $distFile")
    println("RPM distribution for $name is installed with yum.")

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
            error("Error fail to find $it certificate.")
        }
    }

    //Check the install_demo_configuration.log
    println("Start validating the install_demo_configuration.log.")
    sh ('[[ -f /var/log/opensearch/install_demo_configuration.log ]] && echo "install_demo_configuration.log exists" ' +
            '|| (echo "install_demo_configuration.log does not exist" && exit 1)')
    def install_demo_configuration_log = sh (
            script: "cat /var/log/opensearch/install_demo_configuration.log",
            returnStdout: true
    ).trim()
    if (install_demo_configuration_log.contains("Success")) {
        println("install_demo_configuration.log validation succeed.")
    } else {
        println("install_demo_configuration.log failed.")
    }

    //Start the installed OpenSearch distribution
    sh ("sudo systemctl restart $name")

    //Validate if the running status is succeed
    rpmStatusValidation(
            name: name
    )

    //Check the starting cluster
    def cluster_info_json = sh (
            script:  "curl -s \"https://localhost:9200\" -u admin:admin --insecure",
            returnStdout: true
    ).trim()
    println("Cluster info is: \n" + cluster_info_json)
    def cluster_info = readJson(Text: cluster_info_json)
    assert cluster_info["cluster_name"] == name
    println("Cluster name is validated.")
    assert cluster_info["version"]["number"] == version
    println("Cluster version is validated.")
    assert cluster_info["version"]["build_type"] == 'rpm'
    println("Cluster type is validated as rpm.")
    println("Cluster information is validated.")

    //Cluster status validation
    def cluster_status_json = sh (
            script:  "curl -s \"https://localhost:9200/_cluster/health?pretty\" -u admin:admin --insecure",
            returnStdout: true
    ).trim()
    def cluster_status = readJson(Text: cluster_status_json)
    println("Cluster status is: \n" + cluster_status)
    assert cluster_status["cluster_name"] == name
    println("Cluster name is validated.")
    assert cluster_status["status"] == "green"
    println("Cluster status is green!")

    //Check the cluster plugins
    def cluster_plugins = sh (
            script: "curl -s \"https://localhost:9200/_cat/plugins\" -u admin:admin --insecure",
            returnStdout: true
    ).trim().replaceAll("\"", "").replaceAll(",", "")
    println("Cluster plugins are: \n" + cluster_plugins)
    def components_list = []
    for (component in plugin_names) {
        if (component == "OpenSearch" || component == "common-utils") {
            continue
        }
        def location = bundleManifestObj.getLocation(component)
        def component_name_with_version = location.split('/').last().minus('.zip') //e.g. opensearch-job-scheduler-1.3.0.0
        components_list.add(component_name_with_version)
    }
    for (line in cluster_plugins.split("\n")) {
        def component_name = line.split("\\s+")[1].trim()
        def component_version = line.split("\\s+")[2].trim()
        assert components_list.contains([component_name,component_version].join('-'))
        println("Component $component_name is present with correct version $component_version." )
    }

    println("Installation and running for opensearch has been validated.")

    sh ("sudo systemctl stop opensearch")
    sh ("sudo yum remove -y opensearch")

}
