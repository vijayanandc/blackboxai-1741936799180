package com.khatabook.web.resource;

import com.khatabook.core.model.Contact;
import com.khatabook.core.model.Organization;
import com.khatabook.core.service.ContactService;
import com.khatabook.core.service.OrganizationService;
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

@Path("/organizations/{orgId}/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {
    private static final Logger logger = LoggerFactory.getLogger(ContactResource.class);
    
    private final ContactService contactService;
    private final OrganizationService organizationService;

    public ContactResource(ContactService contactService, OrganizationService organizationService) {
        this.contactService = contactService;
        this.organizationService = organizationService;
    }

    @POST
    public Response createContact(
        @PathParam("orgId") Long orgId,
        Contact contact,
        @Context UriInfo uriInfo
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            Contact createdContact = contactService.createContact(contact, organization);
            
            URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(createdContact.getId()))
                .build();
            
            logger.info("Contact created successfully: {} for organization: {}", 
                createdContact.getName(), organization.getOrgName());
            
            return Response.created(location)
                .entity(createdContact)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create contact: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getContact(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            return contactService.getContact(id)
                .map(contact -> {
                    // Verify contact belongs to the organization
                    if (!contact.getOrganization().getId().equals(organization.getId())) {
                        return Response.status(Response.Status.FORBIDDEN)
                            .entity(Map.of("error", "Contact does not belong to the organization"))
                            .build();
                    }
                    
                    logger.info("Retrieved contact: {}", contact.getName());
                    return Response.ok(contact).build();
                })
                .orElseGet(() -> {
                    logger.warn("Contact not found with id: {}", id);
                    return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Contact not found"))
                        .build();
                });
                
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve contact: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    public Response getContactsByOrganization(@PathParam("orgId") Long orgId) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            List<Contact> contacts = contactService.getContactsByOrganization(organization);
            logger.info("Retrieved {} contacts for organization: {}", 
                contacts.size(), organization.getOrgName());
            
            return Response.ok(contacts).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to retrieve contacts: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateContact(
        @PathParam("orgId") Long orgId,
        @PathParam("id") Long id,
        Contact contact
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            // Ensure the contact exists and belongs to the organization
            Contact existingContact = contactService.getContact(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

            if (!existingContact.getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Contact does not belong to the organization"))
                    .build();
            }

            // Set the ID and update
            contact.setId(id);
            Contact updatedContact = contactService.updateContact(contact);
            
            logger.info("Contact updated successfully: {}", updatedContact.getName());
            return Response.ok(updatedContact).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update contact: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteContact(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            // Ensure the contact exists and belongs to the organization
            Contact contact = contactService.getContact(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

            if (!contact.getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Contact does not belong to the organization"))
                    .build();
            }

            contactService.deleteContact(id);
            logger.info("Contact deleted successfully with id: {}", id);
            
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete contact: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/search")
    public Response searchContacts(
        @PathParam("orgId") Long orgId,
        @QueryParam("term") String searchTerm
    ) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            List<Contact> contacts = contactService.searchContacts(searchTerm, organization);
            logger.info("Found {} contacts matching search term: {}", contacts.size(), searchTerm);
            
            return Response.ok(contacts).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to search contacts: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/{id}/balance")
    public Response getContactBalance(@PathParam("orgId") Long orgId, @PathParam("id") Long id) {
        try {
            Organization organization = organizationService.getOrganization(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

            Contact contact = contactService.getContact(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

            if (!contact.getOrganization().getId().equals(organization.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Contact does not belong to the organization"))
                    .build();
            }

            BigDecimal balance = contactService.getContactBalance(id);
            logger.info("Retrieved balance for contact {}: {}", contact.getName(), balance);
            
            return Response.ok(Map.of("balance", balance)).build();
            
        } catch (IllegalArgumentException e) {
            logger.error("Failed to get contact balance: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
}
