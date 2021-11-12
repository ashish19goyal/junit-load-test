package junit.load.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated test results for test suites
 * @author Ashish Goyal
 */
class SuiteResultsReport {
    public String testSuite;
    public double averageTimeTaken;
    public double medianTimeTaken;
    public double p95TimeTaken;
    public double p99TimeTaken;
    public List<Long> latencies = new ArrayList<>();
    public int iterations;
    public int errorCount;
    public int testCount;
    public int testPassed;
}
