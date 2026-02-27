package model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class Sale {
    private Map<Product, Integer> items;
    private double totalValue;
    private PaymentMethod paymentMethod;
    private LocalDateTime dateTime;

    public Sale(Map<Product, Integer> items, double totalValue, PaymentMethod paymentMethod) {
        this.items = Map.copyOf(items);
        this.totalValue = totalValue;
        this.paymentMethod = paymentMethod;
        this.dateTime = LocalDateTime.now();
    }

    public Map<Product, Integer> getItems() {
        return items;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getProductsSummary() {
        return items.entrySet().stream()
                .map(entry -> entry.getKey().getName() + " (x" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }
}
