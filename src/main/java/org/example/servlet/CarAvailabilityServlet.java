package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.repository.CarRepository;
import org.example.util.Db;
import org.example.util.JsonUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@WebServlet("/cars/available")
public class CarAvailabilityServlet extends BaseServlet {
    private final CarRepository carRepository = new CarRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int seats = Integer.parseInt(req.getParameter("seats"));
            LocalDateTime start = LocalDateTime.parse(req.getParameter("start"));
            LocalDateTime end = LocalDateTime.parse(req.getParameter("end"));

            if (!start.isBefore(end)) {
                writeError(resp, 400, "start must be before end");
                return;
            }

            try (Connection conn = Db.getConnection()) {
                JsonUtil.write(resp, 200, carRepository.findAvailablePremiumCars(conn, seats, start, end));
            }
        } catch (NumberFormatException | DateTimeParseException ex) {
            writeError(resp, 400, "invalid seats/start/end format");
        } catch (SQLException ex) {
            writeError(resp, 500, "failed to fetch available cars");
        }
    }
}
