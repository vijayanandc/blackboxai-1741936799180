package com.khatabook.web.config;

import com.khatabook.web.filter.FirebaseAuthFilter;
import com.khatabook.web.resource.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    
    public JerseyConfig() {
        // Register Jackson for JSON processing
        register(JacksonFeature.class);
        
        // Register authentication filter
        register(FirebaseAuthFilter.class);
        
        // Register resources
        register(AuthResource.class);
        register(OrganizationResource.class);
        register(ContactResource.class);
        register(ExpenseCategoryResource.class);
        register(TransactionResource.class);
        register(ReportResource.class);
        
        // Register exception mappers
        register(GenericExceptionMapper.class);
        
        // Configure packages to scan for resources
        packages("com.khatabook.web.resource");
    }
}
