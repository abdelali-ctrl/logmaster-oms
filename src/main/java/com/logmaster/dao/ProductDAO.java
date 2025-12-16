package com.logmaster.dao;

import com.logmaster.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductDAO {

    private EntityManager getEntityManager() {
        return EntityManagerProducer.createEntityManager();
    }

    public Product create(Product product) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
            return product;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Product> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(Product.class, id));
        } finally {
            em.close();
        }
    }

    public List<Product> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Product p ORDER BY p.name", Product.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Product> findByCategory(String category) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.category = :category ORDER BY p.name", Product.class)
                    .setParameter("category", category)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Product> findInStock() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Product p WHERE p.stock > 0 ORDER BY p.name", Product.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Product update(Product product) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Product merged = em.merge(product);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Product product = em.find(Product.class, id);
            if (product != null)
                em.remove(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<String> findAllCategories() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT p.category FROM Product p ORDER BY p.category", String.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
