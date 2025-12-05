package final_project;

public class Student extends Person {

    // default parameters
    private String program;
    private int year;

    // default constructor
    public Student(String id, String name, String program, int year) {
        super(id, name);
        this.program = program;
        this.year = year;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return getId() + " - " + getName() + " (" + program + " " + year + ")";
    }
}
