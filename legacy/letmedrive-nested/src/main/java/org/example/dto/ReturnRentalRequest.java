package org.example.dto;

import java.time.LocalDateTime;

public class ReturnRentalRequest {
    private long rentalId;
    private LocalDateTime actualReturnTime;

    public ReturnRentalRequest() {
    }

    public long getRentalId() {
        return rentalId;
    }

    public void setRentalId(long rentalId) {
        this.rentalId = rentalId;
    }

    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }
}
