package model;

import utils.Utils;

public class Product {
    private static int count = 1;

    private int id;
    private String name;
    private Double price;
    private int stock;

    public Product (String name, Double price, int stock) {
        this.id = count;
        this.name = name;
        this.price = price;
        this.stock = stock;
        Product.count += 1;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String toString() {
        return "Id: " + this.getId() +
                "\nNome: " + this.getName() +
                "\nPreco: " + Utils.doubleToString(this.getPrice()) +
                "\nEstoque: " + this.getStock();
    }
}
