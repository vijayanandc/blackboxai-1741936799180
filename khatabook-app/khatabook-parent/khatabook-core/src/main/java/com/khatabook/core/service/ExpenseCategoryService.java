package com.khatabook.core.service;

import com.khatabook.core.model.ExpenseCategory;
import com.khatabook.core.model.Organization;
import com.khatabook.core.repository.ExpenseCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ExpenseCategoryService {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseCategoryService.class);
    
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public ExpenseCategoryService(ExpenseCategoryRepository expenseCategoryRepository) {
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    public ExpenseCategory createCategory(ExpenseCategory category, Organization organization) {
        validateCategory(category);
        
        // Check if category with same name exists in the organization
        Optional<ExpenseCategory> existingCategory = expenseCategoryRepository
            .findByName(category.getName(), organization);
        
        if (existingCategory.isPresent()) {
            logger.error("Expense category with name {} already exists in organization {}", 
                category.getName(), organization.getOrgName());
            throw new IllegalArgumentException("Expense category with this name already exists in the organization");
        }

        // Set organization and default status
        category.setOrganization(organization);
        if (category.isDefault() == null) {
            category.setDefault(false); // Custom categories are not default by default
        }

        logger.info("Creating new expense category: {} for organization: {}", 
            category.getName(), organization.getOrgName());
        return expenseCategoryRepository.save(category);
    }

    public ExpenseCategory updateCategory(ExpenseCategory category) {
        validateCategory(category);
        
        // Ensure category exists
        ExpenseCategory existingCategory = expenseCategoryRepository.findById(category.getId())
            .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

        // Don't allow changing organization
        category.setOrganization(existingCategory.getOrganization());

        // Don't allow changing default status of default categories
        if (existingCategory.isDefault()) {
            category.setDefault(true);
        }

        logger.info("Updating expense category: {}", category.getName());
        return expenseCategoryRepository.save(category);
    }

    public Optional<ExpenseCategory> getCategory(Long id) {
        logger.info("Fetching expense category with id: {}", id);
        return expenseCategoryRepository.findById(id);
    }

    public List<ExpenseCategory> getCategoriesByOrganization(Organization organization) {
        logger.info("Fetching all expense categories for organization: {}", organization.getOrgName());
        return expenseCategoryRepository.findByOrganization(organization);
    }

    public List<ExpenseCategory> getDefaultCategories(Organization organization) {
        logger.info("Fetching default expense categories for organization: {}", organization.getOrgName());
        return expenseCategoryRepository.findDefaultCategories(organization);
    }

    public void deleteCategory(Long id) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

        // Don't allow deleting default categories
        if (category.isDefault()) {
            logger.error("Attempted to delete default category: {}", category.getName());
            throw new IllegalStateException("Cannot delete default expense categories");
        }

        logger.info("Deleting expense category: {}", category.getName());
        expenseCategoryRepository.deleteById(id);
    }

    private void validateCategory(ExpenseCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Expense category cannot be null");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense category name is required");
        }
        
        // Additional validation rules can be added here
        // For example, minimum/maximum length for category name
        if (category.getName().length() < 2 || category.getName().length() > 50) {
            throw new IllegalArgumentException("Category name must be between 2 and 50 characters");
        }
    }

    public Optional<ExpenseCategory> getCategoryByName(String name, Organization organization) {
        logger.info("Fetching expense category with name: {} in organization: {}", 
            name, organization.getOrgName());
        return expenseCategoryRepository.findByName(name, organization);
    }

    public boolean isCategoryInUse(Long categoryId) {
        // This method could check if there are any expenses using this category
        // For now, just return false
        // In a real implementation, you would query the expenses table
        return false;
    }

    public void validateCategoryBelongsToOrganization(Long categoryId, Organization organization) {
        ExpenseCategory category = expenseCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

        if (!category.getOrganization().getId().equals(organization.getId())) {
            throw new IllegalArgumentException("Expense category does not belong to the organization");
        }
    }
}
