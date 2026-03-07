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
        String sql = "INSERT INTO customers(name, phone, license_number) VALUES(?, ?, ?) RETURNING id";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getLicenseNumber());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        }
    }

    public Optional<Customer> findById(Connection conn, long customerId) throws SQLException {
        String sql = "SELECT id, name, phone, license_number, is_blocked, created_at FROM customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Customer customer = new Customer();
                customer.setId(rs.getLong("id"));
                customer.setName(rs.getString("name"));
                customer.setPhone(rs.getString("phone"));
                customer.setLicenseNumber(rs.getString("license_number"));
                customer.setBlocked(rs.getBoolean("is_blocked"));
                customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return Optional.of(customer);
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
}
