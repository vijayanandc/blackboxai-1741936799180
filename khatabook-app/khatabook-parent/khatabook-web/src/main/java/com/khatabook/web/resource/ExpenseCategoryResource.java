package com.khatabook.web.resource;

import com.khatabook.core.model.ExpenseCategory;
import com.khatabook.core.model.Organization;
import com.khatabook.core.service.ExpenseCategoryService;
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

@Path("/organizations/{orgId}/expense-categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExpenseCategoryResource {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseCategoryResource.class);
    
    private final ExpenseCategoryService expenseCategoryService;
    private final OrganizationService organizationService;

    public ExpenseCategoryResource(
        ExpenseCategoryService expenseCategoryService,
        OrganizationService organizationService
    ) {
        this.expenseCategoryService = expenseCategoryService;
        this.organizationService = organizationService;
    }

    @POST
    public Response createCategory(
        @PathParam("orgId") Long orgId,
        ExpenseCategory category,
        @Context UriInfo uriInfo
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            ExpenseCategory createdCategory = expenseCategoryService.createCategory(category, organization);
            
            URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdCategory.getId()))
                .build();
            
            logger.info("Expense category created successfully: {} for organization: {}", 
                createdCategory.getName(), organization.getOrgName());
            
            return Response.created(location)
                .entity(createdCategory)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create expense category: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getCategory(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            return expenseCategoryService.getCategory(id)
                .map(category -> {
                    // Verify category belongs to the organization
                    if (!category.getOrganization().getId().equals(organization.getId())) {
                        return Response.status(Response.Status.FORBIDDEN)
                            .entity(Map.of("error", "Category does not belong to the organization"))
                            .build();
                    }
                    
                    logger.info("Retrieved expense category: {}", category.getName());
                    return Response.ok(category).build();
                })
                .orElseGet(() -> {
                    logger.warn("Expense category not found with id: {}", id);
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Expense category not found"))
                        .build();
                });
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve expense category: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    public Response getCategoriesByOrganization(@PathParam("orgId") Long orgId) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            List<ExpenseCategory> categories = expenseCategoryService.getCategoriesByOrganization(organization);
            logger.info("Retrieved {} expense categories for organization: {}", 
                categories.size(), organization.getOrgName());
            
            return Response.ok(categories).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve expense categories: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/default")
    public Response getDefaultCategories(@PathParam("orgId") Long orgId) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            List<ExpenseCategory> defaultCategories = expenseCategoryService.getDefaultCategories(organization);
            logger.info("Retrieved {} default expense categories for organization: {}", 
                defaultCategories.size(), organization.getOrgName());
            
            return Response.ok(defaultCategories).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve default expense categories: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateCategory(
        @PathParam("orgId") Long orgId,
        @PathParam("id") Long id,
        ExpenseCategory category
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            // Ensure the category exists and belongs to the organization
            ExpenseCategory existingCategory = expenseCategoryService.getCategory(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

            if (!existingCategory.getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Category does not belong to the organization"))
                    .build();
            }

            // Set the ID and update
            category.setId(id);
            ExpenseCategory updatedCategory = expenseCategoryService.updateCategory(category);
            
            logger.info("Expense category updated successfully: {}", updatedCategory.getName());
            return Response.ok(updatedCategory).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update expense category: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCategory(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            // Ensure the category exists and belongs to the organization
            ExpenseCategory category = expenseCategoryService.getCategory(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

            if (!category.getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Category does not belong to the organization"))
                    .build();
            }

            // Check if category is in use
            if (expenseCategoryService.isCategoryInUse(id)) {
                return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Cannot delete category as it is being used by expenses"))
                    .build();
            }

            expenseCategoryService.deleteCategory(id);
            logger.info("Expense category deleted successfully with id: {}", id);
            
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete expense category: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            logger.error("Cannot delete default expense category: {}", e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
}
