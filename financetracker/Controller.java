package com.example.financetracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class Controller {
    @FXML //used before components so that the fxml loader can like the elements to the controller class
    //stores strings so <String>
    private ComboBox<String> monthComboBox;
    private String selectedMonth;//users choice of month


    @FXML
    private TableView<Expense> expensesTableView;

    @FXML
    private TableColumn<Expense, String> dateColumn;

    @FXML
    private TableColumn<Expense, String> nameColumn;

    @FXML
    private TableColumn<Expense, Double> amountColumn;

    @FXML
    private TableColumn<Expense, String> categoryColumn;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    private Label spendingValueLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField enterNametf;

    @FXML
    private TextField enterAmounttf;

    @FXML
    private ComboBox<String> categoryCombobox;
    private String selectedCategory;

    @FXML
    private DatePicker deletedatePicker;

    @FXML
    private TextField deleteNametf;

    @FXML
    private Button setBudget;

    @FXML
    private Button enterExpenseButton;

    @FXML
    private Button deleteExpenseButton;

    @FXML
    private Label budgetValueLabel;

    @FXML
    private Label rembudgetValueLabel;

    double budget;
    double totalSpending;


    //to set options for the combobox (dropdown). initialize doesnt need to be linked to anything - it is loaded automatically when main class is executed !!
    @FXML
    public void initialize() {

        monthComboBox.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

        //setting default value
        monthComboBox.setValue("January");

        //store user selection
        selectedMonth = monthComboBox.getValue();

        categoryCombobox.getItems().addAll("Food/drinks", "Shopping", "Groceries", "Fuel", "Bills");

        //setting default value
        categoryCombobox.setValue("Food/drinks");

        //store user selection
        selectedCategory= categoryCombobox.getValue();

        //to display previous spendings
        totalSpending = updateTotalSpending();

        //setting up tableview columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        //load expenses for default selected month
        loadExpenses();
        loadChart();
        budget = getBudget();
        displayTotalBudget(budget);
        displayRemBudget(budget, totalSpending);
    }
    public void loadExpenses() {
        String selectedMonth = getSelectedMonth();
        int monthIndex = monthComboBox.getItems().indexOf(selectedMonth) + 1;
        String sql = "SELECT date, name, amount, category FROM expenses WHERE MONTH(STR_TO_DATE(date, '%Y-%m-%d')) = ?";

        //making table
        ObservableList<Expense> expenses = FXCollections.observableArrayList();

        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monthIndex);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    String name = rs.getString("name");
                    double amount = rs.getDouble("amount");
                    String category = rs.getString("category");

                    Expense expense = new Expense(date, name, amount, category);
                    expenses.add(expense);
                }
                expensesTableView.setItems(expenses);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void loadChart() {
        String selectedMonth = getSelectedMonth();
        int monthIndex = monthComboBox.getItems().indexOf(selectedMonth) + 1;
        String sql = "SELECT category, SUM(amount) FROM expenses WHERE MONTH(STR_TO_DATE(date, '%Y-%m-%d')) = ? GROUP BY category";

        //making table
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, monthIndex);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("SUM(amount)");
                    String category = rs.getString("category");

                    pieChartData.add(new PieChart.Data(category, amount));
                }
                categoryPieChart.setData(pieChartData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //if user changes month
    public void onMonthChange() {
        selectedMonth = monthComboBox.getValue();
        totalSpending = updateTotalSpending();
        loadExpenses();
        loadChart();
        budget = getBudget();
        displayTotalBudget(budget);
        displayRemBudget(budget, totalSpending);
    }

    //since month is a private attribute, need a getter to retrieve the value to access outside controller
    public String getSelectedMonth() {
        return selectedMonth;
    }

    public void onCategoryChange() {
        selectedCategory = categoryCombobox.getValue();
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    private void clearInputFields() {
        datePicker.setValue(null);
        enterNametf.clear();
        enterAmounttf.clear();
        categoryCombobox.setValue("Food/drinks");
        deletedatePicker.setValue(null);
        deleteNametf.clear();
    }

    //connecting user data to expense table
    public void addExpense(ActionEvent event) {
        String date = datePicker.getValue().toString();
        String name = enterNametf.getText();
        String amountText = enterAmounttf.getText();
        String category = categoryCombobox.getValue();

        // Validate input
        if (date.isEmpty() || name.isEmpty() || amountText.isEmpty() || category.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input Error");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("Please fill in all fields before submitting.");
            alert.showAndWait();
            return;

        }
        try {
            //textfield data is a string, need to convert money to a double
            double amount = Double.parseDouble(amountText);

            //string in the form of a sql statement
            String sql = "INSERT INTO expenses (date, name, amount, category) VALUES (?, ?, ?, ?)";

            // makes a connection object which connects my database to java,
            // uses the conn object to make a prepared statment of the string we just created, and uses
            // setString to insert the place holder values, with numbers representing the ? position
            // execute update executes the statement
            // try with resources closes the connection in case of an error
            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, date);
                stmt.setString(2, name);
                stmt.setDouble(3, amount);
                stmt.setString(4, category);

                stmt.executeUpdate();
                System.out.println("Expense added successfully!");

                updateTotalSpending();
                clearInputFields();
                loadExpenses();
                loadChart();
                //put method to calculate remaining budget?

            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //making a method to update the total spending label
    private double updateTotalSpending() {
        String selectedMonth = getSelectedMonth();
        //converts string type to date type and then extracts the month
        String sql = "SELECT SUM(amount) FROM expenses WHERE MONTH(STR_TO_DATE(date, '%Y-%m-%d')) = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            //calculating index of the month
            int monthIndex = monthComboBox.getItems().indexOf(selectedMonth) + 1;

            pstmt.setInt(1, monthIndex); // Setting the month index in the SQL query

            //ResultSet is an object that holds the result of the query that was executed
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble(1);
                spendingValueLabel.setText(String.format("%.2f riyals", total));
                return total;
            } else {
                spendingValueLabel.setText("0.00 riyals");
                return 0.00;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    public void deleteExpense(ActionEvent event) {
        String date = deletedatePicker.getValue().toString();
        String name = deleteNametf.getText();

        // Validate input
        if (date.isEmpty() || name.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input Error");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("Please fill in all fields before submitting.");
            alert.showAndWait();
            return;

        }
        try {
            //string in the form of a sql statement
            String sql = "DELETE FROM expenses WHERE date = ? AND name = ?";

            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, date);
                stmt.setString(2, name);

                stmt.executeUpdate();
                System.out.println("Expense deleted successfully!");

                updateTotalSpending();

                clearInputFields();
                loadExpenses();
                loadChart();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void openSetBudget() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Budget.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        Image icon = new Image(Main.class.getResourceAsStream("/com/example/financetracker/saudi_riyal.jpg"));
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL); // blocks main window until closed
        stage.showAndWait(); // shows popup and waits until it's closed

        //to update the main page after setting the budget
        budget = getBudget();
        displayTotalBudget(budget);
        displayRemBudget(budget, totalSpending);
    }

    public double getBudget() {
        String month = monthComboBox.getValue();
        System.out.println(month);
        try {
            //string in the form of a sql statement
            String sql = "SELECT budget FROM budgets WHERE month = ?";

            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, month);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double budget = rs.getDouble(1);
                    return budget;
                } else {
                    return 0.0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void displayTotalBudget(double budget) {
        budgetValueLabel.setText(String.format("%.2f riyals", budget));
    }

    public void displayRemBudget(double budget, double totalSpending) {
        double remBudget = budget - totalSpending;
        rembudgetValueLabel.setText(String.format("%.2f riyals", remBudget));
    }

    public void openCategoryView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CategoryView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        Image icon = new Image(Main.class.getResourceAsStream("/com/example/financetracker/saudi_riyal.jpg"));
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();


    }

    public void openYearlyView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("YearlyView.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        Image icon = new Image(Main.class.getResourceAsStream("/com/example/financetracker/saudi_riyal.jpg"));
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }

}