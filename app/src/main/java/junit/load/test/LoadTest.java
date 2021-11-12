package junit.load.test;

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
}
