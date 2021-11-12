package junit.load.test;

import java.io.*;

/**
 * Provides file IO interactions to help publish test results reports
 * This is an internal only class
 * @author Ashish Goyal
 */
class FileHelper {
    private String folder;
    private String file;

    /**
     * Constructor accepts folder location and file name to manage
     * @param folder folder location where the file to be managed is located
     * @param file name of the file to be managed
     */
    FileHelper(String folder, String file) {
        this.folder = folder;
        this.file = file;
    }

    /**
     * Clears the file for reuse
     */
    void clear() {
        File csvFile = new File(folder + file);
        if (csvFile.exists()) {
            csvFile.delete();
        }
    }

    /**
     * Write to the file
     * @param line content line to be written to the file
     * @throws IOException
     */
    void write(String line) throws IOException {
        File csvFile = getHandle();
        FileWriter writer = new FileWriter(csvFile, true);
        writer.append(line);
        writer.close();
    }

    /**
     * Copies the source file to the managed file location
     * @param source file to be copied
     */
    void copy(String source) {
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

    /**
     * Create a file if it doesn't exist and return a handle for it
     * @return File handle
     * @throws IOException
     */
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
