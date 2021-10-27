package junit.load.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class LoadTestReporter {

    private Map<String, TestResults> results = new HashMap<>();
    private FileHelper csvHelper;
    private FileHelper jsonHelper;

    public LoadTestReporter(FileHelper csvHelper, FileHelper jsonHelper) {

        this.csvHelper = csvHelper;
        this.jsonHelper = jsonHelper;
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
        float errorRate = results.get(key).errorCount.get()/(results.get(key).successCount.get()+results.get(key).errorCount.get());
        return errorRate <= errorThreshold;
    }

    public void generateReport() {
        System.out.println("Generating load-test reports");
        generateCsvReport();
        generateJsonReport();
    }

    private void generateJsonReport() {
        System.out.println("generating load-test json report");
        List<TestResultsReport> resultsReport = getResultsReport();
        jsonHelper.clear();
        try {
            jsonHelper.write(new ObjectMapper().writeValueAsString(resultsReport));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateCsvReport() {
        System.out.println("generating load-test csv report");
        String heading = "test-suite,test-method,time-taken(average),time-taken(median),time-taken(p95),time-taken(p99)\n";
        try {
            csvHelper.clear();
            csvHelper.write(heading);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<TestResultsReport> reportResults = getResultsReport();

        for (TestResultsReport entry : reportResults) {
            String line = String.format("%s,%s,%.2f,%.2f,%.2f,%.2f\n",
                    entry.testSuite,
                    entry.testMethod,
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

        for (Map.Entry<String,TestResults> entry : results.entrySet()) {
            TestResultsReport reportEntry = new TestResultsReport();
            reportEntry.testSuite = entry.getValue().testObject.getClass().getName();
            reportEntry.testMethod = entry.getValue().testMethod.getName();
            reportEntry.errorCount = entry.getValue().errorCount.get();
            reportEntry.successCount = entry.getValue().successCount.get();
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
