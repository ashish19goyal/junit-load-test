package junit.load.test;

import java.util.List;

/**
 * Test results at test method level
 * @author Ashish Goyal
 */
class TestResultsReport {
    String testSuite;
    String testMethod;
    String status; // fail or pass
    double averageTimeTaken;
    double medianTimeTaken;
    double p95TimeTaken;
    double p99TimeTaken;
    List<Long> latencies;
    int iterations;
    int errorCount;
}
