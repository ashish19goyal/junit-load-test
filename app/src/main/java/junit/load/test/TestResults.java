package junit.load.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TestResults {
    public Object testObject;
    public Method testMethod;
    public List<Long> latencies = new ArrayList<>();
    public AtomicInteger successCount = new AtomicInteger();
    public AtomicInteger errorCount = new AtomicInteger();

    public TestResults(Object testObject, Method testMethod) {
        this.testObject = testObject;
        this.testMethod = testMethod;
    }
}
