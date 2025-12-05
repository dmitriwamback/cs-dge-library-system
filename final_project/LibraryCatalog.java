package final_project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LibraryCatalog {

    private final Path snapshot;
    private final Map<String, Book> byIsbn = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public LibraryCatalog(Path baseDir) {
        // create the catalog file directory
        this.snapshot = baseDir.resolve("catalog.dat");
    }

    public void load() throws IOException, ClassNotFoundException {
        lock.writeLock().lock(); // lock the output access
        try {
            // clear the isbn hashmap since it could result in duplicates
            byIsbn.clear();
            // check if the file doesn't exist
            if (!Files.exists(snapshot)) {
                return;
            }
            // binary input using objectinputstream
            try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(snapshot))) {
                Object obj = inputStream.readObject();

                // check if the object is a list
                if (obj instanceof List<?> list) {
                    // iterate through all objects in the list
                    for (Object listObject : list) {
                        // check if the object is a book
                        if (listObject instanceof Book) {
                            Book b = (Book)listObject; // cast as book
                            byIsbn.put(b.getIsbn(), b); // add to the hashmap
                        }
                    }
                }
            }
        } 
        finally {
            lock.writeLock().unlock(); // unlock output access
        }
    }

    public void save() throws IOException {
        lock.readLock().lock(); // lock input access
        try {
            // create directories
            Files.createDirectories(snapshot.getParent());

            // binary output using objectoutputstream
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(snapshot))) {
                out.writeObject(new ArrayList<>(byIsbn.values())); // write the hashmap
            }
        } 
        finally {
            // unlock input access
            lock.readLock().unlock();
        }
    }

    public void add(Book book) {
        lock.writeLock().lock(); // lock output access
        try {
            byIsbn.put(book.getIsbn(), book); // add to the hashmap
        } 
        finally {
            lock.writeLock().unlock(); // unlock output access
        }
    }

    public Optional<Book> get(String isbn) {
        lock.readLock().lock(); // lock input access
        try {
            return Optional.ofNullable(byIsbn.get(isbn)); // return container of the book which might be null
        } 
        finally {
            lock.readLock().unlock(); // unlock input access
        }
    }

    public Collection<Book> all() {
        lock.readLock().lock(); // lock input access
        try {
            // return all of the books in the hashmap
            return new ArrayList<>(byIsbn.values());
        } 
        finally {
            lock.readLock().unlock(); // unlock input access
        }
    }
}