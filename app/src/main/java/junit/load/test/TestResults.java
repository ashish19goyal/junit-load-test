package junit.load.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This object is used to capture test results in-memory during test execution
 * @author Ashish Goyal
 */
class TestResults {
    Object testObject;
    Method testMethod;
    List<Long> latencies = new ArrayList<>();
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger errorCount = new AtomicInteger();
    boolean isSuccess = false;

    TestResults(Object testObject, Method testMethod) {
        this.testObject = testObject;
        this.testMethod = testMethod;
    }
}
