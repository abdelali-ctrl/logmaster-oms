package com.logmaster.dto;

import org.bson.Document;

import java.util.List;

/**
 * DTO for dashboard statistics combining PostgreSQL and MongoDB data
 */
public class DashboardStats {

    // PostgreSQL Order Statistics
    private long pendingOrders;
    private long confirmedOrders;
    private long processingOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;

    // MongoDB Log Statistics
    private long errorCount;
    private long warningCount;
    private List<Document> topActiveUsers;
    private List<Document> eventStatistics;
    private List<Document> errorsByService;
    private List<Document> recentLogs;
    private List<Document> hourlyErrorData; // For 24h error chart

    // Constructors
    public DashboardStats() {
    }

    // Utility Methods
    public long getTotalOrders() {
        return pendingOrders + confirmedOrders + processingOrders +
                shippedOrders + deliveredOrders + cancelledOrders;
    }

    public double getSuccessRate() {
        long total = getTotalOrders();
        if (total == 0)
            return 0.0;
        return (double) deliveredOrders / total * 100.0;
    }

    public double getCancellationRate() {
        long total = getTotalOrders();
        if (total == 0)
            return 0.0;
        return (double) cancelledOrders / total * 100.0;
    }

    public long getActiveOrders() {
        return pendingOrders + confirmedOrders + processingOrders + shippedOrders;
    }

    // Getters and Setters
    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getConfirmedOrders() {
        return confirmedOrders;
    }

    public void setConfirmedOrders(long confirmedOrders) {
        this.confirmedOrders = confirmedOrders;
    }

    public long getProcessingOrders() {
        return processingOrders;
    }

    public void setProcessingOrders(long processingOrders) {
        this.processingOrders = processingOrders;
    }

    public long getShippedOrders() {
        return shippedOrders;
    }

    public void setShippedOrders(long shippedOrders) {
        this.shippedOrders = shippedOrders;
    }

    public long getDeliveredOrders() {
        return deliveredOrders;
    }

    public void setDeliveredOrders(long deliveredOrders) {
        this.deliveredOrders = deliveredOrders;
    }

    public long getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(long cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(long warningCount) {
        this.warningCount = warningCount;
    }

    public List<Document> getTopActiveUsers() {
        return topActiveUsers;
    }

    public void setTopActiveUsers(List<Document> topActiveUsers) {
        this.topActiveUsers = topActiveUsers;
    }

    public List<Document> getEventStatistics() {
        return eventStatistics;
    }

    public void setEventStatistics(List<Document> eventStatistics) {
        this.eventStatistics = eventStatistics;
    }

    public List<Document> getErrorsByService() {
        return errorsByService;
    }

    public void setErrorsByService(List<Document> errorsByService) {
        this.errorsByService = errorsByService;
    }

    public List<Document> getRecentLogs() {
        return recentLogs;
    }

    public void setRecentLogs(List<Document> recentLogs) {
        this.recentLogs = recentLogs;
    }

    public List<Document> getHourlyErrorData() {
        return hourlyErrorData;
    }

    public void setHourlyErrorData(List<Document> hourlyErrorData) {
        this.hourlyErrorData = hourlyErrorData;
    }
}
