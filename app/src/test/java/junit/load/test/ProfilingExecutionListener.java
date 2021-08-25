package junit.load.test;
import org.junit.platform.launcher.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static junit.load.test.Constants.*;

public class ProfilingExecutionListener implements TestExecutionListener{
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        try {
            writeToFile("Executed test plan = " + testPlan.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String line) throws IOException {
        File file = getFileHandle();
        FileWriter writer = new FileWriter(file, true);
        writer.append(line);
        writer.close();
    }

    private File getFileHandle() throws IOException {
        File file = new File(BUILD_FOLDER + REPORT_FOLDER + PROFILING_REPORT_FILE);
        if (!file.exists()) {
            File loadTestFolder = new File(BUILD_FOLDER + REPORT_FOLDER);
            if(!loadTestFolder.exists()) {
                loadTestFolder.mkdir();
            }
            file.createNewFile();
        }
        return file;
    }
}
