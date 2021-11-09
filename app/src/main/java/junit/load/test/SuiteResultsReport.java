package junit.load.test;

import java.util.ArrayList;
import java.util.List;

public class SuiteResultsReport {
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
