package final_project;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

public class LibraryService {

    private final BinaryStudentRegistry registry;
    private final LibraryCatalog catalog;
    private final Path logsDir;
    private final ExecutorService pool;

    // studentId -> set of ISBNs they currently have rented
    private final ConcurrentMap<String, Set<String>> activeRentals = new ConcurrentHashMap<>();

    public LibraryService(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
        } 
        catch (IOException ignored) {}

        this.registry = new BinaryStudentRegistry(dataDir);
        this.catalog  = new LibraryCatalog(dataDir);
        this.logsDir  = dataDir.resolve("logs");
        this.pool     = Executors.newCachedThreadPool();

    }

    public void initDemoData() {
        try {
            registry.load();
            catalog.load();
        } 
        catch (Exception e) {}

        for (Student s : registry.listAll()) {
            rebuildActiveRentalsForStudent(s.getId());
        }
    }

    public BinaryStudentRegistry getRegistry() {
        return registry;
    }

    public LibraryCatalog getCatalog() {
        return catalog;
    }

    public boolean rentBook(String studentId, String isbn) {
        try {
            // threaded student lookup via RecordMatcher
            RecordMatcher matcher = new RecordMatcher(registry, studentId);
            Optional<Student> studentOpt = matcher.call();
            if (studentOpt.isEmpty()) {
                return false;
            }

            Optional<Book> bookOpt = catalog.get(isbn);
            if (bookOpt.isEmpty()) {
                return false;
            }

            // per‑student constraint: cannot rent same ISBN twice
            Set<String> studentRentals = rentalsFor(studentId);
            if (studentRentals.contains(isbn)) {
                // student already has this book
                return false;
            }

            Book book = bookOpt.get();
            if (!book.isAvailable()) {
                return false;
            }

            // mutate inventory
            book.checkout();
            catalog.add(book);
            catalog.save();

            // record that this student now holds this ISBN
            studentRentals.add(isbn);

            // append RENT log
            StudentFileLog log = new StudentFileLog(logsDir, studentId);
            log.append(EventType.RENT, isbn, book.getTitle());

            return true;
        } 
        catch (Exception e) {
            return false;
        }
    }

    public boolean returnBook(String studentId, String isbn) {
        try {
            Optional<Student> studentOpt = registry.findById(studentId);
            if (studentOpt.isEmpty()) {
                return false;
            }

            Optional<Book> bookOpt = catalog.get(isbn);
            if (bookOpt.isEmpty()) {
                return false;
            }

            // per‑student constraint: must actually have this ISBN
            Set<String> studentRentals = rentalsFor(studentId);
            if (!studentRentals.contains(isbn)) {
                // this student never rented (or already returned) this book
                return false;
            }

            Book book = bookOpt.get();
            if (book.getCheckedOut() == 0) {
                // global safety check
                return false;
            }

            // mutate inventory
            book.checkin();
            catalog.add(book);
            catalog.save();

            // remove from student's current rentals
            studentRentals.remove(isbn);

            // append RETURN log
            StudentFileLog log = new StudentFileLog(logsDir, studentId);
            log.append(EventType.RETURN, isbn, book.getTitle());

            return true;
        } 
        catch (Exception e) {
            return false;
        }
    }

    private Set<String> rentalsFor(String studentId) {
        return activeRentals.computeIfAbsent(studentId, id -> ConcurrentHashMap.newKeySet());
    }

    private void rebuildActiveRentalsForStudent(String studentId) {

        // path for student log
        Path studentLogFile = logsDir.resolve(studentId + ".bin");
        if (!Files.exists(studentLogFile)) return;

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(studentLogFile)))) {

            // local map: isbn -> net count
            java.util.Map<String, Integer> counts = new java.util.HashMap<>();

            while (true) {
                try {
                    long ts   = in.readLong(); // timestamp
                    byte code = in.readByte(); // enum type
                    String isbn  = in.readUTF(); // isbn
                    String title = in.readUTF(); // title

                    EventType type = EventType.values()[code];
                    counts.merge(isbn, type == EventType.RENT ? 1 : -1, Integer::sum);
                } catch (EOFException eof) {
                    break;
                }
            }

            // any isbn with count > 0 is currently held by this student
            java.util.Set<String> rentals = rentalsFor(studentId);
            rentals.clear();
            counts.forEach((isbn, c) -> {
                if (c > 0) rentals.add(isbn);
            });

        } 
        catch (IOException e) {}
    }


    public Path getDataDir() {
        return logsDir;
    }

    public void shutdown() {
        pool.shutdown();
    }
}
