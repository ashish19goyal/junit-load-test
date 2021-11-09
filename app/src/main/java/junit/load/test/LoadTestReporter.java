package junit.load.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

public class LoadTestReporter {

    private Map<String, TestResults> results = new HashMap<>();
    private FileHelper csvHelper;
    private FileHelper jsonHelper;
    private FileHelper htmlHelper;
    private long startTime;
    private long totalDuration;

    public LoadTestReporter(FileHelper csvHelper, FileHelper jsonHelper, FileHelper htmlHelper) {

        this.csvHelper = csvHelper;
        this.jsonHelper = jsonHelper;
        this.htmlHelper = htmlHelper;
    }

    public void add(Object testObject, Method testMethod, long timeTakenToExecute) {
        String key = getKey(testObject, testMethod);
        if (!results.containsKey(key)) {
            results.put(key, new TestResults(testObject, testMethod));
        }
        results.get(key).latencies.add(timeTakenToExecute);
    }

    public void markSuccess(Object testObject, Method testMethod) {
        String key = getKey(testObject, testMethod);
        if (!results.containsKey(key)) {
            results.put(key, new TestResults(testObject, testMethod));
        }
        results.get(key).successCount.getAndIncrement();
    }

    public void markError(Object testObject, Method testMethod) {
        String key = getKey(testObject, testMethod);
        if (!results.containsKey(key)) {
            results.put(key, new TestResults(testObject, testMethod));
        }
        results.get(key).errorCount.getAndIncrement();
    }

    public boolean isSuccess(Object testObject, Method testMethod, float errorThreshold) {
        String key = getKey(testObject, testMethod);
        if (errorThreshold == 0) {
            results.get(key).isSuccess = results.get(key).errorCount.get() <= 0;
            return results.get(key).isSuccess;
        }
        float errorRate = results.get(key).errorCount.get() / (results.get(key).successCount.get() + results.get(key).errorCount.get());
        results.get(key).isSuccess = errorRate <= errorThreshold;
        return results.get(key).isSuccess;
    }

    public void generateReport() {
        System.out.println("Generating load-test reports");
        ResultsReport resultsReport = getResultsReport();
        generateCsvReport(resultsReport);
        generateJsonReport(resultsReport);
        generateHtmlReport();
    }

    public void markStarted() {
        this.startTime = System.currentTimeMillis();
    }

    public void markDone() {
        this.totalDuration = System.currentTimeMillis() - startTime;
    }

    private void generateHtmlReport() {
        System.out.println("generating load-test html report");
        htmlHelper.copy("load-test.html");
    }

    private void generateJsonReport(ResultsReport resultsReport) {
        System.out.println("generating load-test json report");
        jsonHelper.clear();
        try {
            jsonHelper.write(new ObjectMapper().writeValueAsString(resultsReport));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateCsvReport(ResultsReport resultsReport) {
        System.out.println("generating load-test csv report");
        String heading = "test-suite,test-method,iterations,error-count,status,time-taken(average),time-taken(median),time-taken(p95),time-taken(p99)\n";
        try {
            csvHelper.clear();
            csvHelper.write(heading);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (TestResultsReport entry : resultsReport.tests) {
            String line = String.format("%s,%s,%d,%d,%s,%.2f,%.2f,%.2f,%.2f\n",
                    entry.testSuite,
                    entry.testMethod,
                    entry.iterations,
                    entry.errorCount,
                    entry.status,
                    entry.averageTimeTaken,
                    entry.medianTimeTaken,
                    entry.p95TimeTaken,
                    entry.p99TimeTaken);
            try {
                csvHelper.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ResultsReport getResultsReport() {
        ResultsReport result = new ResultsReport();
        List<TestResultsReport> testResultsReport = new ArrayList<>();
        List<SuiteResultsReport> suiteResultsReport = new ArrayList<>();

        // preparing results for each test
        for (Map.Entry<String, TestResults> entry : results.entrySet()) {
            TestResultsReport reportEntry = new TestResultsReport();
            reportEntry.testSuite = entry.getValue().testObject.getClass().getName();
            reportEntry.testMethod = entry.getValue().testMethod.getName();
            reportEntry.errorCount = entry.getValue().errorCount.get();
            reportEntry.iterations = entry.getValue().successCount.get() + entry.getValue().errorCount.get();
            reportEntry.latencies = entry.getValue().latencies;
            reportEntry.status = entry.getValue().isSuccess ? "pass" : "fail";
            double[] latencies = entry.getValue().latencies.stream().mapToDouble(Long::doubleValue).toArray();
            Percentile percentile = new Percentile();
            percentile.setData(latencies);
            reportEntry.averageTimeTaken = roundOff(Arrays.stream(latencies).average().getAsDouble());
            reportEntry.medianTimeTaken = roundOff(percentile.evaluate(50.00));
            reportEntry.p95TimeTaken = roundOff(percentile.evaluate(95.00));
            reportEntry.p99TimeTaken = roundOff(percentile.evaluate(99.00));

            testResultsReport.add(reportEntry);
        }

        result.tests = testResultsReport;

        // preparing results for each test suite
        Map<String, List<TestResults>> testResultsBySuite = results.values().stream()
            .collect(groupingBy(x-> x.testObject.getClass().getName()));
        for (Map.Entry<String, List<TestResults>> entry : testResultsBySuite.entrySet()) {
            SuiteResultsReport reportEntry = new SuiteResultsReport();
            reportEntry.testSuite = entry.getKey();

            for (TestResults testResult: entry.getValue()) {
                reportEntry.iterations += testResult.errorCount.get() + testResult.successCount.get();
                reportEntry.errorCount += testResult.errorCount.get();
                reportEntry.latencies.addAll(testResult.latencies);
                reportEntry.testCount++;
                if (testResult.isSuccess) {
                    reportEntry.testPassed++;
                }
            }
            double[] latencies = reportEntry.latencies.stream().mapToDouble(Long::doubleValue).toArray();
            Percentile percentile = new Percentile();
            percentile.setData(latencies);
            reportEntry.averageTimeTaken = roundOff(Arrays.stream(latencies).average().getAsDouble());
            reportEntry.medianTimeTaken = roundOff(percentile.evaluate(50.00));
            reportEntry.p95TimeTaken = roundOff(percentile.evaluate(95.00));
            reportEntry.p99TimeTaken = roundOff(percentile.evaluate(99.00));
            suiteResultsReport.add(reportEntry);
        }

        result.suites = suiteResultsReport;
        result.totalDuration = totalDuration;
        return result;
    }

    private String getKey(Object testObject, Method testMethod) {
        return testObject.getClass().getName() + "-" + testMethod.getName();
    }

    private double roundOff(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
