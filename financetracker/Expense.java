package com.example.financetracker;

public class Expense {
    private String date;
    private String name;
    private String category;
    private Double amount;

    Expense(String date, String name, double amount, String category) {
        this.date = date;
        this.name = name;
        this.category = category;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Double getAmount() {
        return amount;
    }
}
