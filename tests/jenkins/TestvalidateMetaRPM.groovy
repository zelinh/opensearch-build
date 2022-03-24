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

    }

    @Test
    void testValidateMetaRPM() {
        super.testPipeline("tests/jenkins/jobs/ValidateMetaRPM_Jenkinsfile")
    }
}
