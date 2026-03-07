package org.example.dto;

public class BlockCustomerResponse {
    private long customerId;
    private boolean blocked;

    public BlockCustomerResponse() {
    }

    public BlockCustomerResponse(long customerId, boolean blocked) {
        this.customerId = customerId;
        this.blocked = blocked;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
