package org.example.repository;

import org.example.model.RentalForReturn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

public class RentalRepository {
    public boolean hasActiveOverlap(Connection conn, long carId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
                SELECT 1
                FROM rentals
                WHERE car_id = ?
                  AND status = 'BOOKED'
                  AND start_time < ?
                  AND expected_return_time > ?
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carId);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long createRental(Connection conn, long customerId, long carId, LocalDateTime start, LocalDateTime end, BigDecimal baseAmount) throws SQLException {
        String sql = """
                INSERT INTO rentals (
                    customer_id, car_id, start_time, expected_return_time,
                    base_amount, fine_amount, total_amount, status
                ) VALUES (?, ?, ?, ?, ?, 0, ?, 'BOOKED')
                RETURNING id
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, carId);
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));
            ps.setBigDecimal(5, baseAmount);
            ps.setBigDecimal(6, baseAmount);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("id");
            }
        }
    }

    public RentalForReturn findBookedRental(Connection conn, long rentalId) throws SQLException {
        String sql = """
                SELECT r.id, r.expected_return_time, r.base_amount, c.late_fee_per_hour
                FROM rentals r
                JOIN cars c ON c.id = r.car_id
                WHERE r.id = ? AND r.status = 'BOOKED'
                FOR UPDATE
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                RentalForReturn rental = new RentalForReturn();
                rental.setRentalId(rs.getLong("id"));
                rental.setExpectedReturnTime(rs.getTimestamp("expected_return_time").toLocalDateTime());
                rental.setBaseAmount(rs.getBigDecimal("base_amount"));
                rental.setLateFeePerHour(rs.getBigDecimal("late_fee_per_hour"));
                return rental;
            }
        }
    }

    public void completeRental(Connection conn, long rentalId, LocalDateTime actualReturn, BigDecimal fineAmount, BigDecimal totalAmount) throws SQLException {
        String sql = """
                UPDATE rentals
                SET actual_return_time = ?, fine_amount = ?, total_amount = ?, status = 'RETURNED'
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(actualReturn));
            ps.setBigDecimal(2, fineAmount);
            ps.setBigDecimal(3, totalAmount);
            ps.setLong(4, rentalId);
            ps.executeUpdate();
        }
    }

    public BigDecimal calculateBaseAmount(LocalDateTime start, LocalDateTime end, BigDecimal pricePerHour) {
        long mins = Duration.between(start, end).toMinutes();
        long billableHours = Math.max(1L, (long) Math.ceil(mins / 60.0));
        return pricePerHour.multiply(BigDecimal.valueOf(billableHours)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFineAmount(LocalDateTime expectedReturn, LocalDateTime actualReturn, BigDecimal lateFeePerHour) {
        if (!actualReturn.isAfter(expectedReturn)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long minsLate = Duration.between(expectedReturn, actualReturn).toMinutes();
        long lateHours = Math.max(1L, (long) Math.ceil(minsLate / 60.0));
        return lateFeePerHour.multiply(BigDecimal.valueOf(lateHours)).setScale(2, RoundingMode.HALF_UP);
    }
}
