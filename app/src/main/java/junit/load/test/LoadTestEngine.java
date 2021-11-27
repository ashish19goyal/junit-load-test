package junit.load.test;

import org.junit.jupiter.engine.config.CachingJupiterConfiguration;
import org.junit.jupiter.engine.config.DefaultJupiterConfiguration;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.*;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import java.lang.reflect.Method;

/**
 * This is a custom implementation of test engine.
 * It helps in using junit test engine to define and run load tests
 * Only the tests annotated with {@link LoadTest} are executed by this engine
 * The test results report is generated in csv, json and html formats in the build folder
 * Link to html report - build/load-test/index.html
 *
 * @author Ashish Goyal
 */
public class LoadTestEngine implements TestEngine {

    private FileHelper csvHelper =
            new FileHelper(Constants.BUILD_FOLDER + Constants.LOAD_TEST_FOLDER, Constants.LOAD_TEST_CSV_FILE);
    private FileHelper jsonHelper =
            new FileHelper(Constants.BUILD_FOLDER + Constants.LOAD_TEST_FOLDER, Constants.LOAD_TEST_JSON_FILE);
    private FileHelper htmlHelper =
            new FileHelper(Constants.BUILD_FOLDER + Constants.LOAD_TEST_FOLDER, Constants.LOAD_TEST_HTML_FILE);
    private FileHelper jsHelper =
            new FileHelper(Constants.BUILD_FOLDER + Constants.LOAD_TEST_FOLDER, Constants.LOAD_TEST_JS_FILE);
    private LoadTestReporter loadTestReporter = new LoadTestReporter(csvHelper, jsonHelper, htmlHelper, jsHelper);
    private LoadTestExecutor loadTestExecutor = new LoadTestExecutor(loadTestReporter);

    /**
     * Inherited from TestEngine. Mandatorily override
     * Defines a uniqueId for the test engine
     * @return
     */
    @Override
    public String getId() {
        return "load-test-engine";
    }

    /**
     * Inherited from TestEngine. Mandatorily override
     * Find all the test cases to be executed by this Test Engine
     * @param request
     * @param uniqueId UniqueId of the Test Engine
     * @return TestDescriptor that contains all the test cases to be executed
     */
    @Override
    public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {

        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "Load Test");
        JupiterConfiguration configuration = new CachingJupiterConfiguration(
                       new DefaultJupiterConfiguration(request.getConfigurationParameters()));

        addTestClassDescriptors(request, engineDescriptor, uniqueId, configuration);
        return engineDescriptor;
    }

    /**
     * Identify all the classes that contains test cases to be executed
     * @param request
     * @param engineDescriptor
     * @param engineId
     * @param configuration
     */
    private void addTestClassDescriptors(EngineDiscoveryRequest request, TestDescriptor engineDescriptor,
                                         UniqueId engineId, JupiterConfiguration configuration) {
        request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
            boolean selectAllMethods = AnnotationSupport.isAnnotated(selector.getJavaClass(), LoadTest.class);
            UniqueId testUniqueId = engineId.append("class", selector.getClassName());

            TestDescriptor classDescriptor = new AbstractTestDescriptor(
                    testUniqueId,
                    selector.getJavaClass().getSimpleName(),
                    ClassSource.from(selector.getJavaClass())) {
                @Override
                public Type getType() {
                    return Type.CONTAINER;
                }
            };

            engineDescriptor.addChild(classDescriptor);
            addTestMethodDescriptors(selector.getJavaClass(), classDescriptor, testUniqueId,
                    selectAllMethods, configuration);
        });
    }

    /**
     * Identify all the test cases to be executed
     * @param testClass
     * @param classDescriptor
     * @param classUniqueId
     * @param selectAllMethods
     * @param configuration
     */
    private void addTestMethodDescriptors(Class<?> testClass, TestDescriptor classDescriptor, UniqueId classUniqueId,
                                          boolean selectAllMethods, JupiterConfiguration configuration) {
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (selectAllMethods || AnnotationSupport.isAnnotated(method, LoadTest.class)) {
                UniqueId testUniqueId = classUniqueId.append("method", method.getName());

                TestDescriptor child = new TestMethodTestDescriptor(testUniqueId, testClass, method, configuration);

                classDescriptor.addChild(child);
            }
        }
    }

    @Override
    public void execute(ExecutionRequest request) {
        // to get the list of all the tests to execute
        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
        EngineExecutionListener listener = request.getEngineExecutionListener();

        // bind events to execution listener
        listener.executionStarted(engineDescriptor);
        loadTestReporter.markStarted();
        for (TestDescriptor testDescriptor : engineDescriptor.getChildren()) {
            listener.executionStarted(testDescriptor);
            executeTestDescriptor(testDescriptor, listener);
        }
        loadTestReporter.markDone();
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
            if (loadTestExecutor.run(descriptor)) {
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
