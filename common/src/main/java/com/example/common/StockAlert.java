package com.example.common;

public record StockAlert(String itemName, int quantity, String severity) {

    public static final String TOPIC = "bar.stock-alerts";
}
