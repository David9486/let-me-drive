package org.example.repository;

import org.example.model.Car;
import org.example.model.CarRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CarRepository {
    public List<Car> findAvailablePremiumCars(Connection conn, int minSeats, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = """
                SELECT c.id, c.car_name, c.brand, c.seat_count, c.price_per_hour, c.late_fee_per_hour
                FROM cars c
                WHERE c.is_active = TRUE
                  AND c.premium_level = 'PREMIUM'
                  AND c.seat_count >= ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM rentals r
                      WHERE r.car_id = c.id
                        AND r.status = 'BOOKED'
                        AND r.start_time < ?
                        AND r.expected_return_time > ?
                  )
                ORDER BY c.price_per_hour ASC, c.seat_count ASC
                """;

        List<Car> cars = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, minSeats);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Car car = new Car();
                    car.setId(rs.getLong("id"));
                    car.setCarName(rs.getString("car_name"));
                    car.setBrand(rs.getString("brand"));
                    car.setSeatCount(rs.getInt("seat_count"));
                    car.setPricePerHour(rs.getBigDecimal("price_per_hour"));
                    car.setLateFeePerHour(rs.getBigDecimal("late_fee_per_hour"));
                    cars.add(car);
                }
            }
        }
        return cars;
    }

    public CarRate findRateForUpdate(Connection conn, long carId) throws SQLException {
        String sql = "SELECT id, price_per_hour, late_fee_per_hour FROM cars WHERE id = ? AND is_active = TRUE AND premium_level = 'PREMIUM' FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                CarRate carRate = new CarRate();
                carRate.setCarId(rs.getLong("id"));
                carRate.setPricePerHour(rs.getBigDecimal("price_per_hour"));
                carRate.setLateFeePerHour(rs.getBigDecimal("late_fee_per_hour"));
                return carRate;
            }
        }
    }
}
