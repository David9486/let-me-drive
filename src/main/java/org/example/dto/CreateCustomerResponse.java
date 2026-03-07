package org.example.dto;

public class CreateCustomerResponse {
    private long customerId;

    public CreateCustomerResponse() {
    }

    public CreateCustomerResponse(long customerId) {
        this.customerId = customerId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }
}
