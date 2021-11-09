package junit.load.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class LoadTestReporter {

    private Map<String, TestResults> results = new HashMap<>();
    private FileHelper csvHelper;
    private FileHelper jsonHelper;
    private FileHelper htmlHelper;

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
            return results.get(key).errorCount.get() <= 0;
        }
        float errorRate = results.get(key).errorCount.get() / (results.get(key).successCount.get() + results.get(key).errorCount.get());
        return errorRate <= errorThreshold;
    }

    public void generateReport() {
        System.out.println("Generating load-test reports");
        List<TestResultsReport> resultsReport = getResultsReport();
        generateCsvReport(resultsReport);
        generateJsonReport(resultsReport);
        generateHtmlReport(resultsReport);
    }

    private void generateHtmlReport(List<TestResultsReport> resultsReport) {
        System.out.println("generating load-test html report");
        htmlHelper.copy("load-test.html");
    }

    private void generateJsonReport(List<TestResultsReport> resultsReport) {
        System.out.println("generating load-test json report");
        jsonHelper.clear();
        try {
            jsonHelper.write(new ObjectMapper().writeValueAsString(resultsReport));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateCsvReport(List<TestResultsReport> resultsReport) {
        System.out.println("generating load-test csv report");
        String heading = "test-suite,test-method,iterations,time-taken(average),time-taken(median),time-taken(p95),time-taken(p99)\n";
        try {
            csvHelper.clear();
            csvHelper.write(heading);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (TestResultsReport entry : resultsReport) {
            String line = String.format("%s,%s,%d,%.2f,%.2f,%.2f,%.2f\n",
                    entry.testSuite,
                    entry.testMethod,
                    entry.iterations,
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

    private List<TestResultsReport> getResultsReport() {
        List<TestResultsReport> reportResults = new ArrayList<>();

        for (Map.Entry<String, TestResults> entry : results.entrySet()) {
            TestResultsReport reportEntry = new TestResultsReport();
            reportEntry.testSuite = entry.getValue().testObject.getClass().getName();
            reportEntry.testMethod = entry.getValue().testMethod.getName();
            reportEntry.errorCount = entry.getValue().errorCount.get();
            reportEntry.iterations = entry.getValue().successCount.get() + entry.getValue().errorCount.get();
            reportEntry.latencies = entry.getValue().latencies;
            System.out.println("latencies" + entry.getValue().latencies.toString());
            double[] latencies = entry.getValue().latencies.stream().mapToDouble(Long::doubleValue).toArray();
            Percentile percentile = new Percentile();
            percentile.setData(latencies);
            reportEntry.averageTimeTaken = Arrays.stream(latencies).average().getAsDouble();
            reportEntry.medianTimeTaken = percentile.evaluate(50.00);
            reportEntry.p95TimeTaken = percentile.evaluate(95.00);
            reportEntry.p99TimeTaken = percentile.evaluate(99.00);

            reportResults.add(reportEntry);
        }

        return reportResults;
    }

    private String getKey(Object testObject, Method testMethod) {
        return testObject.getClass().getName() + "-" + testMethod.getName();
    }
}
