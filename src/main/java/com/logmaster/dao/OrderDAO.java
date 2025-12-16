package com.logmaster.dao;

import com.logmaster.entity.Order;
import com.logmaster.entity.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderDAO {

    private EntityManager getEntityManager() {
        return EntityManagerProducer.createEntityManager();
    }

    public Order create(Order order) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(order);
            em.flush();
            em.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Order> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return Optional.ofNullable(em.find(Order.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Order> findByIdWithDetails(Long id) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product WHERE o.id = :id", Order.class);
            query.setParameter("id", id);
            List<Order> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    // Keep old method name for compatibility
    public Optional<Order> findByIdWithUser(Long id) {
        return findByIdWithDetails(id);
    }

    public List<Order> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product ORDER BY o.orderDate DESC",
                    Order.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Order> findByUserId(Long userId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.product WHERE o.user.id = :userId ORDER BY o.orderDate DESC",
                    Order.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Order> findByStatus(OrderStatus status) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product WHERE o.status = :status ORDER BY o.orderDate DESC",
                    Order.class)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Order> findRecent(int limit) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.product ORDER BY o.orderDate DESC",
                    Order.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long countByStatus(OrderStatus status) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(o) FROM Order o WHERE o.status = :status", Long.class)
                    .setParameter("status", status)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(o) FROM Order o", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public Order update(Order order) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Order merged = em.merge(order);
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
            Order order = em.find(Order.class, id);
            if (order != null)
                em.remove(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Order updateStatus(Long id, OrderStatus newStatus) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, id);
            if (order != null) {
                order.setStatus(newStatus);
                Order merged = em.merge(order);
                em.getTransaction().commit();
                return merged;
            }
            em.getTransaction().commit();
            return null;
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
