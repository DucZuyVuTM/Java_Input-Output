package nio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Task1ReadFile {
    public static void main(String[] args) {
        // Get file path
        Path path = Paths.get("src/main/resources/data/suppliers.csv");

        System.out.println("=== Exercise 1: Read CSV file with NIO ===");

        try {
            // Read all content to List<String> - usually for small files
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            
            System.out.println("Number of lines: " + lines.size());
            System.out.println("----------------------------------------");
            
            for (String line : lines) {
                System.out.println(line);
            }
            
        } catch (IOException e) {
            System.err.println("Error when reading file: " + e.getMessage());
        }
    }
}
