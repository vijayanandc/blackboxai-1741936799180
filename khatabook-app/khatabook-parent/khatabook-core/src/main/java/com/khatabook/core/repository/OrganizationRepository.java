package com.khatabook.core.repository;

import com.khatabook.core.model.Organization;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class OrganizationRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrganizationRepository.class);
    private final SessionFactory sessionFactory;

    public OrganizationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Organization save(Organization organization) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(organization);
            transaction.commit();
            logger.info("Organization saved successfully: {}", organization.getOrgName());
            return organization;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving organization: {}", e.getMessage());
            throw new RuntimeException("Error saving organization", e);
        }
    }

    public Optional<Organization> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Organization organization = session.get(Organization.class, id);
            return Optional.ofNullable(organization);
        } catch (Exception e) {
            logger.error("Error finding organization by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Error finding organization", e);
        }
    }

    public Optional<Organization> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Organization> query = cb.createQuery(Organization.class);
            Root<Organization> root = query.from(Organization.class);
            
            query.select(root)
                 .where(cb.equal(root.get("orgName"), name));
            
            try {
                Organization organization = session.createQuery(query).getSingleResult();
                return Optional.of(organization);
            } catch (NoResultException e) {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error finding organization by name {}: {}", name, e.getMessage());
            throw new RuntimeException("Error finding organization", e);
        }
    }

    public List<Organization> findAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Organization> query = cb.createQuery(Organization.class);
            Root<Organization> root = query.from(Organization.class);
            query.select(root);
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding all organizations: {}", e.getMessage());
            throw new RuntimeException("Error finding all organizations", e);
        }
    }

    public void delete(Organization organization) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(organization);
            transaction.commit();
            logger.info("Organization deleted successfully: {}", organization.getOrgName());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting organization: {}", e.getMessage());
            throw new RuntimeException("Error deleting organization", e);
        }
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }
}
