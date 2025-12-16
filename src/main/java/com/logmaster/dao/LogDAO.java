package com.logmaster.dao;

import com.logmaster.entity.OrderStatus;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

@ApplicationScoped
public class LogDAO {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> logsCollection;

    public LogDAO() {
        init();
    }

    @PostConstruct
    public void init() {
        if (mongoClient != null)
            return; // Already initialized

        // Support multiple env var formats
        String connectionString = System.getenv("MONGODB_URI");
        if (connectionString == null || connectionString.isEmpty()) {
            String mongoHost = System.getenv("MONGODB_HOST");
            String mongoPort = System.getenv("MONGODB_PORT");
            if (mongoHost != null && !mongoHost.isEmpty()) {
                connectionString = "mongodb://" + mongoHost + ":" + (mongoPort != null ? mongoPort : "27017");
            } else {
                connectionString = "mongodb://localhost:27017";
            }
        }

        try {
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("logmaster_db");
            logsCollection = database.getCollection("application_logs");

            logsCollection.createIndex(new Document("timestamp", -1));
            logsCollection.createIndex(new Document("level", 1));
            logsCollection.createIndex(new Document("service", 1));
            logsCollection.createIndex(new Document("event_type", 1));
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB: " + e.getMessage());
        }
    }

    public void logOrderCreation(Long orderId, String userEmail, Double amount, String address) {
        if (logsCollection == null)
            return;

        Document log = new Document()
                .append("timestamp", new Date())
                .append("level", "INFO")
                .append("event_type", "ORDER_CREATED")
                .append("service", "order-service")
                .append("message", "New order created")
                .append("metadata", new Document()
                        .append("order_id", orderId)
                        .append("user_email", userEmail)
                        .append("amount", amount)
                        .append("shipping_address", address));

        logsCollection.insertOne(log);
    }

    public void logStatusChange(Long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        if (logsCollection == null)
            return;

        Document log = new Document()
                .append("timestamp", new Date())
                .append("level", "INFO")
                .append("event_type", "ORDER_STATUS_CHANGED")
                .append("service", "order-service")
                .append("message", String.format("Order status changed: %s → %s", oldStatus, newStatus))
                .append("metadata", new Document()
                        .append("order_id", orderId)
                        .append("old_status", oldStatus.toString())
                        .append("new_status", newStatus.toString()));

        logsCollection.insertOne(log);
    }

    public void logOrderCancellation(Long orderId, String reason) {
        if (logsCollection == null)
            return;

        Document log = new Document()
                .append("timestamp", new Date())
                .append("level", "WARNING")
                .append("event_type", "ORDER_CANCELLED")
                .append("service", "order-service")
                .append("message", "Order cancelled: " + reason)
                .append("metadata", new Document()
                        .append("order_id", orderId)
                        .append("reason", reason));

        logsCollection.insertOne(log);
    }

    public void logOrderDeletion(Long orderId) {
        if (logsCollection == null)
            return;

        Document log = new Document()
                .append("timestamp", new Date())
                .append("level", "WARNING")
                .append("event_type", "ORDER_DELETED")
                .append("service", "order-service")
                .append("message", "Order permanently deleted")
                .append("metadata", new Document("order_id", orderId));

        logsCollection.insertOne(log);
    }

    public void logError(String service, String message, String stackTrace) {
        if (logsCollection == null)
            return;

        Document log = new Document()
                .append("timestamp", new Date())
                .append("level", "ERROR")
                .append("event_type", "SYSTEM_ERROR")
                .append("service", service)
                .append("message", message)
                .append("stack_trace", stackTrace);

        logsCollection.insertOne(log);
    }

    public List<Document> getRecentLogs(int limit) {
        if (logsCollection == null)
            return new ArrayList<>();

        return logsCollection.find()
                .sort(descending("timestamp"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    public List<Document> getLogsByLevel(String level, int limit) {
        if (logsCollection == null)
            return new ArrayList<>();

        return logsCollection.find(eq("level", level))
                .sort(descending("timestamp"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    public List<Document> getLogsByService(String service, int limit) {
        if (logsCollection == null)
            return new ArrayList<>();

        return logsCollection.find(eq("service", service))
                .sort(descending("timestamp"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    public long countByLevel(String level) {
        if (logsCollection == null)
            return 0;
        return logsCollection.countDocuments(eq("level", level));
    }

    public long countErrorLogs() {
        return countByLevel("ERROR");
    }

    public long countWarningLogs() {
        return countByLevel("WARNING");
    }

    public List<Document> getTopActiveUsers(int limit) {
        if (logsCollection == null)
            return new ArrayList<>();

        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("event_type", "ORDER_CREATED")),
                new Document("$group", new Document()
                        .append("_id", "$metadata.user_email")
                        .append("order_count", new Document("$sum", 1))),
                new Document("$sort", new Document("order_count", -1)),
                new Document("$limit", limit));

        return logsCollection.aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> getEventStatistics() {
        if (logsCollection == null)
            return new ArrayList<>();

        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document()
                        .append("_id", "$event_type")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)));

        return logsCollection.aggregate(pipeline).into(new ArrayList<>());
    }

    public List<Document> getErrorsByServiceLast24h() {
        if (logsCollection == null)
            return new ArrayList<>();

        Date twentyFourHoursAgo = new Date(System.currentTimeMillis() - 86400000);

        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document()
                        .append("level", "ERROR")
                        .append("timestamp", new Document("$gte", twentyFourHoursAgo))),
                new Document("$group", new Document()
                        .append("_id", "$service")
                        .append("error_count", new Document("$sum", 1))
                        .append("last_error", new Document("$max", "$timestamp"))),
                new Document("$sort", new Document("error_count", -1)));

        return logsCollection.aggregate(pipeline).into(new ArrayList<>());
    }

    /**
     * Get hourly error distribution for the last 24 hours (for chart)
     */
    public List<Document> getHourlyErrorsLast24h() {
        if (logsCollection == null)
            return new ArrayList<>();

        Date twentyFourHoursAgo = new Date(System.currentTimeMillis() - 86400000);

        List<Document> pipeline = Arrays.asList(
                // Filter errors from last 24 hours
                new Document("$match", new Document()
                        .append("level", new Document("$in", Arrays.asList("ERROR", "WARNING")))
                        .append("timestamp", new Document("$gte", twentyFourHoursAgo))),

                // Extract hour from timestamp
                new Document("$project", new Document()
                        .append("level", 1)
                        .append("hour", new Document("$hour", "$timestamp"))),

                // Group by hour and level
                new Document("$group", new Document()
                        .append("_id", new Document()
                                .append("hour", "$hour")
                                .append("level", "$level"))
                        .append("count", new Document("$sum", 1))),

                // Sort by hour
                new Document("$sort", new Document("_id.hour", 1)));

        return logsCollection.aggregate(pipeline).into(new ArrayList<>());
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
