from manifests.test_run_manifest import TestRunManifest

data = {
    "schema-version": "1.0",
    "name": "OpenSearch",
    "test-run": {
        "Command": "./test.sh integ-test manifests/2.6.0/opensearch-2.6.0-test.yml --paths opensearch=https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.6.0/7083/linux/x64/tar",
        "TestType": "integ-test",
        "TestManifest": "manifests/2.6.0/opensearch-2.6.0-test.yml",
        "DistributionManifest": "https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.6.0/7083/linux/x64/tar/dist/opensearch/manifest.yml",
        "TestID": "2345"
    },
    "components": [{
        "name": "sql",
        "command": "./test.sh integ-test manifests/2.6.0/opensearch-2.6.0-test.yml --component sql --paths opensearch=https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.6.0/7083/linux/x64/tar",
        "configs": [            # configs is a list of dict
            {
                "name": "with-security",
                "status": "pass",
                "logs": [
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/with-security/test-results/stderr.txt",
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/with-security/test-results/stdout.txt",
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/with-security/local-cluster-logs/stderr.txt",
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/with-security/local-cluster-logs/stdout.txt"
                ]
            },
            {
                "name": "without-security",
                "status": "fail",
                "logs": [
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/without-security/test-results/stderr.txt",
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/without-security/test-results/stdout.txt",
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/without-security/local-cluster-logs/stderr.txt",
                    "https://ci.opensearch.org/ci/dbc/integ-test/2.6.0/7083/linux/x64/tar/test-results/2345/integ-test/sql/without-security/local-cluster-logs/stdout.txt"
                ]
            }
        ]
    },
        # {
        #     "name": "anomaly-detection",
        #     "command": ".test.sh ADADAD",
        #     "configs": [            # configs is a list of dict
        #         {
        #             "name": "with-sec",
        #             "status": "pass",
        #             "logs": [
        #                 "log1AD",
        #                 "log2AD"
        #             ]
        #         },
        #         {
        #             "name": "without-sec",
        #             "status": "fail",
        #             "logs": [
        #                 "log1AD",
        #                 "log2AD"
        #             ]
        #         }
        #     ]
        # }
    ]
}

test_run_manifest = TestRunManifest(data)
output_dir = "/Users/zelinhao/workplace/fork/opensearch-build/test-run.yml"
test_run_manifest.to_file(output_dir)
# test_run_manifest1 = TestRunManifest.from_path(output_dir)
# print(test_run_manifest1.components.name)
