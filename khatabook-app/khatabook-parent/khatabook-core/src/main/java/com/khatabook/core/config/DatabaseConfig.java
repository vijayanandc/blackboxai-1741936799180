package com.khatabook.core.config;

import com.khatabook.core.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                // Load database properties
                Properties settings = new Properties();
                try (var inputStream = DatabaseConfig.class.getClassLoader()
                        .getResourceAsStream("database.properties")) {
                    if (inputStream == null) {
                        throw new RuntimeException("Unable to find database.properties");
                    }
                    settings.load(inputStream);
                } catch (IOException e) {
                    logger.error("Error loading database properties: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to load database properties", e);
                }

                // Map properties to Hibernate settings
                Properties hibernateProps = new Properties();
                
                // Database connection settings
                hibernateProps.put(Environment.DRIVER, settings.getProperty("db.driver"));
                hibernateProps.put(Environment.URL, settings.getProperty("db.url"));
                hibernateProps.put(Environment.USER, settings.getProperty("db.username"));
                hibernateProps.put(Environment.PASS, settings.getProperty("db.password"));
                
                // Hibernate properties
                hibernateProps.put(Environment.DIALECT, settings.getProperty("hibernate.dialect"));
                hibernateProps.put(Environment.SHOW_SQL, settings.getProperty("hibernate.show_sql"));
                hibernateProps.put(Environment.FORMAT_SQL, settings.getProperty("hibernate.format_sql"));
                hibernateProps.put(Environment.HBM2DDL_AUTO, settings.getProperty("hibernate.hbm2ddl.auto"));
                hibernateProps.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, 
                    settings.getProperty("hibernate.current_session_context_class"));
                
                // C3P0 connection pool settings
                hibernateProps.put(Environment.C3P0_MIN_SIZE, settings.getProperty("hibernate.c3p0.min_size"));
                hibernateProps.put(Environment.C3P0_MAX_SIZE, settings.getProperty("hibernate.c3p0.max_size"));
                hibernateProps.put(Environment.C3P0_ACQUIRE_INCREMENT, 
                    settings.getProperty("hibernate.c3p0.acquire_increment"));
                hibernateProps.put(Environment.C3P0_TIMEOUT, settings.getProperty("hibernate.c3p0.timeout"));
                hibernateProps.put(Environment.C3P0_MAX_STATEMENTS, 
                    settings.getProperty("hibernate.c3p0.max_statements"));
                hibernateProps.put(Environment.C3P0_IDLE_TEST_PERIOD, 
                    settings.getProperty("hibernate.c3p0.idle_test_period"));
                
                // Second-level cache settings
                hibernateProps.put(Environment.USE_SECOND_LEVEL_CACHE, 
                    settings.getProperty("hibernate.cache.use_second_level_cache"));
                hibernateProps.put(Environment.USE_QUERY_CACHE, 
                    settings.getProperty("hibernate.cache.use_query_cache"));
                hibernateProps.put(Environment.CACHE_REGION_FACTORY, 
                    settings.getProperty("hibernate.cache.region.factory_class"));

                configuration.setProperties(settings);

                // Register entity classes
                configuration.addAnnotatedClass(Organization.class);
                configuration.addAnnotatedClass(Contact.class);
                configuration.addAnnotatedClass(ExpenseCategory.class);
                configuration.addAnnotatedClass(Transaction.class);
                configuration.addAnnotatedClass(ExpenseTransaction.class);
                configuration.addAnnotatedClass(GiveTakeTransaction.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);
                
                logger.info("Hibernate SessionFactory created successfully");

            } catch (Exception e) {
                logger.error("Error initializing Hibernate SessionFactory: {}", e.getMessage(), e);
                if (sessionFactory != null) {
                    sessionFactory.close();
                }
                throw new RuntimeException("Failed to initialize Hibernate SessionFactory", e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed successfully");
        }
    }

    // Helper method to recreate the session factory (useful for testing)
    public static void recreateSessionFactory() {
        shutdown();
        sessionFactory = null;
        getSessionFactory();
    }

    // Helper method to clear all data (useful for testing)
    public static void clearDatabase() {
        try (var session = getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            try {
                session.createQuery("delete from GiveTakeTransaction").executeUpdate();
                session.createQuery("delete from ExpenseTransaction").executeUpdate();
                session.createQuery("delete from Transaction").executeUpdate();
                session.createQuery("delete from Contact").executeUpdate();
                session.createQuery("delete from ExpenseCategory").executeUpdate();
                session.createQuery("delete from Organization").executeUpdate();
                
                transaction.commit();
                logger.info("Database cleared successfully");
            } catch (Exception e) {
                transaction.rollback();
                logger.error("Error clearing database: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to clear database", e);
            }
        }
    }

    // Helper method to check database connectivity
    public static boolean checkDatabaseConnection() {
        try (var session = getSessionFactory().openSession()) {
            session.createNativeQuery("SELECT 1").uniqueResult();
            logger.info("Database connection test successful");
            return true;
        } catch (Exception e) {
            logger.error("Database connection test failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
