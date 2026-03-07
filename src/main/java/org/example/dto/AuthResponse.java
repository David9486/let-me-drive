package org.example.dto;

public class AuthResponse {
    private long customerId;
    private String name;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(long customerId, String name, String email, String role) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
