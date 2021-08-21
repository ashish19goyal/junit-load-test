package junit.load.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfilingExtension implements BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private static final String BUILD_FOLDER = System.getProperty("user.dir") + "/build";
    private static final String LOAD_TEST_FOLDER = "/load-test";
    private static final String CSV_FILE_NAME = "/test-profiling.csv";

    private static final String CSV_HEADING = "test-suite,test-method,pass/fail,time-taken\n";
    private static final Map<String,Long> START_TIMING_MAP = new HashMap<>();

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        System.out.println("After Executing all tests " + context.getDisplayName());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (context.getTestMethod().get().getAnnotation(ProfilingConfig.class) != null) {
            String name = context.getTestClass().get().getName() + "_" + context.getDisplayName();
            if (!START_TIMING_MAP.containsKey(name)) {
                START_TIMING_MAP.put(name,System.currentTimeMillis());
            }
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (context.getTestMethod().get().getAnnotation(ProfilingConfig.class) != null) {
            String name = context.getTestClass().get().getName() + "_" + context.getDisplayName();
            if (!START_TIMING_MAP.containsKey(name)) {
                return;
            }
            long timeTaken = System.currentTimeMillis() - START_TIMING_MAP.get(name);
            String line = String.format("%s,%s,%s,%s\n",
                    context.getTestClass().get().getName(),
                    context.getDisplayName(),
                    1,timeTaken);
            writeToCSV(line);
        }
    }

    private void writeToCSV(String line) throws IOException {
        File csvFile = getCSVHandle();
        FileWriter writer = new FileWriter(csvFile, true);
        writer.append(line);
        writer.close();
    }

    private File getCSVHandle() throws IOException {
        File csvFile = new File(BUILD_FOLDER + LOAD_TEST_FOLDER + CSV_FILE_NAME);
        if (!csvFile.exists()) {
            File loadTestFolder = new File(BUILD_FOLDER+LOAD_TEST_FOLDER);
            if(!loadTestFolder.exists()) {
                loadTestFolder.mkdir();
            }
            csvFile.createNewFile();
            FileWriter writer = new FileWriter(csvFile, true);
            writer.append(CSV_HEADING);
            writer.close();
        }
        return csvFile;
    }
}
