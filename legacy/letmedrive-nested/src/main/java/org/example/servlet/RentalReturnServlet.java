package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.RentalResponse;
import org.example.dto.ReturnRentalRequest;
import org.example.model.RentalForReturn;
import org.example.repository.RentalRepository;
import org.example.util.Db;
import org.example.util.JsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/rentals/return")
public class RentalReturnServlet extends BaseServlet {
    private final RentalRepository rentalRepository = new RentalRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ReturnRentalRequest body = JsonUtil.readBody(req, ReturnRentalRequest.class);

        if (body.getActualReturnTime() == null) {
            writeError(resp, 400, "actualReturnTime is required");
            return;
        }

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                RentalForReturn rental = rentalRepository.findBookedRental(conn, body.getRentalId());
                if (rental == null) {
                    conn.rollback();
                    writeError(resp, 404, "active rental not found");
                    return;
                }

                BigDecimal fine = rentalRepository.calculateFineAmount(
                        rental.getExpectedReturnTime(),
                        body.getActualReturnTime(),
                        rental.getLateFeePerHour()
                );
                BigDecimal total = rental.getBaseAmount().add(fine);

                rentalRepository.completeRental(conn, body.getRentalId(), body.getActualReturnTime(), fine, total);
                conn.commit();

                RentalResponse response = new RentalResponse();
                response.setRentalId(body.getRentalId());
                response.setBaseAmount(rental.getBaseAmount());
                response.setFineAmount(fine);
                response.setTotalAmount(total);
                response.setStatus("RETURNED");
                JsonUtil.write(resp, 200, response);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            writeError(resp, 500, "failed to complete return");
        }
    }
}
