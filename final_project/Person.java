package final_project;

import java.io.Serializable;

public abstract class Person implements Serializable {

    // default parameters
    private final String id;
    private String name;

    // constructors
    protected Person(String id, String name) {

        // input validation
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be empty");
        }

        // set values
        this.id = id;
        this.name = name;
    }

    // getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // setters
    public void setName(String name) {
        this.name = name;
    }
}
