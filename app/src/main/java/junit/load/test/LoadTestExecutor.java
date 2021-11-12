package junit.load.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;

/**
 * This helper class reads the load test config and process a single load test
 * The results of the test are captured in memory and aggregated with the hep of {@link LoadTestReporter}
 * @author Ashish Goyal
 */
class LoadTestExecutor {

    private LoadTestReporter reporter;

    LoadTestExecutor(LoadTestReporter reporter) {
        this.reporter = reporter;
    }

    /**
     * Executes the test as per load testing config
     * @param testDescriptor the method to be executed
     * @return whether the test was successful or not
     */
    boolean run(TestDescriptor testDescriptor) {
        TestMethodTestDescriptor descriptor = (TestMethodTestDescriptor) testDescriptor;
        Class<?> testClass = descriptor.getTestClass();
        Method testMethod = descriptor.getTestMethod();

        // skipping tests not annotated with load test config annotation
        LoadTest testConfig = testClass.getAnnotation(LoadTest.class);
        if (testMethod.getAnnotation(LoadTest.class) != null) {
            testConfig = testMethod.getAnnotation(LoadTest.class);
        }
        if (testConfig == null) {
            return true;
        }

        try {
            int cycles = testConfig.cycles();
            int threads = testConfig.threads();
            Object testObject = testClass.getDeclaredConstructor().newInstance();
            if (threads == 1) {
                while (cycles-- > 0) {
                    runIteration(testObject, testMethod);
                }
                return reporter.isSuccess(testObject, testMethod, testConfig.errorThreshold());
            }

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            List<Callable<Object>> taskList = new ArrayList<>();
            while (cycles-- > 0) {
                Callable<Object> task = () -> {
                    runIteration(testObject, testMethod);
                    return null;
                };
                taskList.add(task);
            }
            List<Future<Object>> taskResults = executor.invokeAll(taskList);
            return reporter.isSuccess(testObject, testMethod, testConfig.errorThreshold());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Invoke a single iteration of the test method and mark it success or failure
     * @param testObject Reference object to invoke the test method
     * @param testMethod Test Method to be invoked
     */
    private void runIteration(Object testObject, Method testMethod) {
        long start = System.currentTimeMillis();
        try{
            testMethod.invoke(testObject);
            reporter.markSuccess(testObject, testMethod, start);
        } catch (InvocationTargetException e) {
            reporter.markError(testObject, testMethod, start);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            reporter.markError(testObject, testMethod, start);
            e.printStackTrace();
        }
    }
}
