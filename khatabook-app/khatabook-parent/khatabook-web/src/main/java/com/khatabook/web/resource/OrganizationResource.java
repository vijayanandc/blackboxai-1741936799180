package com.khatabook.web.resource;

import com.khatabook.core.model.Organization;
import com.khatabook.core.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganizationResource {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationResource.class);
    
    private final OrganizationService organizationService;

    public OrganizationResource(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @POST
    public Response createOrganization(Organization organization, @Context UriInfo uriInfo) {
        try {
            Organization createdOrg = organizationService.createOrganization(organization);
            
            URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdOrg.getId()))
                .build();
            
            logger.info("Organization created successfully: {}", createdOrg.getOrgName());
            
            return Response.created(location)
                .entity(createdOrg)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create organization: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getOrganization(@PathParam("id") Long id) {
        return organizationService.getOrganization(id)
            .map(org -> {
                logger.info("Retrieved organization: {}", org.getOrgName());
                return Response.ok(org).build();
            })
            .orElseGet(() -> {
                logger.warn("Organization not found with id: {}", id);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Organization not found"))
                    .build();
            });
    }

    @GET
    public Response getAllOrganizations() {
        List<Organization> organizations = organizationService.getAllOrganizations();
        logger.info("Retrieved {} organizations", organizations.size());
        return Response.ok(organizations).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateOrganization(@PathParam("id") Long id, Organization organization) {
        try {
            // Ensure the ID in the path matches the organization
            organization.setId(id);
            
            Organization updatedOrg = organizationService.updateOrganization(organization);
            logger.info("Organization updated successfully: {}", updatedOrg.getOrgName());
            
            return Response.ok(updatedOrg).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update organization: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@PathParam("id") Long id) {
        try {
            organizationService.deleteOrganization(id);
            logger.info("Organization deleted successfully with id: {}", id);
            
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete organization: {}", e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchOrganization(@QueryParam("name") String name) {
        return organizationService.getOrganizationByName(name)
            .map(org -> {
                logger.info("Found organization by name: {}", org.getOrgName());
                return Response.ok(org).build();
            })
            .orElseGet(() -> {
                logger.warn("Organization not found with name: {}", name);
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Organization not found"))
                    .build();
            });
    }

    @GET
    @Path("/{id}/exists")
    public Response checkOrganizationExists(@PathParam("id") Long id) {
        boolean exists = organizationService.getOrganization(id).isPresent();
        logger.info("Checked existence of organization {}: {}", id, exists);
        return Response.ok(Map.of("exists", exists)).build();
    }

    @GET
    @Path("/validate")
    public Response validateOrganizationName(@QueryParam("name") String name) {
        boolean available = organizationService.getOrganizationByName(name).isEmpty();
        logger.info("Validated organization name {}: {}", name, available ? "available" : "taken");
        return Response.ok(Map.of("available", available)).build();
    }
}
