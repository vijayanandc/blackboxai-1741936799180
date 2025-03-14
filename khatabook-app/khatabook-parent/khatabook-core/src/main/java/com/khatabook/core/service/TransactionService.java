package com.khatabook.core.service;

import com.khatabook.core.model.*;
import com.khatabook.core.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionRepository transactionRepository;
    private final ContactService contactService;
    private final ExpenseCategoryService expenseCategoryService;

    public TransactionService(
        TransactionRepository transactionRepository,
        ContactService contactService,
        ExpenseCategoryService expenseCategoryService
    ) {
        this.transactionRepository = transactionRepository;
        this.contactService = contactService;
        this.expenseCategoryService = expenseCategoryService;
    }

    // Expense Transaction Methods
    public ExpenseTransaction createExpenseTransaction(
        BigDecimal amount,
        Long categoryId,
        Long contactId,
        String notes
    ) {
        validateAmount(amount);
        
        Contact contact = contactService.getContact(contactId)
            .orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            
        ExpenseCategory category = expenseCategoryService.getCategory(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Expense category not found"));

        // Validate category belongs to contact's organization
        expenseCategoryService.validateCategoryBelongsToOrganization(
            categoryId, 
            contact.getOrganization()
        );

        ExpenseTransaction transaction = new ExpenseTransaction(amount, category);
        transaction.setContact(contact);
        transaction.setNotes(notes);
        transaction.setDate(LocalDateTime.now());

        logger.info("Creating new expense transaction of {} for contact: {} in category: {}", 
            amount, contact.getName(), category.getName());
        return (ExpenseTransaction) transactionRepository.save(transaction);
    }

    // Give/Take Transaction Methods
    public GiveTakeTransaction createGiveTakeTransaction(
        BigDecimal amount,
        TransactionType type,
        Long contactId,
        String notes
    ) {
        validateAmount(amount);
        
        Contact contact = contactService.getContact(contactId)
            .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        GiveTakeTransaction transaction = new GiveTakeTransaction(amount, type);
        transaction.setContact(contact);
        transaction.setNotes(notes);
        transaction.setDate(LocalDateTime.now());

        // Update contact balance
        BigDecimal currentBalance = contact.getBalance();
        BigDecimal newBalance;
        
        if (type == TransactionType.GIVE) {
            newBalance = currentBalance.add(amount);
        } else {
            newBalance = currentBalance.subtract(amount);
        }
        
        contactService.updateContactBalance(contactId, newBalance);

        logger.info("Creating new {} transaction of {} for contact: {}", 
            type, amount, contact.getName());
        return (GiveTakeTransaction) transactionRepository.save(transaction);
    }

    // General Transaction Methods
    public Optional<Transaction> getTransaction(Long id) {
        logger.info("Fetching transaction with id: {}", id);
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByContact(Contact contact) {
        logger.info("Fetching all transactions for contact: {}", contact.getName());
        return transactionRepository.findByContact(contact);
    }

    public List<ExpenseTransaction> getExpensesByCategory(ExpenseCategory category) {
        logger.info("Fetching all expenses for category: {}", category.getName());
        return transactionRepository.findExpensesByCategory(category);
    }

    public List<GiveTakeTransaction> getGiveTakeTransactions(Contact contact, TransactionType type) {
        logger.info("Fetching all {} transactions for contact: {}", type, contact.getName());
        return transactionRepository.findGiveTakeByType(contact, type);
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Fetching transactions between {} and {}", startDate, endDate);
        return transactionRepository.findByDateRange(startDate, endDate);
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // If it's a give/take transaction, reverse the balance update
        if (transaction instanceof GiveTakeTransaction) {
            GiveTakeTransaction giveTakeTransaction = (GiveTakeTransaction) transaction;
            Contact contact = transaction.getContact();
            BigDecimal currentBalance = contact.getBalance();
            BigDecimal amount = transaction.getAmount();
            
            BigDecimal newBalance;
            if (giveTakeTransaction.getTransactionType() == TransactionType.GIVE) {
                newBalance = currentBalance.subtract(amount);
            } else {
                newBalance = currentBalance.add(amount);
            }
            
            contactService.updateContactBalance(contact.getId(), newBalance);
        }

        logger.info("Deleting transaction with id: {}", id);
        transactionRepository.deleteById(id);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    // Report Generation Methods
    public BigDecimal getTotalReceivables(Contact contact) {
        List<GiveTakeTransaction> giveTransactions = 
            transactionRepository.findGiveTakeByType(contact, TransactionType.GIVE);
        
        return giveTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPayables(Contact contact) {
        List<GiveTakeTransaction> takeTransactions = 
            transactionRepository.findGiveTakeByType(contact, TransactionType.TAKE);
        
        return takeTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpensesByCategory(ExpenseCategory category) {
        List<ExpenseTransaction> expenses = transactionRepository.findExpensesByCategory(category);
        
        return expenses.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
