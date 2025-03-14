package com.khatabook.core.service;

import com.khatabook.core.model.*;
import com.khatabook.core.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    private final TransactionRepository transactionRepository;
    private final ContactService contactService;
    private final ExpenseCategoryService expenseCategoryService;

    public ReportService(
        TransactionRepository transactionRepository,
        ContactService contactService,
        ExpenseCategoryService expenseCategoryService
    ) {
        this.transactionRepository = transactionRepository;
        this.contactService = contactService;
        this.expenseCategoryService = expenseCategoryService;
    }

    // Contact Balance Summary
    public Map<String, BigDecimal> getContactBalanceSummary(Organization organization) {
        logger.info("Generating contact balance summary for organization: {}", organization.getOrgName());
        
        List<Contact> contacts = contactService.getContactsByOrganization(organization);
        Map<String, BigDecimal> summary = new HashMap<>();
        
        for (Contact contact : contacts) {
            summary.put(contact.getName(), contact.getBalance());
        }
        
        return summary;
    }

    // Contact Statement
    public Map<String, Object> getContactStatement(Long contactId, LocalDateTime startDate, LocalDateTime endDate) {
        Contact contact = contactService.getContact(contactId)
            .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            
        logger.info("Generating statement for contact: {} between {} and {}", 
            contact.getName(), startDate, endDate);

        List<Transaction> transactions = transactionRepository.findByDateRange(startDate, endDate)
            .stream()
            .filter(t -> t.getContact().getId().equals(contactId))
            .collect(Collectors.toList());

        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal totalPayable = BigDecimal.ZERO;

        List<Map<String, Object>> transactionDetails = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("date", transaction.getDate());
            detail.put("amount", transaction.getAmount());
            detail.put("notes", transaction.getNotes());

            if (transaction instanceof GiveTakeTransaction) {
                GiveTakeTransaction giveTake = (GiveTakeTransaction) transaction;
                detail.put("type", giveTake.getTransactionType());
                
                if (giveTake.getTransactionType() == TransactionType.GIVE) {
                    totalReceivable = totalReceivable.add(transaction.getAmount());
                } else {
                    totalPayable = totalPayable.add(transaction.getAmount());
                }
            }
            
            transactionDetails.add(detail);
        }

        Map<String, Object> statement = new HashMap<>();
        statement.put("contactName", contact.getName());
        statement.put("startDate", startDate);
        statement.put("endDate", endDate);
        statement.put("totalReceivable", totalReceivable);
        statement.put("totalPayable", totalPayable);
        statement.put("netBalance", contact.getBalance());
        statement.put("transactions", transactionDetails);

        return statement;
    }

    // Overall Statement
    public Map<String, Object> getOverallStatement(Organization organization, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating overall statement for organization: {} between {} and {}", 
            organization.getOrgName(), startDate, endDate);

        List<Contact> contacts = contactService.getContactsByOrganization(organization);
        
        BigDecimal totalReceivables = BigDecimal.ZERO;
        BigDecimal totalPayables = BigDecimal.ZERO;
        List<Map<String, Object>> contactSummaries = new ArrayList<>();

        for (Contact contact : contacts) {
            Map<String, Object> contactStatement = getContactStatement(contact.getId(), startDate, endDate);
            
            totalReceivables = totalReceivables.add((BigDecimal) contactStatement.get("totalReceivable"));
            totalPayables = totalPayables.add((BigDecimal) contactStatement.get("totalPayable"));
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("contactName", contact.getName());
            summary.put("balance", contact.getBalance());
            summary.put("statement", contactStatement);
            
            contactSummaries.add(summary);
        }

        Map<String, Object> overallStatement = new HashMap<>();
        overallStatement.put("organizationName", organization.getOrgName());
        overallStatement.put("startDate", startDate);
        overallStatement.put("endDate", endDate);
        overallStatement.put("totalReceivables", totalReceivables);
        overallStatement.put("totalPayables", totalPayables);
        overallStatement.put("netPosition", totalReceivables.subtract(totalPayables));
        overallStatement.put("contactSummaries", contactSummaries);

        return overallStatement;
    }

    // Expense Summary
    public Map<String, Object> getExpenseSummary(
        Organization organization, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    ) {
        logger.info("Generating expense summary for organization: {} between {} and {}", 
            organization.getOrgName(), startDate, endDate);

        List<ExpenseCategory> categories = expenseCategoryService.getCategoriesByOrganization(organization);
        List<Transaction> transactions = transactionRepository.findByDateRange(startDate, endDate);
        
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        Map<String, List<Map<String, Object>>> categoryDetails = new HashMap<>();

        for (ExpenseCategory category : categories) {
            categoryTotals.put(category.getName(), BigDecimal.ZERO);
            categoryDetails.put(category.getName(), new ArrayList<>());
        }

        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction instanceof ExpenseTransaction) {
                ExpenseTransaction expense = (ExpenseTransaction) transaction;
                String categoryName = expense.getCategory().getName();
                BigDecimal amount = expense.getAmount();

                // Update category total
                categoryTotals.put(
                    categoryName, 
                    categoryTotals.get(categoryName).add(amount)
                );

                // Add transaction detail
                Map<String, Object> detail = new HashMap<>();
                detail.put("date", expense.getDate());
                detail.put("amount", amount);
                detail.put("contact", expense.getContact().getName());
                detail.put("notes", expense.getNotes());
                
                categoryDetails.get(categoryName).add(detail);

                totalExpenses = totalExpenses.add(amount);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("organizationName", organization.getOrgName());
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("totalExpenses", totalExpenses);
        summary.put("categoryTotals", categoryTotals);
        summary.put("categoryDetails", categoryDetails);

        return summary;
    }

    // Period-wise Expense Summary
    public Map<String, Object> getPeriodWiseExpenseSummary(
        Organization organization,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String groupBy // "daily", "weekly", "monthly"
    ) {
        logger.info("Generating period-wise expense summary for organization: {} between {} and {} grouped by {}", 
            organization.getOrgName(), startDate, endDate, groupBy);

        List<Transaction> transactions = transactionRepository.findByDateRange(startDate, endDate);
        Map<String, Map<String, BigDecimal>> periodSummary = new TreeMap<>(); // Period -> (Category -> Amount)

        for (Transaction transaction : transactions) {
            if (transaction instanceof ExpenseTransaction) {
                ExpenseTransaction expense = (ExpenseTransaction) transaction;
                String period = getPeriodKey(expense.getDate(), groupBy);
                String category = expense.getCategory().getName();

                periodSummary.computeIfAbsent(period, k -> new HashMap<>());
                Map<String, BigDecimal> categorySummary = periodSummary.get(period);
                
                categorySummary.merge(category, expense.getAmount(), BigDecimal::add);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("organizationName", organization.getOrgName());
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("groupBy", groupBy);
        summary.put("periodSummary", periodSummary);

        return summary;
    }

    private String getPeriodKey(LocalDateTime date, String groupBy) {
        switch (groupBy.toLowerCase()) {
            case "daily":
                return date.toLocalDate().toString();
            case "weekly":
                return date.toLocalDate().toString() + " (Week " + date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) + ")";
            case "monthly":
                return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            default:
                throw new IllegalArgumentException("Invalid groupBy parameter. Must be 'daily', 'weekly', or 'monthly'");
        }
    }
}
