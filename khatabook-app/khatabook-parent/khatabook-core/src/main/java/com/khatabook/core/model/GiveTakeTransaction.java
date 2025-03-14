package com.khatabook.core.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("GIVE_TAKE")
public class GiveTakeTransaction extends Transaction {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "give_take_type", nullable = false)
    private TransactionType transactionType;

    // Default constructor
    public GiveTakeTransaction() {
        super();
    }

    // Constructor with required fields
    public GiveTakeTransaction(BigDecimal amount, TransactionType transactionType) {
        super(amount);
        this.transactionType = transactionType;
    }

    // Getters and Setters
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    // Helper method to determine if this is a receivable amount
    public boolean isReceivable() {
        return TransactionType.GIVE.equals(this.transactionType);
    }

    // Helper method to determine if this is a payable amount
    public boolean isPayable() {
        return TransactionType.TAKE.equals(this.transactionType);
    }
}
