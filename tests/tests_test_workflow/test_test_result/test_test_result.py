import unittest
from unittest.mock import MagicMock

from test_workflow.test_result.test_result import TestResult


class TestTestResult(unittest.TestCase):
    def setUp(self) -> None:
        self.test_result = TestResult("sql", "with-security", 0)

    def test_failed(self) -> None:
        failed = self.test_result.failed
        self.assertFalse(failed)

    def test_log(self) -> None:
        result = MagicMock()
        with self.assertLogs() as captured:
            self.test_result.log(result)
        self.assertEqual(len(captured), 2)
        self.assertEqual(captured.records[0].getMessage(), str(result))
