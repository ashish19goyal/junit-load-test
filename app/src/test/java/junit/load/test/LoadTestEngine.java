package junit.load.test;

import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.platform.engine.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTestEngine implements TestEngine {

    @Override
    public String getId() {
        return "load-test-engine";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        JupiterConfiguration configuration = new CachingJupiterConfiguration(
			new DefaultJupiterConfiguration(discoveryRequest.getConfigurationParameters()));
		JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(uniqueId, configuration);
        new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest request) {
        // to get the list of all the tests to execute
        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
        EngineExecutionListener listener = request.getEngineExecutionListener();

        // bind events to execution listener
        listener.executionStarted(engineDescriptor);

        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            listener.executionStarted(testDescriptor);
            executeTestDescriptor(testDescriptor, listener);
        }
        // mark execution as successful
        listener.executionFinished(engineDescriptor, TestExecutionResult.successful());
    }

    /**
     * Iterate through test containers
     * @param descriptor test descriptor may be a test method or a test container
     */
    private void executeTestDescriptor(TestDescriptor descriptor, EngineExecutionListener listener) {
        if (descriptor.isTest()) {
            if(executeLoadTest(descriptor)) {
                listener.executionFinished(descriptor, TestExecutionResult.successful());
            } else {
                listener.executionFinished(descriptor,
                        TestExecutionResult.failed(new AssertionError("Load testing failed")));
            }
        } else {
            for (TestDescriptor descriptorChild : descriptor.getChildren()) {
                executeTestDescriptor(descriptorChild, listener);
            }
        }
    }

    /**
     * Executes the test as per load testing config
     * @param testDescriptor the method to be executed
     * @return returns whether the test was successful or not
     */
    private boolean executeLoadTest(TestDescriptor testDescriptor) {
        TestMethodTestDescriptor descriptor = (TestMethodTestDescriptor) testDescriptor;
        Class<?> testClass = descriptor.getTestClass();
        Method testMethod = descriptor.getTestMethod();

        // skipping tests not annotated with load test config annotation
        LoadTest testConfig = testClass.getAnnotation(LoadTest.class);
        if (testMethod.getAnnotation(LoadTest.class) != null) {
            testConfig = testMethod.getAnnotation(LoadTest.class);
        }
        if (testConfig == null) {
            System.out.println("Skipping test execution - "+testClass.getName()+"."+testMethod.getName());
            return true;
        }

        try {
            int cycles = testConfig.cycles();
            int threads = testConfig.threads();
            Object testObject = testClass.getDeclaredConstructor().newInstance();
            AtomicInteger errorCounter = new AtomicInteger();
            if (threads == 1) {
                while (cycles-- > 0) {
                    executeAndCaptureMetrics(testMethod, testObject, errorCounter);
                }
                return evaluateSuccess(errorCounter, cycles, testConfig.errorThreshold());
            }

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            while (cycles-- > 0) {
                Runnable task = () -> {
                    executeAndCaptureMetrics(testMethod, testObject, errorCounter);
                };
                executor.execute(task);
            }
            return evaluateSuccess(errorCounter, cycles, testConfig.errorThreshold());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void executeAndCaptureMetrics(Method testMethod, Object testObject, AtomicInteger errorCounter) {
        try{
            testMethod.invoke(testObject);
        } catch (InvocationTargetException e) {
            errorCounter.getAndIncrement();
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean evaluateSuccess(AtomicInteger errorCounter, int cycles, float errorThreshold) {
        if (errorThreshold == 0) {
            return errorCounter.get() <= 0;
        }
        float errorRate = errorCounter.get()/cycles;
        return errorRate <= errorThreshold;
    }
}
