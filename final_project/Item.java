package final_project;

import java.io.Serializable;

public abstract class Item implements Serializable {

    // parameters
    protected final String isbn;
    protected final String title;

    // item constructor
    protected Item(String isbn, String title) {
        // input validation
        if (isbn == null || isbn.isBlank() || title == null || title.isBlank()) {
            throw new IllegalArgumentException("isbn must not be empty");
        }
        // set values
        this.isbn = isbn;
        this.title = title;
    }

    // getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }
}