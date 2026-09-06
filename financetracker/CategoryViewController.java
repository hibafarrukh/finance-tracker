package com.example.financetracker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryViewController {
    @FXML
    private TableView<Expense> categoryTableView;
    @FXML
    private ComboBox<String> categoryViewCombobox;
    private String selectedCategoryView;
    @FXML
    private TableColumn<Expense, String> dateColumn;

    @FXML
    private TableColumn<Expense, String> nameColumn;

    @FXML
    private TableColumn<Expense, Double> amountColumn;

    @FXML
    private TableColumn<Expense, String> categoryColumn;

    public void initialize() {
        categoryViewCombobox.getItems().addAll("Food/drinks", "Shopping", "Groceries", "Fuel", "Bills");
        categoryViewCombobox.setValue("Food/drinks");
        selectedCategoryView = categoryViewCombobox.getValue();

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        loadExpensesCategory();
    }

    public String getCategory() {
        return categoryViewCombobox.getValue();
    }
    public void onCategoryChange() {
        selectedCategoryView = getCategory();
        loadExpensesCategory();
    }

    public void loadExpensesCategory() {
        String selectedCategoryView = getCategory();
        String sql = "SELECT date, name, amount, category FROM expenses WHERE category = ?";

        //making table
        ObservableList<Expense> expensesCategory = FXCollections.observableArrayList();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, selectedCategoryView);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    String name = rs.getString("name");
                    double amount = rs.getDouble("amount");
                    String category = rs.getString("category");

                    Expense expense = new Expense(date, name, amount, category);
                    expensesCategory.add(expense);
                }
                categoryTableView.setItems(expensesCategory);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
