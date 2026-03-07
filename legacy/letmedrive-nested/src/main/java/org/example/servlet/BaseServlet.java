package org.example.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ErrorResponse;
import org.example.util.JsonUtil;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    protected void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonUtil.write(resp, status, new ErrorResponse(message));
    }
}
