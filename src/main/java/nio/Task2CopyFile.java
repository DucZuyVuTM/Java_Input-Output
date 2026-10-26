package nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;

public class Task2CopyFile {

    public static void main(String[] args) {
        String srcPath = "src/main/resources/data/suppliers_100MB.csv";
        String dstPathIO = "src/main/resources/data/copy_io.csv";
        String dstPathNIO = "src/main/resources/data/copy_nio.csv";
        String dstPathApache = "src/main/resources/data/copy_apache.csv";
        String dstPathFiles = "src/main/resources/data/copy_files.csv";

        System.out.println("=== Exercise 2: Compile speed when copying large file ===");
        System.out.println("Source path: " + srcPath + "\n");

        try {
            // 1. Java IO (FileInputStream/FileOutputStream)
            long startIO = System.nanoTime();
            copyByIO(srcPath, dstPathIO);
            long endIO = System.nanoTime();
            System.out.printf("1. Java IO (Stream):      %10.2f ms%n", (endIO - startIO) / 1_000_000.0);

            // 2. Java NIO (FileChannel)
            long startNIO = System.nanoTime();
            copyByNIO(srcPath, dstPathNIO);
            long endNIO = System.nanoTime();
            System.out.printf("2. Java NIO (Channel):    %10.2f ms%n", (endNIO - startNIO) / 1_000_000.0);

            // 3. Apache Commons IO
            long startApache = System.nanoTime();
            copyByApache(srcPath, dstPathApache);
            long endApache = System.nanoTime();
            System.out.printf("3. Apache Commons IO:     %10.2f ms%n", (endApache - startApache) / 1_000_000.0);

            // 4. Java NIO2 (Files class)
            long startFiles = System.nanoTime();
            copyByFiles(srcPath, dstPathFiles);
            long endFiles = System.nanoTime();
            System.out.printf("4. Java NIO2 (Files):     %10.2f ms%n", (endFiles - startFiles) / 1_000_000.0);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // --- Java IO ---
    public static void copyByIO(String src, String dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[8192]; // Bộ đệm 8KB
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    // --- Java NIO Channel ---
    public static void copyByNIO(String src, String dst) throws IOException {
        try (FileChannel inChannel = new FileInputStream(src).getChannel();
             FileChannel outChannel = new FileOutputStream(dst).getChannel()) {
            
            long size = inChannel.size();
            long position = 0;
            while (position < size) {
                position += inChannel.transferTo(position, size - position, outChannel);
            }
        }
    }

    // --- Apache Commons IO ---
    public static void copyByApache(String src, String dst) throws IOException {
        File srcFile = new File(src);
        File dstFile = new File(dst);
        FileUtils.copyFile(srcFile, dstFile);
    }

    // ---Java NIO2 Files class ---
    public static void copyByFiles(String src, String dst) throws IOException {
        Path source = Paths.get(src);
        Path destination = Paths.get(dst);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
}
