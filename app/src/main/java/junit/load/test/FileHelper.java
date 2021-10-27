package junit.load.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {
    private String folder;
    private String file;

    public FileHelper(String folder, String file) {
        this.folder = folder;
        this.file = file;
    }

    public void clear() {
        File csvFile = new File(folder + file);
        if (csvFile.exists()) {
            csvFile.delete();
        }
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
