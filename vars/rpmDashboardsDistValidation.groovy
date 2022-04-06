/**
 * This is a general function for RPM distribution validation.
 * @param Map args = [:]
 * args.bundleManifestObj: The Groovy Object of BundleManifest.
 * args.rpmDistribution: The location of the RPM distribution file.
 */
def call(Map args = [:]) {

    def lib = library(identifier: 'jenkins@20211123', retriever: legacySCM(scm))
    def bundleManifestObj = args.bundleManifestObj
    def distFile = args.rpmDistribution
    def name = bundleManifestObj.build.getFilename()   //opensearch-dashboards
    def version = bundleManifestObj.build.version        //1.3.0
    def architecture = bundleManifestObj.build.architecture
    def plugin_names = bundleManifestObj.getNames();

    def latestOpenSearchURLRoot = "https://ci.opensearch.org/ci/dbc/Playground/tianleh-test/tianle-opensearch-build-3-22"
    def latestOpenSearchURL = "$latestOpenSearchURLRoot/$version/latest/linux/$architecture/rpm/dist/opensearch/opensearch-$version-linux-${architecture}.rpm"
    def latestOpensearchDist = "$WORKSPACE/opensearch-$version-linux-${architecture}.rpm"
    // Download the latest OpenSearch distribution with same version to be compatible with Dashboards.
    sh("curl -SLO $latestOpenSearchURL")

    // This is a reference meta data which the distribution should be consistent with.
    def refMap = [:]
    refMap['Name'] = name
    refMap['Version'] = version
    refMap['Architecture'] = architecture
    refMap['Group'] = "Application/Internet"
    refMap['License'] = "Apache-2.0"
    refMap['Relocations'] = "(not relocatable)"
    refMap['URL'] = "https://opensearch.org/"
    // The context of meta data should be for OSD
    refMap['Summary'] = "Open source visualization dashboards for OpenSearch"
    refMap['Description'] = "OpenSearch Dashboards is the visualization tool for data in OpenSearch\n" +
            "For more information, see: https://opensearch.org/"

    //Validation for the Meta Data of distribution
    rpmMetaValidation(
            rpmDistribution: distFile,
            refMap: refMap
    )

    //Validation for the installation
    //Install the rpm distribution via yum
    println("Start installation with yum.")
    sh "sudo yum install -y $distFile"
    println("RPM distribution for $name is installed with yum.")
    sh "sudo yum install -y $latestOpensearchDist"
    println("Latest RPM distribution for OpenSearch is also installed with yum.")

    //Start the installed OpenSearch-Dashboards distribution
    sh ("sudo systemctl restart $name")
    sleep 30
    Member
    dblock 2 hours ago

    Is there a better way to do this than just wait long enough? try to hit the endpoint in a loop in a waitForService [url] function? You could simplify both waiting for OS and OSD by just giving a URL and looking for certain text with a timeout.

    Member
    Author
    zelinh 1 hour ago

    I will investigate on this suggestion

    Reply...
    Resolve conversation
    sh ("sudo systemctl restart opensearch")
    sleep 30    //wait for 30 secs for opensearch to start

    //Validate if the running status is succeed
    rpmStatusValidation(
            name: name
    )

    //Start validate if this is dashboards distribution.
    println("This is a dashboards validation.")
    def osd_status = sh (
            script: "curl -s \"http://localhost:5601/api/status\"",
            returnStdout: true
    ).trim()
    println("Dashboards status are here: \n" + osd_status)
    def osd_status_json = readJSON(text: osd_status)
    assert osd_status_json["version"]["number"] == version
    println("Dashboards host version has been validated.")
    assert osd_status_json["status"]["overall"]["state"] == "green"
    println("OpenSearch Dashboards overall state is running green.")

    //Plugin existence validation;
    def osd_plugins = sh (
            script: "/usr/share/opensearch-dashboards/bin/opensearch-dashboards-plugin list",
            returnStdout: true
    ).trim()
    println("osd_plugins are: \n" + osd_plugins)
    def components_list = []
    for (component in plugin_names) {
        if (component == "OpenSearch-Dashboards" || component == "functionalTestDashboards") {
            continue
        }
        def location = bundleManifestObj.getLocation(component)
        def component_name_with_version = location.split('/').last().minus('.zip') //e.g. anomalyDetectionDashboards-1.3.0
        components_list.add(component_name_with_version)
    }
    for (component in components_list) {
        def component_with_version = component.replace("-","@") + ".0"  //e.g. anomalyDetectionDashboards@1.3.0.0
        assert osd_plugins.contains(component_with_version)
        println("Component $component is present with correct version $version." )
    }

    sh ("sudo systemctl stop opensearch-dashboards")
    sh ("sudo yum remove -y opensearch-dashboards")
    sh ("sudo systemctl stop opensearch")
    sh ("sudo yum remove -y opensearch")