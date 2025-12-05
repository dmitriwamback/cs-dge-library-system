package final_project;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

public class MainApp extends Application {

    // library service: to store students (BinaryStudentRegistry) and books (LibraryCatalog)
    private LibraryService service;

    // ObservableList for students, books, and logs (rent/return)
    private final ObservableList<Student> studentData = FXCollections.observableArrayList();
    private final ObservableList<Book> bookData = FXCollections.observableArrayList();
    private final ObservableList<String> logData = FXCollections.observableArrayList();

    // TableView for displaying rows/columns of students and books
    private TableView<Student> studentTable;
    private TableView<Book> bookTable;
    // ListView to display student logs
    private ListView<Student> studentListView;
    private ListView<String> logListView;

    @Override
    public void start(Stage stage) {

        // initialize library service
        service = new LibraryService(Path.of("data"));
        service.initDemoData();
        refreshStudents();
        refreshBooks();

        // create main borderpane
        BorderPane root = new BorderPane();

        // set the menu bar on top
        root.setTop(createMenuBar(stage));
        root.setCenter(createTabPane());

        // create scene
        Scene scene = new Scene(root, 900, 500);
        stage.setTitle("Campus Library Management System");
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar(Stage stage) {

        // create the menu button 'File'
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> { // lambda expression
            service.shutdown();
            Platform.exit();
        });
        fileMenu.getItems().add(exitItem); // add the exit item to the file menu 

        // repeat for student (2 menu options: add student and list student)
        Menu studentMenu = new Menu("Student");
        MenuItem addStudentItem = new MenuItem("Add Student");
        addStudentItem.setOnAction(e -> showAddStudentDialog());

        MenuItem listStudentsItem = new MenuItem("List Students");
        listStudentsItem.setOnAction(e -> refreshStudents());
        studentMenu.getItems().addAll(addStudentItem, listStudentsItem);
        
        // repeat for book (2 menu options: add book and list book)
        Menu bookMenu = new Menu("Book");
        MenuItem addBookItem = new MenuItem("Add Book");
        addBookItem.setOnAction(e -> showAddBookDialog());
        MenuItem listBooksItem = new MenuItem("List Books");
        listBooksItem.setOnAction(e -> refreshBooks());
        bookMenu.getItems().addAll(addBookItem, listBooksItem);

        // repeat for rental menu (2 menu options: rent book and return book)
        Menu rentalMenu = new Menu("Rental");
        MenuItem rentItem = new MenuItem("Rent Book");
        rentItem.setOnAction(e -> showRentDialog());
        MenuItem returnItem = new MenuItem("Return Book");
        returnItem.setOnAction(e -> showReturnDialog());
        rentalMenu.getItems().addAll(rentItem, returnItem);

        // return the menubar
        return new MenuBar(fileMenu, studentMenu, bookMenu, rentalMenu);
    }

    private TabPane createTabPane() {

        // create individual panes for each section
        TabPane tabPane = new TabPane();

        // tab pane for students
        Tab studentsTab = new Tab("Student Records");
        studentsTab.setClosable(false);
        studentsTab.setContent(createStudentTabContent());

        // tab pane for books
        Tab booksTab = new Tab("Book Catalog");
        booksTab.setClosable(false);
        booksTab.setContent(createBookTabContent());

        // tab pane for logs
        Tab logsTab = new Tab("Student Logs");
        logsTab.setClosable(false);
        logsTab.setContent(createLogTabContent());

        // add all the pane to the tapPane
        tabPane.getTabs().addAll(studentsTab, booksTab, logsTab);
        return tabPane;
    }

    private VBox createStudentTabContent() {
        studentTable = new TableView<>(studentData);

        // create a column for the studentID and link it via PropertyValueFactory
        TableColumn<Student, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // create a column for the name and link it via PropertyValueFactory
        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // create a column for the program and link it via PropertyValueFactory
        TableColumn<Student, String> programColumn = new TableColumn<>("Program");
        programColumn.setCellValueFactory(new PropertyValueFactory<>("program"));

        // create a column for the year and link it via PropertyValueFactory
        TableColumn<Student, Integer> yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        // add all the columns to the table and make them all have the same width
        studentTable.getColumns().addAll(idColumn, nameColumn, programColumn, yearColumn);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        GridPane pane = new GridPane();

        HBox form = new HBox(10);   // horizontal gap 10
        form.setAlignment(Pos.CENTER);

        // text fields with prompts
        TextField idField = new TextField();
        idField.setPromptText("Student ID");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField programField = new TextField();
        programField.setPromptText("Program");

        TextField yearField = new TextField();
        yearField.setPromptText("Year");

        // button
        Button addButton = new Button("Add Student");

        // button handler
        addButton.setOnAction(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String program = programField.getText().trim();
            String yearText = yearField.getText().trim();

            Student student = createNewStudent(id, name, program, yearText);
            if (student != null) service.getRegistry().addOrUpdateStudent(student);
            refreshStudents();
        });

        // add controls to the horizontal form
        form.getChildren().addAll(idField, nameField, programField, yearField, addButton);

        // add form to your pane (e.g., below your label)
        pane.add(form, 0, 1);

        // add to a vbox and set internal padding of 8 units
        VBox box = new VBox(5, studentTable);
        box.getChildren().add(pane);
        box.setPadding(new Insets(8));
        return box;
    }

    private VBox createBookTabContent() {
        bookTable = new TableView<>(bookData);

        // create a column for the isbn and link it via PropertyValueFactory
        TableColumn<Book, String> isbnColumn = new TableColumn<>("ISBN");
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        // create a column for the title and link it via PropertyValueFactory
        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // create a column for the total count and link it via PropertyValueFactory
        TableColumn<Book, Integer> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));

        // create a column for the checked out and link it via PropertyValueFactory
        TableColumn<Book, Integer> checkedColumn = new TableColumn<>("Checked Out");
        checkedColumn.setCellValueFactory(new PropertyValueFactory<>("checkedOut"));

        // create a column for the available count and link it via PropertyValueFactory
        TableColumn<Book, Integer> availableColumn = new TableColumn<>("Available");
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));

        // add all the columns to the table and make them all have the same width
        bookTable.getColumns().addAll(isbnColumn, titleColumn, totalColumn, checkedColumn, availableColumn);
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);






        GridPane pane = new GridPane();

        HBox form = new HBox(10);   // horizontal gap 10
        form.setAlignment(Pos.CENTER);

        // text fields with prompts
        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField copiesField = new TextField();
        copiesField.setPromptText("Copies");

        // button
        Button addButton = new Button("Add Book");

        // button handler
        addButton.setOnAction(e -> {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String copies = copiesField.getText().trim();

            Book book = createNewBook(isbn, title, copies);
            if (book != null) service.getCatalog().add(book);
        });

        // add controls to the horizontal form
        form.getChildren().addAll(isbnField, titleField, copiesField, addButton);

        // add form to your pane (e.g., below your label)
        pane.add(form, 0, 1);


        // add to a vbox and set internal padding of 8 units
        VBox box = new VBox(bookTable);
        box.getChildren().add(pane);
        box.setPadding(new Insets(8));
        return box;
    }

    private SplitPane createLogTabContent() {

        // create list view of students and logs (for the log section)
        studentListView = new ListView<>(studentData);
        logListView = new ListView<>(logData);

        // when the studentListView has one of its elements selected, load the logs for the selected (sel) using its id
        studentListView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {

            // check if the selection is not null and if not load the logs
            if (sel != null) {
                loadLogsFor(sel.getId());
            } 
            else {
                logData.clear();
            }
        });

        // create a split pane for the students and logs
        SplitPane splitPane = new SplitPane(studentListView, logListView);
        splitPane.setDividerPositions(0.3); // set boundary
        splitPane.setPadding(new Insets(8)); // set padding
        return splitPane;
    }

    private void refreshStudents() {
        // update student list
        studentData.setAll(service.getRegistry().listAll());
    }

    private void refreshBooks() {
        // update book list
        bookData.setAll(service.getCatalog().all());
    }

    private void loadLogsFor(String studentId) {

        // create threadpool to load the logs for the student
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                // create StudentFileLog which uses thread-safe access to the appropriate log file
                StudentFileLog log = new StudentFileLog(service.getDataDir(), studentId);
                List<String> lines = log.readAllPretty(); // get logs from the file

                // notify the javafx thread when the threadpool is done
                Platform.runLater(() -> {
                    logData.setAll(lines);
                });
            } 
            catch (Exception ex) {
                // notify the javafx thread when the threadpool is done
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Failed to read logs: " + ex.getMessage()));
            }
        });
    }

    private void showAddStudentDialog() {

        // create dialog to add students
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add Student");

        // create textfields for each value (id, name, program, year)
        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField programField = new TextField();
        TextField yearField = new TextField();

        // create gridpane to align the labels and textfields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("ID:"), idField);
        grid.addRow(1, new Label("Name:"), nameField);
        grid.addRow(2, new Label("Program:"), programField);
        grid.addRow(3, new Label("Year:"), yearField);

        // set the gridpane to the dialog and create OK/CANCEL buttons
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // handle button presses
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) { // if the button is OK
                return createNewStudent(idField.getText(), nameField.getText(), programField.getText(), yearField.getText());
            }
            return null;
        });

        // display the dialog
        dialog.showAndWait().ifPresent(s -> {
            // add the student when the ok button is pressed (s)
            service.getRegistry().addOrUpdateStudent(s);
            try {
                // save the file
                service.getRegistry().save();
            } 
            catch (Exception ignored) {}
            refreshStudents(); // update the student list
        });
    }

    private void showAddBookDialog() {

        // create dialog for adding book
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add Book");

        // create textfields for isbn, title, and copies
        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        TextField copiesField = new TextField();

        // create gridpane for the textfields and the dialog
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("ISBN:"), isbnField);
        grid.addRow(1, new Label("Title:"), titleField);
        grid.addRow(2, new Label("Total Copies:"), copiesField);

        // set the gridpane into the dialog
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // handle button presses
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) { // ok button pressed
                // try-catch for parsing string to int
                return createNewBook(isbnField.getText(), titleField.getText(), copiesField.getText());
            }
            return null;
        });

        // show the dialog and wait for the book b to be processed
        dialog.showAndWait().ifPresent(b -> {
            service.getCatalog().add(b); // add the book
            try {
                service.getCatalog().save(); // save to the catalog
            } 
            catch (Exception ignored) {}
            refreshBooks();
        });
    }

    private Book createNewBook(String isbn, String title, String copies) {

        // try-catch for parsing string to int
        try {

            // try-catch to avoid exceptions from loading invalid books
            try {
                // check if the book by isbn exists
                if (!service.getCatalog().get(isbn).isEmpty()) {
                    showAlert(AlertType.ERROR, "Book with this ISBN already exists");
                    return null;
                }
            }
            catch (Exception e) {}

            // parse the total copies
            int total = Integer.parseInt(copies);
            return new Book(isbn, title, total);
        } 
        catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Total copies must be an integer");
        }

        return null;
    }

    private Student createNewStudent(String id, String name, String program, String yearstr) {

        try {

            try {
                // check if the studentID already exists
                if (service.getRegistry().findById(id).get() != null) {
                    showAlert(AlertType.ERROR, "Student with this ID already exists");
                    return null;
                }
            }
            catch (Exception e) {} // if we catch an exception, then the program runs since the findById will yield null

            // parse the year
            int year = Integer.parseInt(yearstr);
            return new Student(id, name, program, year);
        } 
        catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Year must be an integer");
        }

        return null;
    }

    private void showRentDialog() {
        // create dialog to rent books
        Dialog<String[]> rentalDialog = new Dialog<>();
        rentalDialog.setTitle("Rent Book");

        // create appropriate textfields for studentID and ISBN
        TextField studentIDField = new TextField();
        TextField isbnField = new TextField();

        // create gridpane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Student ID:"), studentIDField);
        grid.addRow(1, new Label("ISBN:"), isbnField);

        // set teh gridpane to the dialog
        rentalDialog.getDialogPane().setContent(grid);
        rentalDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // handle button presses
        rentalDialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) { // ok button
                return new String[] {
                    studentIDField.getText().trim(), // studentID
                    isbnField.getText().trim() // ISBN
                };
            }
            return null;
        });

        // show the dialog and wait for the String[] (student id, isbn) to be processed
        rentalDialog.showAndWait().ifPresent(values -> {
            String studentId = values[0];
            String isbn = values[1];

            // check if the studentId or ISBN are valid
            if (studentId.isEmpty() || isbn.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Student ID and ISBN are required");
                return;
            }

            // create threads
            var taskExecutor = Executors.newSingleThreadExecutor();
            taskExecutor.submit(() -> {
                // thread-safe way to rent the books
                boolean ok = service.rentBook(studentId, isbn);
                Platform.runLater(() -> { // notify the javafx application thread
                    if (!ok) { // cannot rent the book
                        showAlert(Alert.AlertType.ERROR, "Rent failed");
                    } 
                    else {
                        // refresh the book list
                        refreshBooks();

                        // check if the studentListView is selected and that the id is the same as the current id
                        if (studentListView.getSelectionModel().getSelectedItem() != null && 
                            studentListView.getSelectionModel().getSelectedItem().getId().equals(studentId)) {
                            loadLogsFor(studentId);
                        }
                    }
                });
            });
        });
    }

    private void showReturnDialog() {
        // create dialog to return books
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Return Book");

        // create appropriate textfields (studentID, ISBN)
        TextField studentIDField = new TextField();
        TextField isbnField = new TextField();

        // create gridpane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Student ID:"), studentIDField);
        grid.addRow(1, new Label("ISBN:"), isbnField);

        // set the gridpane to the dialog
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // button handling
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) { // ok button
                // return the studentID and ISBN
                return new String[] {
                    studentIDField.getText().trim(),
                    isbnField.getText().trim()
                };
            }
            return null;
        });

        // display the dialog
        dialog.showAndWait().ifPresent(values -> {

            // get the studentID and isbn
            String studentId = values[0];
            String isbn = values[1];

            // check if the id and isbn are valid
            if (studentId.isEmpty() || isbn.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Student ID and ISBN are required");
                return;
            }

            // multithreading
            var taskExecutor = Executors.newSingleThreadExecutor();

            // run thread
            taskExecutor.submit(() -> {
                boolean ok = service.returnBook(studentId, isbn); // return book method
                Platform.runLater(() -> {
                    if (!ok) { // if we cannot rent the book
                        showAlert(Alert.AlertType.ERROR, "Return failed");
                    } 
                    else {
                        // refresh the book list
                        refreshBooks();
                        // update the logs (if selected)
                        if (studentListView.getSelectionModel().getSelectedItem() != null &&
                            studentListView.getSelectionModel().getSelectedItem().getId().equals(studentId)) {
                            loadLogsFor(studentId);
                        }
                    }
                });
            });
        });
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        service.shutdown();
    }

    public static void main(String[] args) {
        launch();
    }
}