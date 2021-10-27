package junit.load.test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class LoadTestReporter {

    private Map<String, TestResults> results = new HashMap<>();
    private CsvHelper csvHelper;

    public LoadTestReporter(CsvHelper csvHelper) {
        this.csvHelper = csvHelper;
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
        generateCsvReport();
    }

    private void generateCsvReport() {
        System.out.println("generating loadtest csv report");
        String heading = "test-suite,test-method,time-taken(average),time-taken(median),time-taken(p95),time-taken(p99)";
        try {
            csvHelper.write(heading);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String,TestResults> entry : results.entrySet()) {
            String testSuite = entry.getValue().testObject.getClass().getName();
            String testMethod = entry.getValue().testMethod.getName();
            double[] latencies = entry.getValue().latencies.stream().mapToDouble(Long::doubleValue).toArray();
            Percentile percentile = new Percentile();
            percentile.setData(latencies);
            double average = Arrays.stream(latencies).average().getAsDouble();
            double median = percentile.evaluate(50.00);
            double p95 = percentile.evaluate(95.00);
            double p99 = percentile.evaluate(99.00);
            String line = String.format("%s,%s,%d,%d,%d,%d",testSuite,testMethod,average,median,p95,p99);
            try {
                csvHelper.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getKey(Object testObject, Method testMethod) {
        return testObject.getClass().getName() + "-" + testMethod.getName();
    }
}
