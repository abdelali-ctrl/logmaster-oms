package com.logmaster.service;

import com.logmaster.dao.LogDAO;
import com.logmaster.dao.OrderDAO;
import com.logmaster.dto.DashboardStats;
import com.logmaster.entity.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DashboardService {

    @Inject
    private OrderDAO orderDAO;

    @Inject
    private LogDAO logDAO;

    // Default constructor for CDI
    public DashboardService() {
    }

    /**
     * Get all dashboard statistics from both PostgreSQL and MongoDB
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // PostgreSQL Order Statistics
        stats.setPendingOrders(orderDAO.countByStatus(OrderStatus.PENDING));
        stats.setConfirmedOrders(orderDAO.countByStatus(OrderStatus.CONFIRMED));
        stats.setProcessingOrders(orderDAO.countByStatus(OrderStatus.PROCESSING));
        stats.setShippedOrders(orderDAO.countByStatus(OrderStatus.SHIPPED));
        stats.setDeliveredOrders(orderDAO.countByStatus(OrderStatus.DELIVERED));
        stats.setCancelledOrders(orderDAO.countByStatus(OrderStatus.CANCELLED));

        // MongoDB Log Statistics
        try {
            stats.setErrorCount(logDAO.countErrorLogs());
            stats.setWarningCount(logDAO.countWarningLogs());
            stats.setTopActiveUsers(logDAO.getTopActiveUsers(5));
            stats.setEventStatistics(logDAO.getEventStatistics());
            stats.setErrorsByService(logDAO.getErrorsByServiceLast24h());
            stats.setRecentLogs(logDAO.getRecentLogs(10));
            stats.setHourlyErrorData(logDAO.getHourlyErrorsLast24h()); // For 24h chart
        } catch (Exception e) {
            System.err.println("Failed to get MongoDB stats: " + e.getMessage());
            stats.setErrorCount(0);
            stats.setWarningCount(0);
        }

        return stats;
    }
}
