package com.logmaster.controller;

import com.logmaster.dao.ProductDAO;
import com.logmaster.entity.Product;
import com.logmaster.entity.User;
import com.logmaster.entity.UserRole;
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

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    @Inject
    private ProductDAO productDAO;

    private User getLoggedInUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("user") : null;
    }

    private boolean isAdmin(HttpServletRequest req) {
        User user = getLoggedInUser(req);
        return user != null && user.getRole() == UserRole.ADMIN;
    }

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
                    listProducts(req, resp);
                    break;
                case "create":
                    if (!isAdmin(req)) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
                        return;
                    }
                    showCreateForm(req, resp);
                    break;
                case "edit":
                    if (!isAdmin(req)) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
                        return;
                    }
                    showEditForm(req, resp);
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

        // All POST actions require admin
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
            return;
        }

        String action = req.getParameter("action");
        if (action == null || action.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action required");
            return;
        }

        try {
            switch (action) {
                case "create":
                    createProduct(req, resp);
                    break;
                case "update":
                    updateProduct(req, resp);
                    break;
                case "delete":
                    deleteProduct(req, resp);
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

    private void listProducts(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Product> products = productDAO.findAll();
        req.setAttribute("products", products);
        req.setAttribute("isAdmin", isAdmin(req));
        req.getRequestDispatcher("/WEB-INF/views/productList.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("isAdmin", true);
        req.getRequestDispatcher("/WEB-INF/views/productForm.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String productId = req.getParameter("id");
        if (productId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product ID required");
            return;
        }

        Optional<Product> productOpt = productDAO.findById(Long.parseLong(productId));
        if (productOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
            return;
        }

        req.setAttribute("product", productOpt.get());
        req.setAttribute("editMode", true);
        req.setAttribute("isAdmin", true);
        req.getRequestDispatcher("/WEB-INF/views/productForm.jsp").forward(req, resp);
    }

    private void createProduct(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        String priceStr = req.getParameter("price");
        String stockStr = req.getParameter("stock");
        String category = req.getParameter("category");

        if (name == null || priceStr == null || stockStr == null || category == null) {
            req.setAttribute("error", "Tous les champs sont requis");
            showCreateForm(req, resp);
            return;
        }

        try {
            Product product = new Product();
            product.setName(name);
            product.setPrice(Double.parseDouble(priceStr));
            product.setStock(Integer.parseInt(stockStr));
            product.setCategory(category);

            productDAO.create(product);
            resp.sendRedirect(req.getContextPath() + "/products?action=list");
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Format de nombre invalide");
            showCreateForm(req, resp);
        }
    }

    private void updateProduct(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String productIdStr = req.getParameter("productId");
        String name = req.getParameter("name");
        String priceStr = req.getParameter("price");
        String stockStr = req.getParameter("stock");
        String category = req.getParameter("category");

        try {
            Long productId = Long.parseLong(productIdStr);
            Optional<Product> productOpt = productDAO.findById(productId);

            if (productOpt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
                return;
            }

            Product product = productOpt.get();
            product.setName(name);
            product.setPrice(Double.parseDouble(priceStr));
            product.setStock(Integer.parseInt(stockStr));
            product.setCategory(category);

            productDAO.update(product);
            resp.sendRedirect(req.getContextPath() + "/products?action=list");
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Format de nombre invalide");
            showEditForm(req, resp);
        }
    }

    private void deleteProduct(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String productIdStr = req.getParameter("productId");

        if (productIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product ID required");
            return;
        }

        productDAO.delete(Long.parseLong(productIdStr));
        resp.sendRedirect(req.getContextPath() + "/products?action=list");
    }
}
