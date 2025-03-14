package com.khatabook.core.repository;

import com.khatabook.core.model.ExpenseCategory;
import com.khatabook.core.model.Organization;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class ExpenseCategoryRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseCategoryRepository.class);
    private final SessionFactory sessionFactory;

    public ExpenseCategoryRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public ExpenseCategory save(ExpenseCategory category) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(category);
            transaction.commit();
            logger.info("Expense category saved successfully: {}", category.getName());
            return category;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving expense category: {}", e.getMessage());
            throw new RuntimeException("Error saving expense category", e);
        }
    }

    public Optional<ExpenseCategory> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            ExpenseCategory category = session.get(ExpenseCategory.class, id);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            logger.error("Error finding expense category by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Error finding expense category", e);
        }
    }

    public List<ExpenseCategory> findByOrganization(Organization organization) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExpenseCategory> query = cb.createQuery(ExpenseCategory.class);
            Root<ExpenseCategory> root = query.from(ExpenseCategory.class);
            
            query.select(root)
                 .where(cb.equal(root.get("organization"), organization));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding expense categories for organization {}: {}", 
                organization.getOrgName(), e.getMessage());
            throw new RuntimeException("Error finding expense categories for organization", e);
        }
    }

    public List<ExpenseCategory> findDefaultCategories(Organization organization) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExpenseCategory> query = cb.createQuery(ExpenseCategory.class);
            Root<ExpenseCategory> root = query.from(ExpenseCategory.class);
            
            query.select(root)
                 .where(cb.and(
                     cb.equal(root.get("organization"), organization),
                     cb.equal(root.get("isDefault"), true)
                 ));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding default expense categories: {}", e.getMessage());
            throw new RuntimeException("Error finding default expense categories", e);
        }
    }

    public Optional<ExpenseCategory> findByName(String name, Organization organization) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExpenseCategory> query = cb.createQuery(ExpenseCategory.class);
            Root<ExpenseCategory> root = query.from(ExpenseCategory.class);
            
            query.select(root)
                 .where(cb.and(
                     cb.equal(root.get("name"), name),
                     cb.equal(root.get("organization"), organization)
                 ));
            
            List<ExpenseCategory> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding expense category by name {}: {}", name, e.getMessage());
            throw new RuntimeException("Error finding expense category by name", e);
        }
    }

    public void delete(ExpenseCategory category) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(category);
            transaction.commit();
            logger.info("Expense category deleted successfully: {}", category.getName());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting expense category: {}", e.getMessage());
            throw new RuntimeException("Error deleting expense category", e);
        }
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
}
