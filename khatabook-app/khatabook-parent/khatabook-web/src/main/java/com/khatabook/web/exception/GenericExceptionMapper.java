package com.khatabook.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        logger.error("An error occurred: ", exception);

        Map<String, Object> response = new HashMap<>();
        Response.Status status;

        if (exception instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST;
            response.put("error", "Invalid request");
            response.put("message", exception.getMessage());
        } 
        else if (exception instanceof IllegalStateException) {
            status = Response.Status.CONFLICT;
            response.put("error", "Operation conflict");
            response.put("message", exception.getMessage());
        }
        else if (exception instanceof SecurityException) {
            status = Response.Status.FORBIDDEN;
            response.put("error", "Access denied");
            response.put("message", "You don't have permission to perform this operation");
        }
        else if (exception instanceof javax.persistence.EntityNotFoundException) {
            status = Response.Status.NOT_FOUND;
            response.put("error", "Resource not found");
            response.put("message", exception.getMessage());
        }
        else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            response.put("error", "Internal server error");
            response.put("message", "An unexpected error occurred");
            
            // Only include detailed error message in development
            if (isDevelopmentMode()) {
                response.put("details", exception.getMessage());
                response.put("stackTrace", getStackTrace(exception));
            }
        }

        return Response.status(status)
                .entity(response)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private boolean isDevelopmentMode() {
        // This could be configured via environment variable or properties file
        return true; // For development purposes
    }

    private String[] getStackTrace(Throwable exception) {
        return java.util.Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toArray(String[]::new);
    }
}
