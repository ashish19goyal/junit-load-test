package junit.load.test;

import java.io.*;

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

    public void copy(String source) {
        File destination = new File(folder + file);

        try {
            InputStream sourceFile = getClass().getClassLoader().getResourceAsStream(source);
            OutputStream destinationFile = new BufferedOutputStream(new FileOutputStream(destination));
            byte[] buffer = sourceFile.readAllBytes();
            destinationFile.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getHandle() throws IOException {
        File csvFile = new File(folder + file);
        if (!csvFile.exists()) {
            File loadTestFolder = new File(folder);
            if (!loadTestFolder.exists()) {
                loadTestFolder.mkdir();
            }
            csvFile.createNewFile();
        }
        return csvFile;
    }
}
