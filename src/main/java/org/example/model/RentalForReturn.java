package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RentalForReturn {
    private long rentalId;
    private LocalDateTime expectedReturnTime;
    private BigDecimal baseAmount;
    private BigDecimal lateFeePerHour;

    public RentalForReturn() {
    }

    public long getRentalId() {
        return rentalId;
    }

    public void setRentalId(long rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getLateFeePerHour() {
        return lateFeePerHour;
    }

    public void setLateFeePerHour(BigDecimal lateFeePerHour) {
        this.lateFeePerHour = lateFeePerHour;
    }
}
