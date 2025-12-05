package final_project;

public class Book extends Item implements Rentable {

    private int totalCopies;
    private int checkedOut;
    private int available;

    // default constructor
    public Book(String isbn, String title, int totalCopies) {
        this(isbn, title, totalCopies, 0); // call the secondary constructor
    }


    public Book(String isbn, String title, int totalCopies, int checkedOut) {
        super(isbn, title); // call the Item

        // input validation
        if (totalCopies < 0 || checkedOut < 0 || checkedOut > totalCopies) {
            throw new IllegalArgumentException("Invalid copy counts");
        }

        // set values
        this.totalCopies = totalCopies;
        this.checkedOut = checkedOut;
        this.available = totalCopies;
    }

    // overridden voids
    @Override
    public boolean isAvailable() {
        return checkedOut < totalCopies;
    }

    @Override
    public void checkout() {
        if (!isAvailable()) {
            return;
        }
        checkedOut++;
        available--;
    }

    @Override
    public void checkin() {
        if (checkedOut == 0) {
            throw new IllegalStateException("Nothing to return");
        }
        checkedOut--;
        available++;
    }

    // getters

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getCheckedOut() {
        return checkedOut;
    }

    public int getAvailable() {
        return totalCopies - checkedOut;
    }

    // set totalCopies
    public void setTotalCopies(int totalCopies) {
        if (totalCopies < checkedOut) {
            throw new IllegalArgumentException("totalCopies cannot be less than checkedOut");
        }
        this.totalCopies = totalCopies;
    }

    @Override
    public String toString() {
        return isbn + " - " + title + " (" + getAvailable() + "/" + totalCopies + " available)";
    }
}