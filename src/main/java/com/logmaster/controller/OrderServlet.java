package com.logmaster.controller;

import com.logmaster.dao.ProductDAO;
import com.logmaster.dao.UserDAO;
import com.logmaster.entity.Order;
import com.logmaster.entity.OrderItem;
import com.logmaster.entity.OrderStatus;
import com.logmaster.entity.Product;
import com.logmaster.entity.User;
import com.logmaster.entity.UserRole;
import com.logmaster.service.OrderService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    @Inject
    private OrderService orderService;

    @Inject
    private UserDAO userDAO;

    @Inject
    private ProductDAO productDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        try {
            switch (action) {
                case "list":
                    listOrders(req, resp);
                    break;
                case "myorders":
                    listMyOrders(req, resp);
                    break;
                case "detail":
                    showOrderDetail(req, resp);
                    break;
                case "create":
                    showCreateForm(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null || action.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action required");
            return;
        }

        try {
            switch (action) {
                case "create":
                    createOrder(req, resp);
                    break;
                case "createMulti":
                    createMultiOrder(req, resp);
                    break;
                case "updateStatus":
                    updateOrderStatus(req, resp);
                    break;
                case "cancel":
                    cancelOrder(req, resp);
                    break;
                case "delete":
                    deleteOrder(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private User getLoggedInUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("user") : null;
    }

    private boolean isAdmin(HttpServletRequest req) {
        User user = getLoggedInUser(req);
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    private void listOrders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Only admins can see all orders
        if (!isAdmin(req)) {
            // Redirect regular users to their orders
            resp.sendRedirect(req.getContextPath() + "/orders?action=myorders");
            return;
        }

        String statusFilter = req.getParameter("status");
        List<Order> orders;

        if (statusFilter != null && !statusFilter.isEmpty()) {
            try {
                OrderStatus status = OrderStatus.valueOf(statusFilter);
                orders = orderService.getOrdersByStatus(status);
                req.setAttribute("currentStatus", status);
            } catch (IllegalArgumentException e) {
                orders = orderService.getAllOrders();
            }
        } else {
            orders = orderService.getAllOrders();
        }

        req.setAttribute("orders", orders);
        req.setAttribute("statuses", OrderStatus.values());
        req.setAttribute("isAdmin", true);
        req.getRequestDispatcher("/WEB-INF/views/orderList.jsp").forward(req, resp);
    }

    private void listMyOrders(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getLoggedInUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Order> orders = orderService.getOrdersByUser(user.getId());
        req.setAttribute("orders", orders);
        req.setAttribute("statuses", OrderStatus.values());
        req.setAttribute("isAdmin", isAdmin(req));
        req.setAttribute("isMyOrders", true);
        req.getRequestDispatcher("/WEB-INF/views/orderList.jsp").forward(req, resp);
    }

    private void showOrderDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String orderId = req.getParameter("id");
        if (orderId == null || orderId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID required");
            return;
        }

        Optional<Order> orderOpt = orderService.getOrderById(Long.parseLong(orderId));
        if (orderOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        Order order = orderOpt.get();
        User loggedInUser = getLoggedInUser(req);

        // Regular users can only view their own orders
        if (!isAdmin(req) && !order.getUser().getId().equals(loggedInUser.getId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous ne pouvez voir que vos propres commandes");
            return;
        }

        req.setAttribute("order", order);
        req.setAttribute("isAdmin", isAdmin(req));
        req.getRequestDispatcher("/WEB-INF/views/orderDetail.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User loggedInUser = getLoggedInUser(req);

        // For regular users, only show in-stock products and their own user
        if (isAdmin(req)) {
            List<User> users = userDAO.findAll();
            req.setAttribute("users", users);
        } else {
            // Regular user can only order for themselves
            req.setAttribute("selectedUser", loggedInUser);
        }

        List<Product> products = productDAO.findInStock();
        req.setAttribute("products", products);
        req.setAttribute("isAdmin", isAdmin(req));

        // Use cart page for multi-product ordering
        req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, resp);
    }

    private void createOrder(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User loggedInUser = getLoggedInUser(req);
        String userIdStr = req.getParameter("userId");
        String productIdStr = req.getParameter("productId");
        String quantityStr = req.getParameter("quantity");
        String shippingAddress = req.getParameter("shippingAddress");
        String notes = req.getParameter("notes");

        // For regular users, force userId to their own
        Long userId;
        if (isAdmin(req) && userIdStr != null) {
            userId = Long.parseLong(userIdStr);
        } else {
            userId = loggedInUser.getId();
        }

        if (productIdStr == null || quantityStr == null || shippingAddress == null) {
            req.setAttribute("error", "Tous les champs requis doivent être remplis");
            showCreateForm(req, resp);
            return;
        }

        try {
            Long productId = Long.parseLong(productIdStr);
            Integer quantity = Integer.parseInt(quantityStr);

            if (quantity <= 0) {
                req.setAttribute("error", "La quantité doit être supérieure à 0");
                showCreateForm(req, resp);
                return;
            }

            Order order = orderService.createOrder(userId, productId, quantity, shippingAddress, notes);
            resp.sendRedirect(req.getContextPath() + "/orders?action=detail&id=" + order.getId());

        } catch (NumberFormatException e) {
            req.setAttribute("error", "Format de nombre invalide");
            showCreateForm(req, resp);
        } catch (IllegalStateException | IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            showCreateForm(req, resp);
        }
    }

    private void updateOrderStatus(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Only admins can update order status
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Seuls les administrateurs peuvent modifier le statut");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        String statusStr = req.getParameter("status");

        if (orderIdStr == null || statusStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID and status required");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            OrderStatus newStatus = OrderStatus.valueOf(statusStr);

            orderService.updateStatus(orderId, newStatus);
            resp.sendRedirect(req.getContextPath() + "/orders?action=detail&id=" + orderId);

        } catch (IllegalStateException | IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            showOrderDetail(req, resp);
        }
    }

    private void cancelOrder(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Only admins can cancel orders
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Seuls les administrateurs peuvent annuler les commandes");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        String reason = req.getParameter("reason");

        if (orderIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID required");
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            reason = "Aucune raison fournie";
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            orderService.cancelOrder(orderId, reason);
            resp.sendRedirect(req.getContextPath() + "/orders?action=detail&id=" + orderId);

        } catch (IllegalStateException | IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private void deleteOrder(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Only admins can delete orders
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Seuls les administrateurs peuvent supprimer les commandes");
            return;
        }

        String orderIdStr = req.getParameter("orderId");

        if (orderIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID required");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr);
            orderService.deleteOrder(orderId);
            resp.sendRedirect(req.getContextPath() + "/orders?action=list");

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private void createMultiOrder(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User loggedInUser = getLoggedInUser(req);
        String shippingAddress = req.getParameter("shippingAddress");
        String notes = req.getParameter("notes");

        // Parse cart items from form
        java.util.Map<Long, Integer> cartItems = new java.util.HashMap<>();
        java.util.Enumeration<String> paramNames = req.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.startsWith("items[") && paramName.endsWith("]")) {
                try {
                    String productIdStr = paramName.substring(6, paramName.length() - 1);
                    Long productId = Long.parseLong(productIdStr);
                    Integer quantity = Integer.parseInt(req.getParameter(paramName));
                    if (quantity > 0) {
                        cartItems.put(productId, quantity);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }

        if (cartItems.isEmpty()) {
            req.setAttribute("error", "Panier vide");
            showCreateForm(req, resp);
            return;
        }

        if (shippingAddress == null || shippingAddress.isEmpty()) {
            req.setAttribute("error", "Adresse de livraison requise");
            showCreateForm(req, resp);
            return;
        }

        try {
            // Create order with items
            Order order = new Order();
            order.setUser(loggedInUser);
            order.setShippingAddress(shippingAddress);
            order.setNotes(notes);

            double total = 0;
            for (java.util.Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
                Optional<Product> productOpt = productDAO.findById(entry.getKey());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    int qty = entry.getValue();

                    // Validate stock
                    if (product.getStock() < qty) {
                        req.setAttribute("error", "Stock insuffisant pour " + product.getName());
                        showCreateForm(req, resp);
                        return;
                    }

                    // Create order item
                    OrderItem item = new OrderItem(product, qty);
                    order.addItem(item);
                    total += item.getSubtotal();

                    // Update stock
                    product.setStock(product.getStock() - qty);
                    productDAO.update(product);
                }
            }

            order.setTotalAmount(total);
            orderService.createOrder(order);

            resp.sendRedirect(req.getContextPath() + "/orders?action=myorders");

        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            showCreateForm(req, resp);
        }
    }
}
