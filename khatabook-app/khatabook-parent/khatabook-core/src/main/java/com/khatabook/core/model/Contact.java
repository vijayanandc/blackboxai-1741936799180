package com.khatabook.core.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contacts")
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

    // Default constructor
    public Contact() {}

    // Constructor with required fields
    public Contact(String name, String mobileNumber) {
        this.name = name;
        this.mobileNumber = mobileNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Helper methods
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setContact(this);
        
        // Update balance based on transaction type
        if (transaction instanceof GiveTakeTransaction) {
            GiveTakeTransaction giveTake = (GiveTakeTransaction) transaction;
            if (giveTake.getTransactionType() == TransactionType.GIVE) {
                this.balance = this.balance.add(transaction.getAmount());
            } else {
                this.balance = this.balance.subtract(transaction.getAmount());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        return id != null && id.equals(contact.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
