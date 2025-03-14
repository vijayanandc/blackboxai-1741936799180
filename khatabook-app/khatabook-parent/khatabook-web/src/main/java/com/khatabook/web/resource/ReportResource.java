package com.khatabook.web.resource;

import com.khatabook.core.model.Organization;
import com.khatabook.core.service.OrganizationService;
import com.khatabook.core.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Path("/organizations/{orgId}/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportResource {
    private static final Logger logger = LoggerFactory.getLogger(ReportResource.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    private final ReportService reportService;
    private final OrganizationService organizationService;

    public ReportResource(ReportService reportService, OrganizationService organizationService) {
        this.reportService = reportService;
        this.organizationService = organizationService;
    }

    @GET
    @Path("/contact-balance-summary")
    public Response getContactBalanceSummary(@PathParam("orgId") Long orgId) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            Map<String, Object> summary = Map.of(
                "balanceSummary", reportService.getContactBalanceSummary(organization)
            );
            
            logger.info("Generated contact balance summary for organization: {}", 
                organization.getOrgName());
            
            return Response.ok(summary).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to generate contact balance summary: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/contact-statement/{contactId}")
    public Response getContactStatement(
        @PathParam("orgId") Long orgId,
        @PathParam("contactId") Long contactId,
        @QueryParam("startDate") String startDateStr,
        @QueryParam("endDate") String endDateStr
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DATE_FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DATE_FORMATTER);

            Map<String, Object> statement = reportService.getContactStatement(
                contactId, startDate, endDate
            );
            
            logger.info("Generated statement for contact {} between {} and {}", 
                contactId, startDate, endDate);
            
            return Response.ok(statement).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to generate contact statement: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/overall-statement")
    public Response getOverallStatement(
        @PathParam("orgId") Long orgId,
        @QueryParam("startDate") String startDateStr,
        @QueryParam("endDate") String endDateStr
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DATE_FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DATE_FORMATTER);

            Map<String, Object> statement = reportService.getOverallStatement(
                organization, startDate, endDate
            );
            
            logger.info("Generated overall statement for organization {} between {} and {}", 
                organization.getOrgName(), startDate, endDate);
            
            return Response.ok(statement).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to generate overall statement: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/expense-summary")
    public Response getExpenseSummary(
        @PathParam("orgId") Long orgId,
        @QueryParam("startDate") String startDateStr,
        @QueryParam("endDate") String endDateStr
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DATE_FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DATE_FORMATTER);

            Map<String, Object> summary = reportService.getExpenseSummary(
                organization, startDate, endDate
            );
            
            logger.info("Generated expense summary for organization {} between {} and {}", 
                organization.getOrgName(), startDate, endDate);
            
            return Response.ok(summary).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to generate expense summary: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/period-wise-expense-summary")
    public Response getPeriodWiseExpenseSummary(
        @PathParam("orgId") Long orgId,
        @QueryParam("startDate") String startDateStr,
        @QueryParam("endDate") String endDateStr,
        @QueryParam("groupBy") @DefaultValue("monthly") String groupBy
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DATE_FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DATE_FORMATTER);

            // Validate groupBy parameter
            if (!groupBy.matches("(?i)daily|weekly|monthly")) {
                throw new IllegalArgumentException("Invalid groupBy parameter. Must be 'daily', 'weekly', or 'monthly'");
            }

            Map<String, Object> summary = reportService.getPeriodWiseExpenseSummary(
                organization, startDate, endDate, groupBy.toLowerCase()
            );
            
            logger.info("Generated period-wise expense summary for organization {} between {} and {} grouped by {}", 
                organization.getOrgName(), startDate, endDate, groupBy);
            
            return Response.ok(summary).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to generate period-wise expense summary: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
}
