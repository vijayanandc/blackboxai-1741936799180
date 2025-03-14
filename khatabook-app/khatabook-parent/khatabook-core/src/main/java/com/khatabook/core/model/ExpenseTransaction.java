package com.khatabook.core.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("EXPENSE")
public class ExpenseTransaction extends Transaction {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    // Default constructor
    public ExpenseTransaction() {
        super();
    }

    // Constructor with required fields
    public ExpenseTransaction(BigDecimal amount, ExpenseCategory category) {
        super(amount);
        this.category = category;
    }

    // Getters and Setters
    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
