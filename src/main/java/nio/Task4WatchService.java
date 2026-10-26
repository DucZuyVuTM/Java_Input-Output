package nio;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task4WatchService {

    // Store the content of old file to compare changes
    private static final Map<String, List<String>> fileCache = new HashMap<>();
    // Store the file size and checksum before the file is deleted
    private static final Map<String, String> deletedFileInfo = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        // Directory that need to watch
        Path dirToWatch = Paths.get("src/main/resources/data/watch_folder");
        Files.createDirectories(dirToWatch); // Auto create if not found

        WatchService watchService = FileSystems.getDefault().newWatchService();
        dirToWatch.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        System.out.println("=== Exercise 4: Watch service ===");
        System.out.println("Watching dir: " + dirToWatch.toAbsolutePath());
        System.out.println("Open another Terminal and try creating, modifying, or deleting a file in this directory!");
        System.out.println("---------------------------------------------------------");

        // Scan existing files in the directory upon startup and store them in the cache
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirToWatch)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    fileCache.put(path.getFileName().toString(), Files.readAllLines(path));
                }
            }
        }

        // Listen to events
        while (true) {
            WatchKey key = watchService.take(); // Wait for another event

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) continue;

                // Get changed file name
                Path fileName = (Path) event.context();
                Path fullPath = dirToWatch.resolve(fileName);
                String name = fileName.toString();

                if (kind == ENTRY_CREATE) {
                    System.out.println("\n[+] CREATE NEW FILE: " + name);
                    // Read and cache the original content
                    if (Files.isRegularFile(fullPath)) {
                        fileCache.put(name, Files.readAllLines(fullPath));
                    }

                } else if (kind == ENTRY_MODIFY) {
                    if (Files.isRegularFile(fullPath)) {
                        System.out.println("\n[*] FILE CHANGE: " + name);
                        List<String> oldLines = fileCache.getOrDefault(name, List.of());
                        List<String> newLines = Files.readAllLines(fullPath);

                        // Compare added/deleted lines
                        printDiff(oldLines, newLines);

                        // Update cache again and calculate checksum
                        fileCache.put(name, newLines);
                        updateFileInfo(fullPath, name);
                    }

                } else if (kind == ENTRY_DELETE) {
                    System.out.println("\n[-] DELETE FILE: " + name);
                    // Retrieve previously stored information
                    String info = deletedFileInfo.getOrDefault(name, "No info (file deleted too fast)");
                    System.out.println("    Size & Checksum: " + info);
                    
                    // Clear memory
                    fileCache.remove(name);
                    deletedFileInfo.remove(name);
                }
            }

            boolean valid = key.reset();
            if (!valid) break;
        }
    }

    // Calculate and store size + checksum
    private static void updateFileInfo(Path path, String name) {
        try {
            long size = Files.size(path);
            // Call function from Task 3
            short checksum = Task3Checksum.calculateChecksum16(path);
            String info = String.format("Size = %d bytes, Checksum = 0x%04X", size, checksum);
            deletedFileInfo.put(name, info);
        } catch (IOException e) {
            System.err.println("Error when get file info: " + e.getMessage());
        }
    }

    // Print the differences between two lists of lines
    private static void printDiff(List<String> oldLines, List<String> newLines) {
        int max = Math.max(oldLines.size(), newLines.size());
        for (int i = 0; i < max; i++) {
            String oldLine = i < oldLines.size() ? oldLines.get(i) : null;
            String newLine = i < newLines.size() ? newLines.get(i) : null;

            if (oldLine == null) {
                System.out.println("    + Add line " + (i + 1) + ": " + newLine);
            } else if (newLine == null) {
                System.out.println("    - Delete line " + (i + 1) + ": " + oldLine);
            } else if (!oldLine.equals(newLine)) {
                System.out.println("    ~ Change line " + (i + 1) + ": [" + oldLine + "] -> [" + newLine + "]");
            }
        }
    }
}
