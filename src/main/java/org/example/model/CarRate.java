package org.example.model;

import java.math.BigDecimal;

public class CarRate {
    private long carId;
    private BigDecimal pricePerHour;
    private BigDecimal lateFeePerHour;

    public CarRate() {
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
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
}
