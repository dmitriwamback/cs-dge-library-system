package final_project;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BinaryStudentRegistry {

    private final Path dataFile;
    private final Map<String, Student> indexById = new HashMap<>(); // hashmap for studentId => Student Object
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // lock that allows thread-safety resource access

    // default constructor
    public BinaryStudentRegistry(Path baseDir) {
        this.dataFile = baseDir.resolve("students.dat");
    }

    // load function
    public void load() throws IOException, ClassNotFoundException {

        // don't allow writing by locking the thread
        lock.writeLock().lock();


        try {
            // clear the hashmap
            indexById.clear();

            // check if the path exists
            if (!Files.exists(dataFile)) {
                return;
            }

            // read the file using ObjectInputStream
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(dataFile.toFile()))) {
                Object obj = inputStream.readObject(); // read the object

                // suppose we don't know the object but it's a list
                if (obj instanceof List<?> list) {
                    for (Object listObject : list) { // iterate through all elements
                        if (listObject instanceof Student) { // check if the object is a student
                            Student s = (Student)listObject; // cast
                            indexById.put(s.getId(), s); // populate the hashmap
                        }
                    }
                }
            }
        } 
        finally {
            // unlock the writing
            lock.writeLock().unlock();
        }
    }

    public void save() throws IOException {
        // only allow writing by locking reading
        lock.readLock().lock();
        try {
            // create the directories
            Files.createDirectories(dataFile.getParent());

            // create objectoutputstream to write the student object into the datafile
            try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataFile.toFile()))) {
                outputStream.writeObject(new ArrayList<>(indexById.values()));
            }
        } 
        finally {
            // unlock the reading lock
            lock.readLock().unlock();
        }
    }

    public void addOrUpdateStudent(Student s) {
        lock.writeLock().lock(); // lock the output so we can save to the file
        try {
            // try to create hashmap element
            indexById.put(s.getId(), s);
        } 
        finally {
            // allow writing
            lock.writeLock().unlock();
        }
    }

    public Optional<Student> findById(String id) {
        lock.readLock().lock(); // lock the input
        try {
            // create an Optional to hold students that could be null
            return Optional.ofNullable(indexById.get(id));
        } 
        finally {
            // unlock reading
            lock.readLock().unlock();
        }
    }

    public List<Student> listAll() {
        // lock reading
        lock.readLock().lock();
        try {
            // return all of the student objects
            return new ArrayList<>(indexById.values());
        } 
        finally {
            // unlock reading
            lock.readLock().unlock();
        }
    }
}