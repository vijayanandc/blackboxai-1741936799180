package com.khatabook.core.service;

import com.khatabook.core.model.Organization;
import com.khatabook.core.model.ExpenseCategory;
import com.khatabook.core.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OrganizationService {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);
    
    private final OrganizationRepository organizationRepository;

    // Default expense categories that will be created for each new organization
    private static final List<String> DEFAULT_EXPENSE_CATEGORIES = Arrays.asList(
        "Utilities",
        "Rent",
        "Salaries",
        "Office Supplies",
        "Marketing",
        "Travel",
        "Maintenance",
        "Insurance",
        "Miscellaneous"
    );

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization createOrganization(Organization organization) {
        validateOrganization(organization);
        
        // Check if organization with same name exists
        Optional<Organization> existingOrg = organizationRepository.findByName(organization.getOrgName());
        if (existingOrg.isPresent()) {
            logger.error("Organization with name {} already exists", organization.getOrgName());
            throw new IllegalArgumentException("Organization with this name already exists");
        }

        // Add default expense categories
        Set<ExpenseCategory> defaultCategories = createDefaultExpenseCategories(organization);
        organization.setExpenseCategories(defaultCategories);

        logger.info("Creating new organization: {}", organization.getOrgName());
        return organizationRepository.save(organization);
    }

    public Organization updateOrganization(Organization organization) {
        validateOrganization(organization);
        
        // Ensure organization exists
        organizationRepository.findById(organization.getId())
            .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        logger.info("Updating organization: {}", organization.getOrgName());
        return organizationRepository.save(organization);
    }

    public Optional<Organization> getOrganization(Long id) {
        logger.info("Fetching organization with id: {}", id);
        return organizationRepository.findById(id);
    }

    public Optional<Organization> getOrganizationByName(String name) {
        logger.info("Fetching organization with name: {}", name);
        return organizationRepository.findByName(name);
    }

    public List<Organization> getAllOrganizations() {
        logger.info("Fetching all organizations");
        return organizationRepository.findAll();
    }

    public void deleteOrganization(Long id) {
        logger.info("Deleting organization with id: {}", id);
        organizationRepository.deleteById(id);
    }

    private void validateOrganization(Organization organization) {
        if (organization == null) {
            throw new IllegalArgumentException("Organization cannot be null");
        }
        if (organization.getOrgName() == null || organization.getOrgName().trim().isEmpty()) {
            throw new IllegalArgumentException("Organization name is required");
        }
        if (organization.getCurrency() == null || organization.getCurrency().trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (organization.getCountry() == null || organization.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
    }

    private Set<ExpenseCategory> createDefaultExpenseCategories(Organization organization) {
        Set<ExpenseCategory> categories = new HashSet<>();
        for (String categoryName : DEFAULT_EXPENSE_CATEGORIES) {
            ExpenseCategory category = new ExpenseCategory(categoryName, true);
            category.setOrganization(organization);
            categories.add(category);
        }
        return categories;
    }
}
