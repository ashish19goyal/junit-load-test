package junit.load.test;

import java.util.List;

/**
 * Publishable test results
 * @author Ashish Goyal
 */
class ResultsReport {
    public List<TestResultsReport> tests;
    public List<SuiteResultsReport> suites;
    public long totalDuration;
}
