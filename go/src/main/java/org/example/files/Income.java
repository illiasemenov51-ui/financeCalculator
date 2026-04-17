package org.example.files;

import java.time.LocalDate;

public class Income {
    private int id;
    private int userId;
    private double amount;
    private String description;
    private String category;   // "umowa_o_prace", "b2b", "zlecenie", "inne"
    private LocalDate date;

    public Income() {}

    public Income(int userId, double amount, String description, String category, LocalDate date) {
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return String.format("  [%d] %s | %.2f PLN | %s | %s",
                id, date, amount, category,
                description != null ? description : "");
    }
}
