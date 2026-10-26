package nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Task3Checksum {

    public static void main(String[] args) {
        Path path = Paths.get("src/main/resources/data/suppliers.csv");

        System.out.println("=== Exercise 3: Calculate 16-bit checksum with bytebuffer ===");
        System.out.println("File: " + path.getFileName());

        try {
            short checksum = calculateChecksum16(path);
            
            // Print HEX format
            System.out.printf("Checksum (Hex): 0x%04X%n", checksum);
            // Print DEC format
            System.out.println("Checksum (Dec): " + Short.toUnsignedInt(checksum));
            
        } catch (IOException e) {
            System.err.println("Error when reading file: " + e.getMessage());
        }
    }

    /**
     * Calculate Internet Checksum 16-bit (RFC 1071)
     */
    public static short calculateChecksum16(Path path) throws IOException {
        long sum = 0;

        // Open FileChannel to read file with ByteBuffer
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            // Allocate 8KB Buffer (8192 bytes)
            ByteBuffer buffer = ByteBuffer.allocate(8192);

            while (channel.read(buffer) != -1) {
                // Flip buffer to read mode
                buffer.flip();

                // Read every byte pair (16-bit)
                while (buffer.remaining() >= 2) {
                    // Get 2 bytes and merge into a 16-bit number (Data type Short)
                    // `& 0xFFFF` to avoid overflow when number is negative
                    int word = buffer.getShort() & 0xFFFF;
                    sum += word;

                    // Return carry back to 16 bit
                    if ((sum & 0xFFFF0000) != 0) {
                        sum = (sum & 0xFFFF) + (sum >> 16);
                    }
                }

                // If byte amount is odd, last byte has to be added with 1 byte 0
                if (buffer.remaining() == 1) {
                    int lastByte = buffer.get() & 0xFF;
                    sum += (lastByte << 8);

                    if ((sum & 0xFFFF0000) != 0) {
                        sum = (sum & 0xFFFF) + (sum >> 16);
                    }
                }

                buffer.clear();
            }
        }

        // One's complement
        return (short) ~sum;
    }
}
