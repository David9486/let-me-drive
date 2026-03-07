package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.CreateRentalRequest;
import org.example.dto.RentalResponse;
import org.example.model.CarRate;
import org.example.model.Customer;
import org.example.repository.CarRepository;
import org.example.repository.CustomerRepository;
import org.example.repository.RentalRepository;
import org.example.util.Db;
import org.example.util.JsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/rentals")
public class RentalServlet extends BaseServlet {
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final CarRepository carRepository = new CarRepository();
    private final RentalRepository rentalRepository = new RentalRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CreateRentalRequest body = JsonUtil.readBody(req, CreateRentalRequest.class);

        if (body.getStartTime() == null || body.getExpectedReturnTime() == null || !body.getStartTime().isBefore(body.getExpectedReturnTime())) {
            writeError(resp, 400, "startTime must be before expectedReturnTime");
            return;
        }

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Customer> customerOpt = customerRepository.findById(conn, body.getCustomerId());
                if (customerOpt.isEmpty()) {
                    conn.rollback();
                    writeError(resp, 404, "customer not found");
                    return;
                }
                if (customerOpt.get().isBlocked()) {
                    conn.rollback();
                    writeError(resp, 403, "customer is blocked by admin");
                    return;
                }

                CarRate carRate = carRepository.findRateForUpdate(conn, body.getCarId());
                if (carRate == null) {
                    conn.rollback();
                    writeError(resp, 404, "premium car not found or inactive");
                    return;
                }

                if (rentalRepository.hasActiveOverlap(conn, body.getCarId(), body.getStartTime(), body.getExpectedReturnTime())) {
                    conn.rollback();
                    writeError(resp, 409, "car is not available for requested time");
                    return;
                }

                BigDecimal baseAmount = rentalRepository.calculateBaseAmount(body.getStartTime(), body.getExpectedReturnTime(), carRate.getPricePerHour());
                long rentalId = rentalRepository.createRental(conn, body.getCustomerId(), body.getCarId(), body.getStartTime(), body.getExpectedReturnTime(), baseAmount);
                conn.commit();

                RentalResponse response = new RentalResponse();
                response.setRentalId(rentalId);
                response.setBaseAmount(baseAmount);
                response.setFineAmount(BigDecimal.ZERO);
                response.setTotalAmount(baseAmount);
                response.setStatus("BOOKED");
                JsonUtil.write(resp, 201, response);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            writeError(resp, 500, "failed to create rental");
        }
    }
}
