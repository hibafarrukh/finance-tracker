package com.example.financetracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BudgetController {
    @FXML
    private ComboBox<String> budgetComboBox;
    private String selectedBudgetMonth;//users choice of month

    @FXML
    private TextField enterBudget;

    @FXML
    private Button insertBudget;

    public void initialize() {

        budgetComboBox.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

        //setting default value
        budgetComboBox.setValue("January");

        //store user selection
        selectedBudgetMonth = budgetComboBox.getValue();

    }

    public void onMonthChange() {
        selectedBudgetMonth = budgetComboBox.getValue();
    }

    public void enterBudget(ActionEvent event) {
        String month = budgetComboBox.getValue();
        String amountText = enterBudget.getText();

        if (month.isEmpty() || amountText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Input Error");
            alert.setHeaderText("Missing Fields");
            alert.setContentText("Please fill in all fields before submitting.");
            alert.showAndWait();
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String sql = "INSERT INTO Budgets(month, budget) VALUES (?, ?)";

            try (Connection conn = DatabaseManager.connect();
                 PreparedStatement pstm = conn.prepareStatement(sql)) {
                pstm.setString(1, month);
                pstm.setDouble(2, amount);

                pstm.executeUpdate();
            }

        } catch (NumberFormatException e) {
            System.out.println("invalid amount format");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
