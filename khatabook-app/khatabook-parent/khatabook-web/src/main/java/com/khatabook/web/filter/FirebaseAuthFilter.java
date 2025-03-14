package com.khatabook.web.filter;

import com.google.firebase.auth.FirebaseToken;
import com.khatabook.core.config.FirebaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class FirebaseAuthFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthFilter.class);
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Skip authentication for login endpoint
        if (requestContext.getUriInfo().getPath().equals("auth/login")) {
            return;
        }

        // Get the Authorization header
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            // Validate the token using Firebase
            FirebaseToken decodedToken = FirebaseConfig.verifyToken(token);
            
            // Store user information in the request context
            requestContext.setProperty("userId", decodedToken.getUid());
            requestContext.setProperty("userPhone", decodedToken.getClaims().get("phone_number"));
            
            logger.info("Authenticated user: {}", decodedToken.getUid());
            
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authHeader) {
        return authHeader != null && authHeader.toLowerCase()
            .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME)
                .build());
    }
}
