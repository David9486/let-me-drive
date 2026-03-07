package org.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private JsonUtil() {
    }

    public static <T> T readBody(HttpServletRequest req, Class<T> type) throws IOException {
        return MAPPER.readValue(req.getInputStream(), type);
    }

    public static void write(HttpServletResponse resp, int status, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        MAPPER.writeValue(resp.getWriter(), data);
    }
}
