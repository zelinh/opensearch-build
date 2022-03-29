/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */


import jenkins.tests.BuildPipelineTest
import org.junit.Before
import org.junit.Test

class TestvalidateMetaRPM extends BuildPipelineTest {

    @Before
    void setUp() {

        super.setUp()
        def out = "Name        : opensearch\n" +
                "Version     : 1.3.0\n" +
                "Release     : 1\n" +
                "Architecture: x86_64\n" +
                "Install Date: (not installed)\n" +
                "Group       : Application/Internet\n" +
                "Size        : 646503829\n" +
                "License     : Apache-2.0\n" +
                "Signature   : (none)\n" +
                "Source RPM  : opensearch-1.3.0-1.src.rpm\n" +
                "Build Date  : Wed Mar 23 22:10:17 2022\n" +
                "Build Host  : f8a4d27a00d9\n" +
                "Relocations : (not relocatable)\n" +
                "URL         : https://opensearch.org/\n" +
                "Summary     : An open source distributed and RESTful search engine\n" +
                "Description :\n" +
                "OpenSearch makes it easy to ingest, search, visualize, and analyze your data.\n" +
                "For more information, see: https://opensearch.org/"
        helper.addShMock("rpm -qip /Users/zelinhao/workplace/RPM dist/opensearch-1.3.0-linux-x64.rpm") { script ->
            return [stdout: out, exitValue: 0]
        }
    }

    @Test
    void testValidateMetaRPM() {
        super.testPipeline("tests/jenkins/jobs/ValidateMetaRPM_Jenkinsfile")
    }
}
