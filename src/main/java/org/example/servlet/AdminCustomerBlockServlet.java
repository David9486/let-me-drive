package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.BlockCustomerRequest;
import org.example.dto.BlockCustomerResponse;
import org.example.repository.CustomerRepository;
import org.example.util.JsonUtil;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/customers/block")
public class AdminCustomerBlockServlet extends BaseServlet {
    private final CustomerRepository customerRepository = new CustomerRepository();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlockCustomerRequest body = JsonUtil.readBody(req, BlockCustomerRequest.class);

        if (body.getCustomerId() <= 0) {
            writeError(resp, 400, "invalid customerId");
            return;
        }

        try {
            boolean updated = customerRepository.setBlocked(body.getCustomerId(), body.isBlocked());
            if (!updated) {
                writeError(resp, 404, "customer not found");
                return;
            }

            JsonUtil.write(resp, 200, new BlockCustomerResponse(body.getCustomerId(), body.isBlocked()));
        } catch (SQLException ex) {
            writeError(resp, 500, "failed to update customer block status");
        }
    }
}
