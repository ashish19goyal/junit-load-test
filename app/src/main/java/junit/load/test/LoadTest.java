package junit.load.test;

import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation for marking the tests for inclusion in load testing
 * If the number of failed iterations is less than the cycles * error threshold / 100
 * then the test is not marked as failed
 * @author Ashish Goyal
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Testable
public @interface LoadTest {

    /**
     * Number of iterations of the load test to be executed
     * @return
     */
    int cycles() default 1;

    /**
     * Number of threads to use for concurrent execution of multiple cycles
     * @return
     */
    int threads() default 1;

    /**
     * Specified as percentage
     * Minimum value is 0, meaning, even if a single iteration fails, the test is marked as failed
     * Max value is 100, meaning, even if no iteration passes, the test is marked as success
     * @return
     */
    float errorThreshold() default 0;

    /**
     * Name of the test data file
     * Test data should be provided as String, int or boolean values in CSV file format
     * The order of fields in the CSV should match the order of input parameters to the test method
     * File should be created inside the test/resources folder
     * Each test case can have its own test data file
     * Or multiple test cases can share a test data file if they use the same input parameters
     * @return
     */
    String testDataFile() default "";
}
