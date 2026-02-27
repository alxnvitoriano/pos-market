package model;

public enum PaymentMethod {
    PIX("PIX"),
    DINHEIRO("Dinheiro"),
    CARTAO("Cartão"),
    CARNE("Carnê");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
