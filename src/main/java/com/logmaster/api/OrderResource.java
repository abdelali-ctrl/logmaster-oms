package com.logmaster.api;

import com.logmaster.dao.OrderDAO;
import com.logmaster.dao.ProductDAO;
import com.logmaster.dao.UserDAO;
import com.logmaster.entity.Order;
import com.logmaster.entity.OrderStatus;
import com.logmaster.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Inject
    private OrderService orderService;

    @Inject
    private OrderDAO orderDAO;

    @GET
    public Response getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return Response.ok(ordersToJson(orders)).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") Long id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("Order not found"))
                    .build();
        }
        return Response.ok(orderToJson(order.get())).build();
    }

    @GET
    @Path("/status/{status}")
    public Response getOrdersByStatus(@PathParam("status") String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(orderStatus);
            return Response.ok(ordersToJson(orders)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson("Invalid status: " + status))
                    .build();
        }
    }

    @POST
    public Response createOrder(Map<String, Object> body) {
        try {
            Long userId = ((Number) body.get("userId")).longValue();
            Long productId = ((Number) body.get("productId")).longValue();
            Integer quantity = ((Number) body.get("quantity")).intValue();
            String shippingAddress = (String) body.get("shippingAddress");
            String notes = (String) body.get("notes");

            Order order = orderService.createOrder(userId, productId, quantity, shippingAddress, notes);

            return Response.status(Response.Status.CREATED)
                    .entity(orderToJson(order))
                    .build();

        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorJson("Server error: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") Long id, Map<String, String> body) {
        try {
            String statusStr = body.get("status");
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderService.updateStatus(id, newStatus);

            return Response.ok(orderToJson(order)).build();

        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrder(@PathParam("id") Long id) {
        try {
            orderService.deleteOrder(id);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/count")
    public Response getOrderCount() {
        long count = orderDAO.count();
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Response.ok(result).build();
    }

    // Helper methods
    private List<Map<String, Object>> ordersToJson(List<Order> orders) {
        return orders.stream().map(this::orderToJson).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> orderToJson(Order order) {
        Map<String, Object> json = new HashMap<>();
        json.put("id", order.getId());
        json.put("userId", order.getUser().getId());
        json.put("userName", order.getUser().getName());
        json.put("userEmail", order.getUser().getEmail());
        json.put("productId", order.getProduct().getId());
        json.put("productName", order.getProduct().getName());
        json.put("quantity", order.getQuantity());
        json.put("totalAmount", order.getTotalAmount());
        json.put("orderDate", order.getOrderDate().toString());
        json.put("status", order.getStatus().toString());
        json.put("shippingAddress", order.getShippingAddress());
        json.put("notes", order.getNotes());
        return json;
    }

    private Map<String, String> errorJson(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
