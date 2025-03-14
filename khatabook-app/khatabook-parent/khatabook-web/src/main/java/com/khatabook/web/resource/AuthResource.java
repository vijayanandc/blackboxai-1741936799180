package com.khatabook.web.resource;

import com.google.firebase.auth.FirebaseToken;
import com.khatabook.core.config.FirebaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @POST
    @Path("/verify-token")
    public Response verifyToken(Map<String, String> request) {
        String idToken = request.get("idToken");
        
        if (idToken == null || idToken.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "ID token is required"))
                .build();
        }

        try {
            FirebaseToken decodedToken = FirebaseConfig.verifyToken(idToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", decodedToken.getUid());
            response.put("phoneNumber", decodedToken.getClaims().get("phone_number"));
            
            logger.info("Token verified successfully for user: {}", decodedToken.getUid());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Invalid token"))
                .build();
        }
    }

    @POST
    @Path("/refresh-token")
    public Response refreshToken(Map<String, String> request) {
        String idToken = request.get("idToken");
        
        if (idToken == null || idToken.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "ID token is required"))
                .build();
        }

        try {
            // Verify the current token first
            FirebaseToken decodedToken = FirebaseConfig.verifyToken(idToken);
            
            // In a real implementation, you would use Firebase Admin SDK to create a new custom token
            // For now, we'll just return success with the verified token info
            Map<String, Object> response = new HashMap<>();
            response.put("userId", decodedToken.getUid());
            response.put("phoneNumber", decodedToken.getClaims().get("phone_number"));
            
            logger.info("Token refreshed successfully for user: {}", decodedToken.getUid());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Invalid token"))
                .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(Map<String, String> request) {
        String idToken = request.get("idToken");
        
        if (idToken == null || idToken.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "ID token is required"))
                .build();
        }

        try {
            // Verify the token is valid before processing logout
            FirebaseToken decodedToken = FirebaseConfig.verifyToken(idToken);
            
            // In a real implementation, you might want to:
            // 1. Invalidate the token on Firebase side
            // 2. Clear any server-side session data
            // 3. Perform any other cleanup
            
            logger.info("User logged out successfully: {}", decodedToken.getUid());
            
            return Response.ok(Map.of("message", "Logged out successfully")).build();
            
        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Invalid token"))
                .build();
        }
    }

    @GET
    @Path("/status")
    public Response getAuthStatus(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("authenticated", false))
                .build();
        }

        String token = authHeader.substring("Bearer ".length());

        try {
            FirebaseToken decodedToken = FirebaseConfig.verifyToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("userId", decodedToken.getUid());
            response.put("phoneNumber", decodedToken.getClaims().get("phone_number"));
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Auth status check failed: {}", e.getMessage());
            
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("authenticated", false))
                .build();
        }
    }
}
