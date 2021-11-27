package junit.load.test;

/**
 * Provides constants to publish the test results reports
 * This is an internal only class
 * These constants are not to be used by any external library or application
 * @author Ashish Goyal
 */
class Constants {
    static final String BUILD_FOLDER = System.getProperty("user.dir") + "/build";
    static final String LOAD_TEST_FOLDER = "/load-test";
    static final String TEST_RESOURCES_FOLDER = "/resources/test/";
    static final String LOAD_TEST_CSV_FILE = "/results.csv";
    static final String LOAD_TEST_JSON_FILE = "/results.json";
    static final String LOAD_TEST_HTML_FILE = "/index.html";
    static final String LOAD_TEST_JS_FILE = "/results.js";
}
