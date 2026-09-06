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

public class YearlyViewController {
    @FXML
    private TableView<Expense> yearlyTableView;

    @FXML
    private ComboBox<String> yearlyViewCombobox;
    private String selectedYearlyView;
    @FXML
    private TableColumn<Expense, String> dateColumn;

    @FXML
    private TableColumn<Expense, String> nameColumn;

    @FXML
    private TableColumn<Expense, Double> amountColumn;

    @FXML
    private TableColumn<Expense, String> categoryColumn;

    public void initialize() {
        yearlyViewCombobox.getItems().addAll("2024", "2025", "2026");
        yearlyViewCombobox.setValue("2025");

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        loadExpensesCategory();
    }

    public void loadExpensesCategory() {
        //add selected month logic
        String sql = "SELECT date, name, amount, category FROM expenses ORDER BY amount";

        //making table
        ObservableList<Expense> expensesCategory = FXCollections.observableArrayList();

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    String name = rs.getString("name");
                    double amount = rs.getDouble("amount");
                    String category = rs.getString("category");

                    Expense expense = new Expense(date, name, amount, category);
                    expensesCategory.add(expense);
                }
                yearlyTableView.setItems(expensesCategory);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
