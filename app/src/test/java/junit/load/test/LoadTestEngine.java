package junit.load.test;

import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.discovery.DiscoverySelectorResolver;
import org.junit.platform.engine.*;


public class LoadTestEngine implements TestEngine {

    private CsvHelper csvHelper =
            new CsvHelper(Constants.BUILD_FOLDER + Constants.LOAD_TEST_FOLDER, Constants.LOAD_TEST_FILE);
    private LoadTestReporter loadTestReporter = new LoadTestReporter(csvHelper);
    private LoadTestExecutor loadTestExecutor = new LoadTestExecutor(loadTestReporter);

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
        System.out.println("Executing load test engine");
        // to get the list of all the tests to execute
        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
        EngineExecutionListener listener = request.getEngineExecutionListener();

        // bind events to execution listener
        listener.executionStarted(engineDescriptor);

        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            listener.executionStarted(testDescriptor);
            executeTestDescriptor(testDescriptor, listener);
        }

        // generate report
        loadTestReporter.generateReport();

        // mark execution as successful
        listener.executionFinished(engineDescriptor, TestExecutionResult.successful());
    }

    /**
     * Iterate through test containers
     * @param descriptor test descriptor may be a test method or a test container
     */
    private void executeTestDescriptor(TestDescriptor descriptor, EngineExecutionListener listener) {
        if (descriptor.isTest()) {
            if(loadTestExecutor.run(descriptor)) {
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
}
