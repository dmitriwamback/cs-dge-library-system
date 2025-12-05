package final_project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentFileLog {

    private final Path logFile;

    public StudentFileLog(Path logsDir, String studentId) {
        this.logFile = logsDir.resolve(studentId + ".bin");
    }

    // synchronized = allowed only by 1 thread at a time
    public synchronized void append(EventType type, String isbn, String title) throws IOException {
        Files.createDirectories(logFile.getParent());

        // binary output with dataoutputstream
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(logFile, Files.exists(logFile)
                                        ? java.nio.file.StandardOpenOption.APPEND
                                        : java.nio.file.StandardOpenOption.CREATE)))) {

            // get the current time
            long ts = System.currentTimeMillis();
            out.writeLong(ts); // write the time
            out.writeByte(type.ordinal()); // Rent/Return
            out.writeUTF(isbn); // write isbn
            out.writeUTF(title); // write the title
        }
    }

    public List<String> readAllPretty() throws IOException {

        // create logs arraylist
        List<String> result = new ArrayList<>();
        if (!Files.exists(logFile)) {
            return result;
        }

        // write in a specific date format
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        // binary input with datainputstream
        try (DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(logFile.toFile())))) {

            while (true) {
                try {
                    long ts         = inputStream.readLong(); // read timestamp
                    byte code       = inputStream.readByte(); // read enum Rent/Return
                    String isbn     = inputStream.readUTF(); // read ISBN
                    String title    = inputStream.readUTF(); // read title

                    EventType type = EventType.values()[code];
                    String timeStr = fmt.format(Instant.ofEpochMilli(ts));
                    result.add(timeStr + " - " + type + " - " + title + " (" + isbn + ")"); // formatting
                } 
                catch (EOFException eof) {
                    break;
                }
            }
        }
        return result;
    }
}