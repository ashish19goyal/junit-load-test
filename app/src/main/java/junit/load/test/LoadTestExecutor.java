package junit.load.test;

import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.TestDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This helper class reads the load test config and process a single load test
 * The results of the test are captured in memory and aggregated with the hep of {@link LoadTestReporter}
 *
 * @author Ashish Goyal
 */
class LoadTestExecutor {

    private LoadTestReporter reporter;

    LoadTestExecutor(LoadTestReporter reporter) {
        this.reporter = reporter;
    }

    /**
     * Executes the test as per load testing config
     *
     * @param testDescriptor the method to be executed
     * @return whether the test was successful or not
     */
    boolean run(TestDescriptor testDescriptor) {
        TestMethodTestDescriptor descriptor = (TestMethodTestDescriptor) testDescriptor;
        Class<?> testClass = descriptor.getTestClass();
        Method testMethod = descriptor.getTestMethod();
        Object testObject = null;
        try {
            testObject = testClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |  InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }

        // skipping tests not annotated with load test config annotation
        LoadTest testConfig = testClass.getAnnotation(LoadTest.class);
        if (testMethod.getAnnotation(LoadTest.class) != null) {
            testConfig = testMethod.getAnnotation(LoadTest.class);
        }
        if (testConfig == null) {
            return true;
        }

        String testDataFileName = testConfig.testDataFile();
        List<Future<Object>> taskResults;
        if (testDataFileName == null || testDataFileName.isEmpty() || testDataFileName.isBlank()) {
            try {
                int cycles = testConfig.cycles();
                int threads = testConfig.threads();

                if (threads == 1) {
                    while (cycles-- > 0) {
                        runIteration(testObject, testMethod, null);
                    }
                    return reporter.isSuccess(testObject, testMethod, testConfig.errorThreshold());
                }

                ExecutorService executor = Executors.newFixedThreadPool(threads);
                List<Callable<Object>> taskList = new ArrayList<>();
                final Object testObjectCopy = testObject;
                while (cycles-- > 0) {
                    Callable<Object> task = () -> {
                        runIteration(testObjectCopy, testMethod, null);
                        return null;
                    };
                    taskList.add(task);
                }
                taskResults = executor.invokeAll(taskList);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            FileHelper testDataFileHelper = new FileHelper(Constants.BUILD_FOLDER + Constants.TEST_RESOURCES_FOLDER, testDataFileName);
            try {
                List<String> paramsLines = testDataFileHelper.read();
                int threads = testConfig.threads();

                if (threads == 1) {
                    for (String params : paramsLines) {
                        // TODO: use a smarter csv reader for handling commas in the text
                        String[] stringParams = params.split(",");
                        runIteration(testObject, testMethod, stringParams);
                    }
                    return reporter.isSuccess(testObject, testMethod, testConfig.errorThreshold());
                }

                ExecutorService executor = Executors.newFixedThreadPool(threads);
                List<Callable<Object>> taskList = new ArrayList<>();
                final Object testObjectCopy = testObject;
                for (String params : paramsLines) {
                    String[] stringParams = params.split(",");
                    Callable<Object> task = () -> {
                        runIteration(testObjectCopy, testMethod, stringParams);
                        return null;
                    };
                    taskList.add(task);
                }
                taskResults = executor.invokeAll(taskList);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return reporter.isSuccess(testObject, testMethod, testConfig.errorThreshold());


    }

    /**
     * Invoke a single iteration of the test method and mark it success or failure
     *
     * @param testObject Reference object to invoke the test method
     * @param testMethod Test Method to be invoked
     */
    private void runIteration(Object testObject, Method testMethod, Object[] args) {
        long start = System.currentTimeMillis();
        try {
            if (args != null) {
                testMethod.invoke(testObject, args);
            } else {
                testMethod.invoke(testObject);
            }
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
