package model;

import java.time.LocalDateTime;

public class Expense {
    private String type;
    private String description;
    private double value;
    private LocalDateTime dateTime;

    public Expense(String type, String description, double value) {
        this.type = type;
        this.description = description;
        this.value = value;
        this.dateTime = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
