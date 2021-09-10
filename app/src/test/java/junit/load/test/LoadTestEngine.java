package junit.load.test;

import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.platform.engine.*;

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

        // bind events to execution listener
        EngineExecutionListener listener = request.getEngineExecutionListener();
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
     * @param testMethod the method to be executed
     * @return returns whether the test was successful or not
     */
    private boolean executeLoadTest(TestDescriptor testMethod) {
        ((TestMethodTestDescriptor) testMethod).execute()
        System.out.println("Test descriptor class - " + testMethod.getClass());
        return false;
    }
}
