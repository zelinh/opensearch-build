def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def DistributionManifestObj = lib.jenkins.DistributionManifest.new(readYaml(file: args.distManifest))
    def distFile = args.rpmDistribution
    def name = DistributionManifestObj.build.getFilename()   //opensearch; opensearch-dashboards
    def version = DistributionManifestObj.build.version        //1.3.0
    def architecture = DistributionManifestObj.build.architecture
    def plugin_names = DistributionManifestObj.getNames();
    def latestOpenSearchURL = "https://ci.opensearch.org/ci/dbc/Playground/tianleh-test/tianle-opensearch-build-3-22/1.3.1/latest/linux/$architecture/rpm/dist/opensearch/opensearch-$version-linux-${architecture}.rpm"
    def latestOpensearchDist = "$WORKSPACE/opensearch-$version-linux-${architecture}.rpm"

    if (name == "opensearch-dashboards") {
        sh("curl -SLO $latestOpenSearchURL")
    }
    sh("pwd")
    sh("ls")
    sh("yum install $latestOpensearchDist")


//
//    if (DistributionManifestObj.build.distribution != 'rpm') {
//        error("Invalid distribution manifest. Please input the correct one.")
//    }
//
//    def refMap = [:]
//    refMap['Name'] = name
//    refMap['Version'] = version
//    refMap['Architecture'] = architecture
//    //refMap['Distribution'] = DistributionManifestObj.build.distribution
//    //refMap['Release'] = 1
//    //refMap['Platform'] = "linux" //Hard code the platform since it's assumed always linux for rpm
//    refMap['Group'] = "Application/Internet"
//    refMap['License'] = "Apache-2.0"
//    refMap['Relocations'] = "(not relocatable)"
//    refMap['URL'] = "https://opensearch.org/"
//    switch (name) {
//        case "opensearch":
//            // the context the meta data should be for OpenSearch
//            refMap['Summary'] = "An open source distributed and RESTful search engine"
//            refMap['Description'] = "OpenSearch makes it easy to ingest, search, visualize, and analyze your data.\n" +
//                    "For more information, see: https://opensearch.org/"
//            break
//        case "opensearch-dashboards":
//            // The context of meta data should be for OSD
//            refMap['Summary'] = "Open source visualization dashboards for OpenSearch"
//            refMap['Description'] = "OpenSearch Dashboards is the visualization tool for data in OpenSearch\n" +
//                    "For more information, see: https://opensearch.org/"
//    }
//
//    //Validation for the Name convention
//    println("Name convention for distribution file starts:")
//    def distFileNameWithExtension = distFile.split('/').last()
//    println("the File name is : $distFileNameWithExtension")        //opensearch-1.3.0-linux-x64.rpm
//    if (!distFileNameWithExtension.endsWith(".rpm")) {
//        error("This isn't a valid rpm distribution.")
//    }
//    def distFileName = distFileNameWithExtension.replace(".rpm", "")
//    def refFileName = [refMap["Name"], refMap["Version"], "linux", refMap["Architecture"]].join("-")
//    assert distFileName == refFileName
//    println("File name for the RPM distribution has been validated.")
//
//    //Validation for the Meta Data of distribution
//    println("*******************************")
//    println("Meta data validations start:")
//    def metadata = sh (
//            script: "rpm -qip $distFile",
//            returnStdout: true
//    ).trim()
//    println("Meta data for the RPM distribution is: ")
//    println(metadata)
//    // Extract the meta data from the distribution to Map
//    def metaMap = [:]
//    def lines = metadata.split('\n')
//    for (line in lines) {
//        def key = line.split(':')[0].trim()
//        if (key != 'Description') {
//            metaMap[key] = line.split(':', 2)[1].trim()
//        } else {
//            metaMap[key] = metadata.split(line)[1].trim()
//            break
//        }
//    }
//
//    // Start validating
//    refMap.each{ key, value ->
//        if (key == "Architecture") {
//            if (value == 'x64') {        //up to change if naming confirmed in the distribution manifest
//                assert metaMap[key] == 'x86_64'
//            } else if (value == 'arm64') {
//                assert metaMap[key] == 'aarch64'
//            }
//        } else {
//            assert metaMap[key] == value
//        }
//        println("Meta data for $key is validated")
//    }
//    println("Validation for meta data of RPM distribution completed.")
//
//    //Validation for the installation
//    //install the rpm distribution via yum
//    println("Start installation**************************************")
//    sh "sudo yum install -y $distFile"
//    println("RPM distribution for $name is installed with yum.")
//    if (name == "opensearch-dashboards") {
//        sh "sudo yum install -y $latestOpensearchDist"
//        println("Latest RPM distribution for OpenSearch is also installed with yum.")
//    }
//
//    if (name == "opensearch") {
//        // The validation for opensearch only.
//        //Check certs in /etc/opensearch/
//        println("Check if the certs are existed.")
//        sh ('[[ -d /etc/opensearch ]] && echo "/etc/opensearch directory exists"' +
//                '|| (echo "/etc/opensearch does not exist" && exit 1)')
//        def certs = sh (
//                script: "ls /etc/opensearch",
//                returnStdout: true
//        ).trim()
//        def requiredCerts = ["esnode-key.pem", "kirk.pem", "esnode.pem", "kirk-key.pem", "root-ca.pem"]
//        requiredCerts.each {
//            if (certs.contains(it)){
//                println("$it is found existed")
//            } else {
//                error("Error finding $it certificate.")
//            }
//        }
//        //Check the install_demo_configuration.log
//        println("Checking the demo log**************")
//        sh ('[[ -f /var/log/opensearch/install_demo_configuration.log ]] && echo "install_demo_configuration.log exists" ' +
//                '|| (echo "install_demo_configuration.log does not exist" && exit 1)')
//        def install_demo_configuration_log = sh (
//                script: "cat /var/log/opensearch/install_demo_configuration.log",
//                returnStdout: true
//        ).trim()
//        if (install_demo_configuration_log.contains("Success")) {
//            println("install_demo_configuration.log validation succeed.!!!!!!")
//        } else {
//            println("install_demo_configuration.log failed.")
//        }
//    }
//
//    //Start the installed OpenSearch/OpenSearch-Dashboards distribution
//    sh ("sudo systemctl restart $name")
//    sleep 30    // We will need to start OpenSearch no matter if we are validating for opensearch or OSD
//    sh ("sudo systemctl restart opensearch")
//    sleep 30    //wait for 30 secs for opensearch to start
//    def running_status = sh (
//            script: "sudo systemctl status $name",
//            returnStdout: true
//    ).trim()
//    def active_status_message = "Active: active (running)"
//    if (running_status.contains(active_status_message)) {
//        println("After checking the status, the installed $name is actively running!")
//    } else {
//        error("Something went run! Installed $name is not actively running.")
//    }
//
//    if (name == "opensearch") {
//        //Check the starting cluster
//        def cluster_info = sh (
//                script:  "curl -s \"https://localhost:9200\" -u admin:admin --insecure",
//                returnStdout: true
//        ).trim().replaceAll("\"", "").replaceAll(",", "")
//        println("Cluster info is: " + cluster_info)
//        for (line in cluster_info.split("\n")) {
//            def key = line.split(":")[0].trim()
//            if (key == "cluster_name") {
//                assert line.split(":")[1].trim() == name
//                println("Cluster name is validated.")
//            } else if (key == "number") {
//                assert line.split(":")[1].trim() == version
//                println("Cluster version is validated.")
//            } else if (key == "build_type") {
//                assert line.split(":")[1].trim() == 'rpm'
//                println("Cluster type is validated as rpm.")
//            }
//        }
//        println("Cluster information is validated.")
//
//        //Cluster status validation
//        def cluster_status = sh (
//                script:  "curl -s \"https://localhost:9200/_cluster/health?pretty\" -u admin:admin --insecure",
//                returnStdout: true
//        ).trim().replaceAll("\"", "").replaceAll(",", "")
//        println("Cluster status is: " + cluster_status)
//        for (line in cluster_status.split("\n")) {
//            def key = line.split(":")[0].trim()
//            if (key == "cluster_name") {
//                assert line.split(":")[1].trim() == name
//                println("Cluster name is validated.")
//            } else if (key == "status") {
//                assert line.split(":")[1].trim() == "green"
//                println("Cluster status is green!")
//            }
//        }
//
//        //Check the cluster
//        sh ("curl -s \"https://localhost:9200/_cat/plugins?v\" -u admin:admin --insecure")
////    name                                              component                            version
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-alerting                  1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-anomaly-detection         1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-asynchronous-search       1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-cross-cluster-replication 1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-index-management          1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-job-scheduler             1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-knn                       1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-ml                        1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-observability             1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-performance-analyzer      1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-reports-scheduler         1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-security                  1.3.0.0
////    dev-dsk-zhujiaxi-2a-5c9b3e5e.us-west-2.amazon.com opensearch-sql                       1.3.0.0
//        def cluster_plugins = sh (
//                script: "curl -s \"https://localhost:9200/_cat/plugins?v\" -u admin:admin --insecure",
//                returnStdout: true
//        ).trim().replaceAll("\"", "").replaceAll(",", "")
//        println("Cluster plugins are: " + cluster_plugins)
//        def components_list = []
//        for (component in plugin_names) {
//            if (component == "OpenSearch" || component == "common-utils") {
//                continue
//            }
//            def location = DistributionManifestObj.getLocation(component)
//            println(location)
//            def component_name_with_version = location.split('/').last().minus('.zip')
//            println(component_name_with_version)
//            components_list.add(component_name_with_version)
//        }
//        for (line in cluster_plugins.split("\n").drop(1)) {
//            def component_name = line.split("\\s+")[1].trim()
//            def component_version = line.split("\\s+")[2].trim()
//            assert components_list.contains([component_name,component_version].join('-'))
//            println("Component $component_name is present with correct version $component_version." )
//        }
//
//        println("Installation and running for opensearch has been validated.")
//    } else {    //validating cluster for opensearch-dashboards
//        //Start validate if this is dashboards distribution.
//        //curl -s http://localhost:5601/api/status
//        println("This is a dashboards validation**********************")
//        def osd_status = sh (
//                script: "curl -s \"http://localhost:5601/api/status\"",
//                returnStdout: true
//        ).trim()
//        println("Dashboards nodes are here: $osd_status")
//        println("-------------------------------------------------------------------------------------------------------")
//        def osd_status_json = readJSON(text: osd_status)
//        assert osd_status_json["version"]["number"] == version
//        println("Dashboards host version has been validated.")
//        assert osd_status_json["status"]["overall"]["state"] == "green"
//        println("OpenSearch Dashboards overall state is running in green.")
//
//        //Plugin existence validation;
//        def osd_plugins = sh (
//                script: "cd /usr/share/opensearch-dashboards/bin\n" +
//                        "./opensearch-dashboards-plugin list",
//                returnStdout: true
//        ).trim()
//        println("osd_plugins are: $osd_plugins")
//        def components_list = []
//        for (component in plugin_names) {
//            if (component == "OpenSearch-Dashboards" || component == "functionalTestDashboards") {
//                continue
//            }
//            def location = DistributionManifestObj.getLocation(component)
//            println(location)
//            def component_name_with_version = location.split('/').last().minus('.zip')
//            println(component_name_with_version)
//            components_list.add(component_name_with_version)
//        }
//        for (component in components_list) {
//            def component_with_version = component.replace("-","@") + ".0"
//            assert osd_plugins.contains(component_with_version)
//            println("Component $component is present with correct version $version." )
//        }
//    }

}
