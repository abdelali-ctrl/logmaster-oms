package com.logmaster.dao;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton EntityManager Factory for RESOURCE_LOCAL transactions
 */
@ApplicationScoped
public class EntityManagerProducer {

    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("LogMasterPU");
        } catch (Exception e) {
            System.err.println("Failed to create EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static EntityManager createEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("LogMasterPU");
        }
        return emf.createEntityManager();
    }

    public static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
