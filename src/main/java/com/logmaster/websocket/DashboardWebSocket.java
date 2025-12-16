package com.logmaster.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * WebSocket endpoint for real-time dashboard updates
 */
@ServerEndpoint("/ws/dashboard")
public class DashboardWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("WebSocket opened: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket closed: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message received from " + session.getId() + ": " + message);
        // Echo back or handle specific messages
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error for " + session.getId() + ": " + error.getMessage());
        sessions.remove(session);
    }

    /**
     * Broadcast a message to all connected clients
     */
    public static void broadcast(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        System.err.println("Failed to send message: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Notify clients about a new order
     */
    public static void notifyNewOrder(Long orderId, String productName, Double amount) {
        String json = String.format(
                "{\"type\":\"NEW_ORDER\",\"orderId\":%d,\"productName\":\"%s\",\"amount\":%.2f}",
                orderId, productName.replace("\"", "\\\""), amount);
        broadcast(json);
    }

    /**
     * Notify clients about order status change
     */
    public static void notifyStatusChange(Long orderId, String oldStatus, String newStatus) {
        String json = String.format(
                "{\"type\":\"STATUS_CHANGE\",\"orderId\":%d,\"oldStatus\":\"%s\",\"newStatus\":\"%s\"}",
                orderId, oldStatus, newStatus);
        broadcast(json);
    }

    /**
     * Notify clients to refresh stats
     */
    public static void notifyRefreshStats() {
        broadcast("{\"type\":\"REFRESH_STATS\"}");
    }
}
