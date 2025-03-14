package com.khatabook.web.resource;

import com.khatabook.core.model.*;
import com.khatabook.core.service.ContactService;
import com.khatabook.core.service.ExpenseCategoryService;
import com.khatabook.core.service.OrganizationService;
import com.khatabook.core.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Path("/organizations/{orgId}/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {
    private static final Logger logger = LoggerFactory.getLogger(TransactionResource.class);
    
    private final TransactionService transactionService;
    private final OrganizationService organizationService;
    private final ContactService contactService;
    private final ExpenseCategoryService expenseCategoryService;

    public TransactionResource(
        TransactionService transactionService,
        OrganizationService organizationService,
        ContactService contactService,
        ExpenseCategoryService expenseCategoryService
    ) {
        this.transactionService = transactionService;
        this.organizationService = organizationService;
        this.contactService = contactService;
        this.expenseCategoryService = expenseCategoryService;
    }

    @POST
    @Path("/expenses")
    public Response createExpenseTransaction(
        @PathParam("orgId") Long orgId,
        Map<String, Object> request,
        @Context UriInfo uriInfo
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            // Extract and validate request parameters
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            Long categoryId = Long.valueOf(request.get("categoryId").toString());
            Long contactId = Long.valueOf(request.get("contactId").toString());
            String notes = (String) request.get("notes");

            // Validate that category and contact belong to the organization
            expenseCategoryService.validateCategoryBelongsToOrganization(categoryId, organization);
            Contact contact = contactService.getContact(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            if (!contact.getOrganization().getId().equals(organization.getId())) {
                throw new IllegalArgumentException("Contact does not belong to the organization");
            }

            ExpenseTransaction transaction = transactionService.createExpenseTransaction(
                amount, categoryId, contactId, notes
            );
            
            URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(transaction.getId()))
                .build();
            
            logger.info("Expense transaction created successfully for amount: {} in category: {}", 
                amount, categoryId);
            
            return Response.created(location)
                .entity(transaction)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create expense transaction: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/give-take")
    public Response createGiveTakeTransaction(
        @PathParam("orgId") Long orgId,
        Map<String, Object> request,
        @Context UriInfo uriInfo
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            // Extract and validate request parameters
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            TransactionType type = TransactionType.valueOf(request.get("type").toString());
            Long contactId = Long.valueOf(request.get("contactId").toString());
            String notes = (String) request.get("notes");

            // Validate that contact belongs to the organization
            Contact contact = contactService.getContact(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            if (!contact.getOrganization().getId().equals(organization.getId())) {
                throw new IllegalArgumentException("Contact does not belong to the organization");
            }

            GiveTakeTransaction transaction = transactionService.createGiveTakeTransaction(
                amount, type, contactId, notes
            );
            
            URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(transaction.getId()))
                .build();
            
            logger.info("Give/Take transaction created successfully for amount: {} of type: {}", 
                amount, type);
            
            return Response.created(location)
                .entity(transaction)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create give/take transaction: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getTransaction(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            return transactionService.getTransaction(id)
                .map(transaction -> {
                    // Verify transaction belongs to the organization
                    if (!transaction.getContact().getOrganization().getId().equals(organization.getId())) {
                        return Response.status(Response.Status.FORBIDDEN)
                            .entity(Map.of("error", "Transaction does not belong to the organization"))
                            .build();
                    }
                    
                    logger.info("Retrieved transaction with id: {}", id);
                    return Response.ok(transaction).build();
                })
                .orElseGet(() -> {
                    logger.warn("Transaction not found with id: {}", id);
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Transaction not found"))
                        .build();
                });
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve transaction: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/contact/{contactId}")
    public Response getTransactionsByContact(
        @PathParam("orgId") Long orgId,
        @PathParam("contactId") Long contactId
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            Contact contact = contactService.getContact(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

            if (!contact.getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Contact does not belong to the organization"))
                    .build();
            }

            List<Transaction> transactions = transactionService.getTransactionsByContact(contact);
            logger.info("Retrieved {} transactions for contact: {}", 
                transactions.size(), contact.getName());
            
            return Response.ok(transactions).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve transactions: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTransaction(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            Transaction transaction = transactionService.getTransaction(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

            if (!transaction.getContact().getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Transaction does not belong to the organization"))
                    .build();
            }

            transactionService.deleteTransaction(id);
            logger.info("Transaction deleted successfully with id: {}", id);
            
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete transaction: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
}
