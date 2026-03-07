package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.CreateCustomerRequest;
import org.example.dto.CreateCustomerResponse;
import org.example.model.Customer;
import org.example.repository.CustomerRepository;
import org.example.util.JsonUtil;
import org.example.util.PasswordUtil;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/customers")
public class CustomerServlet extends BaseServlet {
    private final CustomerRepository customerRepository = new CustomerRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CreateCustomerRequest body = JsonUtil.readBody(req, CreateCustomerRequest.class);
        String name = valueOrEmpty(body.getName());
        String phone = valueOrEmpty(body.getPhone());
        String licenseNumber = valueOrEmpty(body.getLicenseNumber());
        String email = valueOrEmpty(body.getEmail()).toLowerCase();
        String password = valueOrEmpty(body.getPassword());

        if (name.isBlank() || phone.isBlank() || licenseNumber.isBlank() || email.isBlank() || password.isBlank()) {
            writeError(resp, 400, "name, phone, licenseNumber, email, and password are required");
            return;
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setLicenseNumber(licenseNumber);
        customer.setEmail(email);
        customer.setPasswordHash(PasswordUtil.sha256(password));
        customer.setRole("user");

        try {
            long id = customerRepository.create(customer);
            JsonUtil.write(resp, 201, new CreateCustomerResponse(id));
        } catch (SQLException ex) {
            writeError(resp, 500, "failed to create customer: " + ex.getMessage());
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
