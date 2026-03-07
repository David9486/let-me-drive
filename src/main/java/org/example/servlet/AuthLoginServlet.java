package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.AuthLoginRequest;
import org.example.dto.AuthResponse;
import org.example.model.Customer;
import org.example.repository.CustomerRepository;
import org.example.util.JsonUtil;
import org.example.util.PasswordUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/auth/login")
public class AuthLoginServlet extends BaseServlet {
    private final CustomerRepository customerRepository = new CustomerRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthLoginRequest body = JsonUtil.readBody(req, AuthLoginRequest.class);
        String email = valueOrEmpty(body.getEmail()).toLowerCase();
        String password = valueOrEmpty(body.getPassword());

        if (email.isBlank() || password.isBlank()) {
            writeError(resp, 400, "email and password are required");
            return;
        }

        try {
            Optional<Customer> customer = customerRepository.authenticate(email, PasswordUtil.sha256(password));
            if (customer.isEmpty()) {
                writeError(resp, 401, "invalid credentials");
                return;
            }

            Customer user = customer.get();
            JsonUtil.write(resp, 200, new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole()));
        } catch (SQLException ex) {
            writeError(resp, 500, "failed to authenticate user: " + ex.getMessage());
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
