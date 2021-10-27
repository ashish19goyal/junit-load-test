package junit.load.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvHelper {
    private String folder;
    private String file;

    public CsvHelper(String folder, String file) {
        this.folder = folder;
        this.file = file;
    }

    public void write(String line) throws IOException {
        File csvFile = getHandle();
        FileWriter writer = new FileWriter(csvFile, true);
        writer.append(line);
        writer.close();
    }

    private File getHandle() throws IOException {
        File csvFile = new File(folder + file);
        if (!csvFile.exists()) {
            File loadTestFolder = new File(folder);
            if(!loadTestFolder.exists()) {
                loadTestFolder.mkdir();
            }
            csvFile.createNewFile();
        }
        return csvFile;
    }
}
