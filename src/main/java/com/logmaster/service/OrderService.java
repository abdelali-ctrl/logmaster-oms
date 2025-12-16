package com.logmaster.service;

import com.logmaster.dao.LogDAO;
import com.logmaster.dao.OrderDAO;
import com.logmaster.dao.ProductDAO;
import com.logmaster.dao.UserDAO;
import com.logmaster.entity.Order;
import com.logmaster.entity.OrderStatus;
import com.logmaster.entity.Product;
import com.logmaster.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.logmaster.websocket.DashboardWebSocket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderDAO orderDAO;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ProductDAO productDAO;

    @Inject
    private LogDAO logDAO;

    public OrderService() {
    }

    /**
     * Create a new order with stock validation
     */
    public Order createOrder(Long userId, Long productId, Integer quantity, String shippingAddress, String notes) {
        // Find the user
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId);
        }
        User user = userOpt.get();

        // Find the product
        Optional<Product> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Produit non trouvé avec l'ID: " + productId);
        }
        Product product = productOpt.get();

        // Check stock availability
        if (product.getStock() < quantity) {
            throw new IllegalStateException(
                    String.format("Stock insuffisant! Disponible: %d, Demandé: %d",
                            product.getStock(), quantity));
        }

        // Calculate total
        Double totalAmount = product.getPrice() * quantity;

        // Create the order
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setNotes(notes);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // Save order
        Order savedOrder = orderDAO.create(order);

        // Decrease stock
        product.setStock(product.getStock() - quantity);
        productDAO.update(product);

        // Log to MongoDB
        try {
            logDAO.logOrderCreation(savedOrder.getId(), user.getEmail(), totalAmount, shippingAddress);
        } catch (Exception e) {
            System.err.println("Failed to log order creation to MongoDB: " + e.getMessage());
        }

        // WebSocket notification
        try {
            DashboardWebSocket.notifyNewOrder(savedOrder.getId(), product.getName(), totalAmount);
        } catch (Exception e) {
            System.err.println("WebSocket notification failed: " + e.getMessage());
        }

        return savedOrder;
    }

    /**
     * Create a new order from an Order object (for multi-product orders)
     */
    public Order createOrder(Order order) {
        // Ensure order date and status
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }

        // Save order
        Order savedOrder = orderDAO.create(order);

        // Log to MongoDB
        try {
            logDAO.logOrderCreation(savedOrder.getId(), order.getUser().getEmail(),
                    order.getTotalAmount(), order.getShippingAddress());
        } catch (Exception e) {
            System.err.println("Failed to log order creation to MongoDB: " + e.getMessage());
        }

        // WebSocket notification
        try {
            String productSummary = order.getProductsSummary();
            DashboardWebSocket.notifyNewOrder(savedOrder.getId(), productSummary, order.getTotalAmount());
        } catch (Exception e) {
            System.err.println("WebSocket notification failed: " + e.getMessage());
        }

        return savedOrder;
    }

    /**
     * Update order status with logging
     */
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Optional<Order> orderOpt = orderDAO.findByIdWithDetails(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Commande non trouvée avec l'ID: " + orderId);
        }

        Order order = orderOpt.get();
        OrderStatus oldStatus = order.getStatus();

        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new IllegalStateException(
                    String.format("Transition de statut invalide: %s → %s", oldStatus, newStatus));
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderDAO.update(order);

        try {
            logDAO.logStatusChange(orderId, oldStatus, newStatus);
        } catch (Exception e) {
            System.err.println("Failed to log status change to MongoDB: " + e.getMessage());
        }

        // WebSocket notification
        try {
            DashboardWebSocket.notifyStatusChange(orderId, oldStatus.toString(), newStatus.toString());
        } catch (Exception e) {
            System.err.println("WebSocket notification failed: " + e.getMessage());
        }

        return updatedOrder;
    }

    /**
     * Cancel an order and restore stock
     */
    public Order cancelOrder(Long orderId, String reason) {
        Optional<Order> orderOpt = orderDAO.findByIdWithDetails(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Commande non trouvée avec l'ID: " + orderId);
        }

        Order order = orderOpt.get();
        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Impossible d'annuler une commande déjà expédiée ou livrée");
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException("La commande est déjà annulée");
        }

        // Restore stock
        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productDAO.update(product);

        // Cancel order
        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(reason);
        Order cancelledOrder = orderDAO.update(order);

        try {
            logDAO.logOrderCancellation(orderId, reason);
        } catch (Exception e) {
            System.err.println("Failed to log cancellation to MongoDB: " + e.getMessage());
        }

        return cancelledOrder;
    }

    /**
     * Delete an order
     */
    public void deleteOrder(Long orderId) {
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Commande non trouvée avec l'ID: " + orderId);
        }

        orderDAO.delete(orderId);

        try {
            logDAO.logOrderDeletion(orderId);
        } catch (Exception e) {
            System.err.println("Failed to log deletion to MongoDB: " + e.getMessage());
        }
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderDAO.findByIdWithDetails(orderId);
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderDAO.findByStatus(status);
    }

    public List<Order> getRecentOrders(int limit) {
        return orderDAO.findRecent(limit);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderDAO.findByUserId(userId);
    }

    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}
