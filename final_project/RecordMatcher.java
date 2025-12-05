package final_project;

import java.util.Optional;
import java.util.concurrent.Callable;

public class RecordMatcher implements Callable<Optional<Student>> {

    // default parameters
    private final BinaryStudentRegistry registry;
    private final String studentId;

    // default constructor
    public RecordMatcher(BinaryStudentRegistry registry, String studentId) {
        this.registry = registry;
        this.studentId = studentId;
    }

    // overridden call function 
    @Override
    public Optional<Student> call() {
        return registry.findById(studentId);
    }
}