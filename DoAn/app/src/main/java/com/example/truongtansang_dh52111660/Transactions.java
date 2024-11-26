package com.example.truongtansang_dh52111660;

import java.sql.Date;

public class Transactions {
    private int transaction_id;
    private Double amount;
    private String date;
    private String Category;
    private String description;
    private int category_id;
    private int user_id;



    public Transactions() {
    }

    public Transactions(int transaction_id, Double amount, String date, String category, String description) {
        this.transaction_id = transaction_id;
        this.amount = amount;
        this.date = date;
        Category = category;
        this.description = description;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
