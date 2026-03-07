package org.example.repository;

import org.example.model.Customer;
import org.example.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CustomerRepository {
    public long create(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers(name, phone, license_number, email, password_hash, role) " +
            "VALUES(?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getLicenseNumber());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getPasswordHash());
            ps.setString(6, customer.getRole());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        }
    }

    public Optional<Customer> findById(Connection conn, long customerId) throws SQLException {
        String sql = "SELECT id, name, phone, license_number, email, password_hash, role, is_blocked, created_at " +
            "FROM customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapCustomer(rs));
            }
        }
    }

    public Optional<Customer> authenticate(String email, String passwordHash) throws SQLException {
        String sql = "SELECT id, name, phone, license_number, email, password_hash, role, is_blocked, created_at " +
            "FROM customers WHERE LOWER(email) = LOWER(?) AND password_hash = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapCustomer(rs));
            }
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM customers WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean setBlocked(long customerId, boolean blocked) throws SQLException {
        String sql = "UPDATE customers SET is_blocked = ? WHERE id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, blocked);
            ps.setLong(2, customerId);
            return ps.executeUpdate() == 1;
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setPhone(rs.getString("phone"));
        customer.setLicenseNumber(rs.getString("license_number"));
        customer.setEmail(rs.getString("email"));
        customer.setPasswordHash(rs.getString("password_hash"));
        customer.setRole(rs.getString("role"));
        customer.setBlocked(rs.getBoolean("is_blocked"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return customer;
    }
}
