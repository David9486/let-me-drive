package org.example.model;

import java.math.BigDecimal;

public class Car {
    private long id;
    private String carName;
    private String brand;
    private int seatCount;
    private String premiumLevel;
    private BigDecimal pricePerHour;
    private BigDecimal lateFeePerHour;
    private boolean active;

    public Car() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public String getPremiumLevel() {
        return premiumLevel;
    }

    public void setPremiumLevel(String premiumLevel) {
        this.premiumLevel = premiumLevel;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public BigDecimal getLateFeePerHour() {
        return lateFeePerHour;
    }

    public void setLateFeePerHour(BigDecimal lateFeePerHour) {
        this.lateFeePerHour = lateFeePerHour;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
