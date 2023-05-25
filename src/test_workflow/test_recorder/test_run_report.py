# Copyright OpenSearch Contributors
# SPDX-License-Identifier: Apache-2.0
#
# The OpenSearch Contributors require contributions made to
# this file be licensed under the Apache-2.0 license or a
# compatible open source license.

import logging
import os
import shutil
from typing import Any

import yaml

import paths
from test_workflow.test_recorder.test_result_data import TestResultData
from manifests.test_manifest import TestManifest,TestComponent
from manifests.test_run_manifest import TestRunManifest,TestComponent


class TestRunReport:
    test_run_id: str
    test_type: str
    test_product: str
    location: str
    test_manifest_path: str
    test_manifest: TestManifest
    distribution_manifest: str
    base_path: str
    repo_dir: str
    test_run_manifest: TestRunManifest
    test_run_dict: dict

    def __init__(self, test_run_id: str, test_type: str, test_product: str, test_manifest_path: str, base_path: str, repo_dir: str) -> None:
        self.test_run_id = test_run_id
        self.test_type = test_type
        self.test_product = test_product
        self.test_manifest_path = test_manifest_path
        self.test_manifest = TestManifest.from_path(test_manifest_path)
        self.base_path = base_path
        self.repo_dir = repo_dir
        self.test_run = self.test_run_info()
        self

        # self.location = os.path.join(tests_dir, str(self.test_run_id), self.test_type)
        os.makedirs(self.location, exist_ok=True)
        logging.info(f"TestRecorder recording logs in {self.location}")

    def test_run_info(self):
        return {
                   "Command": self.test_command(),
                   "TestType": self.test_type,
                   "TestManifest": self.test_manifest_path,
                   "DistributionManifest": paths.join(self.base_path, f"dist/{self.test_product}/manifest.yml"),
                   "TestID": self.test_run_id
               },
        pass

    def test_command(self):
        return "./test.sh integ-test manifests/2.6.0/opensearch-2.6.0-test.yml --paths opensearch=https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.6.0/7083/linux/x64/tar"

    def create_raw_manifest(self) -> None:
        test_run_dict = {
            "schema-version": "1.0",
            "name": "OpenSearch",
            "test-run": None if self.test_run is None else self.test_run,
            "components": self.test_manifest.components
        }

    def to_file(self, output_path: str) -> None:
        self.test_run_manifest = TestRunManifest(self.test_run_dict)
        self.test_run_manifest.to_file(output_path)

    def test_command(self, component: str, distribution: str) -> str:
        return "./test.sh integ-test manifests/2.6.0/opensearch-2.6.0-test.yml --paths " \
               "opensearch=https://ci.opensearch.org/ci/dbc/distribution-build-opensearch/2.6.0/7083/linux/x64/tar"



