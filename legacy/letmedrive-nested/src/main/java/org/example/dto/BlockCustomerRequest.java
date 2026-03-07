package org.example.dto;

public class BlockCustomerRequest {
    private long customerId;
    private boolean blocked;

    public BlockCustomerRequest() {
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
