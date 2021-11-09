package junit.load.test;

import java.util.List;

public class TestResultsReport {
    public String testSuite;
    public String testMethod;
    public String status; // fail or pass
    public double averageTimeTaken;
    public double medianTimeTaken;
    public double p95TimeTaken;
    public double p99TimeTaken;
    public List<Long> latencies;
    public int iterations;
    public int errorCount;

}
