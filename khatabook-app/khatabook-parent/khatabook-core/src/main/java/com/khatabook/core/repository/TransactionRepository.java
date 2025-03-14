package com.khatabook.core.repository;

import com.khatabook.core.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);
    private final SessionFactory sessionFactory;

    public TransactionRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public com.khatabook.core.model.Transaction save(com.khatabook.core.model.Transaction transaction) {
        Transaction hibernateTransaction = null;
        try (Session session = sessionFactory.openSession()) {
            hibernateTransaction = session.beginTransaction();
            session.saveOrUpdate(transaction);
            hibernateTransaction.commit();
            logger.info("Transaction saved successfully with id: {}", transaction.getId());
            return transaction;
        } catch (Exception e) {
            if (hibernateTransaction != null) {
                hibernateTransaction.rollback();
            }
            logger.error("Error saving transaction: {}", e.getMessage());
            throw new RuntimeException("Error saving transaction", e);
        }
    }

    public Optional<com.khatabook.core.model.Transaction> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            com.khatabook.core.model.Transaction transaction = session.get(com.khatabook.core.model.Transaction.class, id);
            return Optional.ofNullable(transaction);
        } catch (Exception e) {
            logger.error("Error finding transaction by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Error finding transaction", e);
        }
    }

    public List<com.khatabook.core.model.Transaction> findByContact(Contact contact) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<com.khatabook.core.model.Transaction> query = cb.createQuery(com.khatabook.core.model.Transaction.class);
            Root<com.khatabook.core.model.Transaction> root = query.from(com.khatabook.core.model.Transaction.class);
            
            query.select(root)
                 .where(cb.equal(root.get("contact"), contact))
                 .orderBy(cb.desc(root.get("date")));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding transactions for contact {}: {}", contact.getName(), e.getMessage());
            throw new RuntimeException("Error finding transactions for contact", e);
        }
    }

    public List<ExpenseTransaction> findExpensesByCategory(ExpenseCategory category) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ExpenseTransaction> query = cb.createQuery(ExpenseTransaction.class);
            Root<ExpenseTransaction> root = query.from(ExpenseTransaction.class);
            
            query.select(root)
                 .where(cb.equal(root.get("category"), category))
                 .orderBy(cb.desc(root.get("date")));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding expenses for category {}: {}", category.getName(), e.getMessage());
            throw new RuntimeException("Error finding expenses for category", e);
        }
    }

    public List<GiveTakeTransaction> findGiveTakeByType(Contact contact, TransactionType type) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<GiveTakeTransaction> query = cb.createQuery(GiveTakeTransaction.class);
            Root<GiveTakeTransaction> root = query.from(GiveTakeTransaction.class);
            
            query.select(root)
                 .where(cb.and(
                     cb.equal(root.get("contact"), contact),
                     cb.equal(root.get("transactionType"), type)
                 ))
                 .orderBy(cb.desc(root.get("date")));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding give/take transactions for contact {} and type {}: {}", 
                contact.getName(), type, e.getMessage());
            throw new RuntimeException("Error finding give/take transactions", e);
        }
    }

    public List<com.khatabook.core.model.Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<com.khatabook.core.model.Transaction> query = cb.createQuery(com.khatabook.core.model.Transaction.class);
            Root<com.khatabook.core.model.Transaction> root = query.from(com.khatabook.core.model.Transaction.class);
            
            query.select(root)
                 .where(cb.and(
                     cb.greaterThanOrEqualTo(root.get("date"), startDate),
                     cb.lessThanOrEqualTo(root.get("date"), endDate)
                 ))
                 .orderBy(cb.desc(root.get("date")));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding transactions between dates {} and {}: {}", 
                startDate, endDate, e.getMessage());
            throw new RuntimeException("Error finding transactions by date range", e);
        }
    }

    public void delete(com.khatabook.core.model.Transaction transaction) {
        Transaction hibernateTransaction = null;
        try (Session session = sessionFactory.openSession()) {
            hibernateTransaction = session.beginTransaction();
            session.delete(transaction);
            hibernateTransaction.commit();
            logger.info("Transaction deleted successfully with id: {}", transaction.getId());
        } catch (Exception e) {
            if (hibernateTransaction != null) {
                hibernateTransaction.rollback();
            }
            logger.error("Error deleting transaction: {}", e.getMessage());
            throw new RuntimeException("Error deleting transaction", e);
        }
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
}
