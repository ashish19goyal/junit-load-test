# junit-load-test
This library provides a junit test engine to perform load testing.

## Why should you use this load test engine -
Junit already provides a lot of capabilities to add unit testing and integration testing to Java projects. However, a lot of java projects are not able to write load testing scripts and results using java. The projects that do add load testing code have to go through a lot of trouble to do so. Others, use python scipts or jmeter like tools.

Using other technologies or tools to add load testing has a lot of downsides as listed below
- involves a learning curve to use the other technologies and tools
- the dev or CI tool setup becomes more complex with use of multiple tools and technologies
- multiple tools generate different test reports that are difficult to analyze in combination with unit testing and integration testing reports

This library resolves all of these problems. 

## How to use the load test engine

### Create a java project with gradle 
Currently, this library only works with gradle projects. It will be soon extended to work with other projects.

### Update the build.gradle file

Include the load test engine as a test dependency
```kotlin
dependencies {
    testImplementation("junit.load.test:junit-load-test:1.0-SNAPSHOT")
    
    ....
}
```

Create a gradle task for running load test as follows
```kotlin
tasks.register<Test>("loadtest") {
    reports.html.required.set(false)
    useJUnitPlatform {
        includeEngines("load-test-engine")
    }
    logger.lifecycle("Load testing report is generated. Open this link in browser to access the report - " + project.buildDir +"/load-test/index.html");
}
```

### Add load tests
Create a java file with definitions of load tests as follows.

```java
import junit.load.test.LoadTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppLoadTest {
    
    // A single iteration of this test case is run
    @LoadTest(errorThreshold = 0)
    public void singleIterationSumCheck() {
        assertEquals(2,1+1);
    }

    // 10 iterations of this test are run on 5 threads concurrently
    // If any iteration fails, the test case is marked as failed
    @LoadTest(cycles = 10, errorThreshold = 0, threads = 5)
    public void multipleIterationSumCheck() {
        assertEquals(2,1+1);
    }

    // in the test/resources folder, add a sum.csv file that contains test data to run iterations of this test
    // sum.csv should have 3 columns. 
    // The order of columns in the file should match the order of input parameters to test method
    // If less than 10% of the iterations fail, the test is still marked as successful
    @LoadTest(errorThreshold = 10, testDataFile = "sum.csv")
    public void sumCheckWithInput(String a, String b, String c) {
        int param1 = Integer.parseInt(a);
        int param2 = Integer.parseInt(b);
        int param3 = Integer.parseInt(c);
        assertEquals(param3,param1+param2);
    }

    // This test will not be executed by the load test engine as it is not annotated with LoadTest annotation
    @Test
    public void notLoadTest() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}

```

### Run load test
Use following command to run load tests for you application
```shell
gradle loadtest
```

### Consume load test results
Load testing reports are published in the build folder. Open the reports in a browser. Location of the reports file
```
build/load-test/index.html
```