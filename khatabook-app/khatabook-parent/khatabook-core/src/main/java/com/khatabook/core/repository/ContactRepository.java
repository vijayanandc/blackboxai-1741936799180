package com.khatabook.core.repository;

import com.khatabook.core.model.Contact;
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

public class ContactRepository {
    private static final Logger logger = LoggerFactory.getLogger(ContactRepository.class);
    private final SessionFactory sessionFactory;

    public ContactRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Contact save(Contact contact) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(contact);
            transaction.commit();
            logger.info("Contact saved successfully: {}", contact.getName());
            return contact;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving contact: {}", e.getMessage());
            throw new RuntimeException("Error saving contact", e);
        }
    }

    public Optional<Contact> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Contact contact = session.get(Contact.class, id);
            return Optional.ofNullable(contact);
        } catch (Exception e) {
            logger.error("Error finding contact by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Error finding contact", e);
        }
    }

    public List<Contact> findByOrganization(Organization organization) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Contact> query = cb.createQuery(Contact.class);
            Root<Contact> root = query.from(Contact.class);
            
            query.select(root)
                 .where(cb.equal(root.get("organization"), organization));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding contacts for organization {}: {}", 
                organization.getOrgName(), e.getMessage());
            throw new RuntimeException("Error finding contacts for organization", e);
        }
    }

    public Optional<Contact> findByMobileNumber(String mobileNumber, Organization organization) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Contact> query = cb.createQuery(Contact.class);
            Root<Contact> root = query.from(Contact.class);
            
            query.select(root)
                 .where(cb.and(
                     cb.equal(root.get("mobileNumber"), mobileNumber),
                     cb.equal(root.get("organization"), organization)
                 ));
            
            List<Contact> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding contact by mobile number {}: {}", 
                mobileNumber, e.getMessage());
            throw new RuntimeException("Error finding contact by mobile number", e);
        }
    }

    public List<Contact> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Contact> query = cb.createQuery(Contact.class);
            Root<Contact> root = query.from(Contact.class);
            query.select(root);
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding all contacts: {}", e.getMessage());
            throw new RuntimeException("Error finding all contacts", e);
        }
    }

    public void delete(Contact contact) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(contact);
            transaction.commit();
            logger.info("Contact deleted successfully: {}", contact.getName());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting contact: {}", e.getMessage());
            throw new RuntimeException("Error deleting contact", e);
        }
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
}
