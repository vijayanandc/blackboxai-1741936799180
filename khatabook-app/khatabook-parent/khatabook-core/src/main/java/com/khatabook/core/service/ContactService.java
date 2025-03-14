package com.khatabook.core.service;

import com.khatabook.core.model.Contact;
import com.khatabook.core.model.Organization;
import com.khatabook.core.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ContactService {
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact createContact(Contact contact, Organization organization) {
        validateContact(contact);
        
        // Check if contact with same mobile number exists in the organization
        Optional<Contact> existingContact = contactRepository.findByMobileNumber(
            contact.getMobileNumber(), 
            organization
        );
        
        if (existingContact.isPresent()) {
            logger.error("Contact with mobile number {} already exists in organization {}", 
                contact.getMobileNumber(), organization.getOrgName());
            throw new IllegalArgumentException("Contact with this mobile number already exists in the organization");
        }

        // Set initial balance to zero if not set
        if (contact.getBalance() == null) {
            contact.setBalance(BigDecimal.ZERO);
        }

        // Set organization
        contact.setOrganization(organization);

        logger.info("Creating new contact: {} for organization: {}", 
            contact.getName(), organization.getOrgName());
        return contactRepository.save(contact);
    }

    public Contact updateContact(Contact contact) {
        validateContact(contact);
        
        // Ensure contact exists
        Contact existingContact = contactRepository.findById(contact.getId())
            .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        // Preserve the original organization
        contact.setOrganization(existingContact.getOrganization());

        // Preserve the original balance unless explicitly changed
        if (contact.getBalance() == null) {
            contact.setBalance(existingContact.getBalance());
        }

        logger.info("Updating contact: {}", contact.getName());
        return contactRepository.save(contact);
    }

    public Optional<Contact> getContact(Long id) {
        logger.info("Fetching contact with id: {}", id);
        return contactRepository.findById(id);
    }

    public Optional<Contact> getContactByMobileNumber(String mobileNumber, Organization organization) {
        logger.info("Fetching contact with mobile number: {} in organization: {}", 
            mobileNumber, organization.getOrgName());
        return contactRepository.findByMobileNumber(mobileNumber, organization);
    }

    public List<Contact> getContactsByOrganization(Organization organization) {
        logger.info("Fetching all contacts for organization: {}", organization.getOrgName());
        return contactRepository.findByOrganization(organization);
    }

    public void deleteContact(Long id) {
        logger.info("Deleting contact with id: {}", id);
        contactRepository.deleteById(id);
    }

    private void validateContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        if (contact.getName() == null || contact.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name is required");
        }
        if (contact.getMobileNumber() == null || contact.getMobileNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }
        
        // Validate mobile number format (assuming a simple format check)
        String mobileNumberPattern = "^[0-9]{10}$"; // Assumes 10-digit mobile number
        if (!contact.getMobileNumber().matches(mobileNumberPattern)) {
            throw new IllegalArgumentException("Invalid mobile number format. Must be 10 digits");
        }

        // Validate balance if provided
        if (contact.getBalance() != null && contact.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }

    public List<Contact> searchContacts(String searchTerm, Organization organization) {
        // This method could be implemented in the repository layer to search
        // contacts by name or mobile number
        logger.info("Searching contacts with term: {} in organization: {}", 
            searchTerm, organization.getOrgName());
        // For now, just return all contacts
        return getContactsByOrganization(organization);
    }

    public BigDecimal getContactBalance(Long contactId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        return contact.getBalance();
    }

    public void updateContactBalance(Long contactId, BigDecimal newBalance) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
        
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        contact.setBalance(newBalance);
        contactRepository.save(contact);
        logger.info("Updated balance for contact: {} to: {}", contact.getName(), newBalance);
    }
}
