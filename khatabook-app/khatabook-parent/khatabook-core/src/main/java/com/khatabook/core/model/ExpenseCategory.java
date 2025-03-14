package com.khatabook.core.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expense_categories")
public class ExpenseCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_default")
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ExpenseTransaction> expenses = new HashSet<>();

    // Default constructor
    public ExpenseCategory() {}

    // Constructor for creating default categories
    public ExpenseCategory(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Set<ExpenseTransaction> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<ExpenseTransaction> expenses) {
        this.expenses = expenses;
    }

    // Helper method to add expense
    public void addExpense(ExpenseTransaction expense) {
        expenses.add(expense);
        expense.setCategory(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseCategory)) return false;
        ExpenseCategory that = (ExpenseCategory) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
