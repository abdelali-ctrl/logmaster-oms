package com.logmaster.controller;

import com.logmaster.dao.UserDAO;
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

@WebServlet("/users")
public class UserServlet extends HttpServlet {

    @Inject
    private UserDAO userDAO;

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
                    // Only admins can list all users
                    if (!isAdmin(req)) {
                        resp.sendRedirect(req.getContextPath() + "/users?action=myaccount");
                        return;
                    }
                    listUsers(req, resp);
                    break;
                case "myaccount":
                    showMyAccount(req, resp);
                    break;
                case "create":
                    // Only admins can create users
                    if (!isAdmin(req)) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
                        return;
                    }
                    showCreateForm(req, resp);
                    break;
                case "edit":
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

        String action = req.getParameter("action");
        if (action == null || action.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Action required");
            return;
        }

        try {
            switch (action) {
                case "create":
                    // Only admins can create users
                    if (!isAdmin(req)) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
                        return;
                    }
                    createUser(req, resp);
                    break;
                case "update":
                    updateUser(req, resp);
                    break;
                case "delete":
                    // Only admins can delete users
                    if (!isAdmin(req)) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin only");
                        return;
                    }
                    deleteUser(req, resp);
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

    private void listUsers(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<User> users = userDAO.findAll();
        req.setAttribute("users", users);
        req.setAttribute("isAdmin", true);
        req.getRequestDispatcher("/WEB-INF/views/userList.jsp").forward(req, resp);
    }

    private void showMyAccount(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User loggedInUser = getLoggedInUser(req);
        req.setAttribute("user", loggedInUser);
        req.setAttribute("editMode", true);
        req.setAttribute("isMyAccount", true);
        req.setAttribute("isAdmin", isAdmin(req));
        req.getRequestDispatcher("/WEB-INF/views/userForm.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("isAdmin", true);
        req.getRequestDispatcher("/WEB-INF/views/userForm.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String userId = req.getParameter("id");
        User loggedInUser = getLoggedInUser(req);

        if (userId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID required");
            return;
        }

        Long userIdLong = Long.parseLong(userId);

        // Regular users can only edit their own account
        if (!isAdmin(req) && !loggedInUser.getId().equals(userIdLong)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous ne pouvez modifier que votre propre compte");
            return;
        }

        Optional<User> userOpt = userDAO.findById(userIdLong);
        if (userOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        req.setAttribute("user", userOpt.get());
        req.setAttribute("editMode", true);
        req.setAttribute("isAdmin", isAdmin(req));
        req.getRequestDispatcher("/WEB-INF/views/userForm.jsp").forward(req, resp);
    }

    private void createUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String roleStr = req.getParameter("role");

        if (name == null || email == null || password == null ||
                name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Tous les champs sont requis");
            showCreateForm(req, resp);
            return;
        }

        // Check if email already exists
        if (userDAO.findByEmail(email).isPresent()) {
            req.setAttribute("error", "Cet email existe déjà");
            showCreateForm(req, resp);
            return;
        }

        User user = new User(name, email, password);
        if (roleStr != null && !roleStr.isEmpty()) {
            user.setRole(UserRole.valueOf(roleStr));
        }
        userDAO.create(user);
        resp.sendRedirect(req.getContextPath() + "/users?action=list");
    }

    private void updateUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String userIdStr = req.getParameter("userId");
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String roleStr = req.getParameter("role");

        User loggedInUser = getLoggedInUser(req);

        try {
            Long userId = Long.parseLong(userIdStr);

            // Regular users can only update their own account
            if (!isAdmin(req) && !loggedInUser.getId().equals(userId)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous ne pouvez modifier que votre propre compte");
                return;
            }

            Optional<User> userOpt = userDAO.findById(userId);

            if (userOpt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }

            User user = userOpt.get();
            user.setName(name);
            user.setEmail(email);
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);
            }
            // Only admins can change roles
            if (isAdmin(req) && roleStr != null && !roleStr.isEmpty()) {
                user.setRole(UserRole.valueOf(roleStr));
            }

            userDAO.update(user);

            // Update session if user updated their own account
            if (loggedInUser.getId().equals(userId)) {
                HttpSession session = req.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userName", user.getName());
            }

            if (isAdmin(req)) {
                resp.sendRedirect(req.getContextPath() + "/users?action=list");
            } else {
                resp.sendRedirect(req.getContextPath() + "/users?action=myaccount&success=true");
            }
        } catch (NumberFormatException e) {
            req.setAttribute("error", "ID utilisateur invalide");
            showEditForm(req, resp);
        }
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String userIdStr = req.getParameter("userId");

        if (userIdStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID required");
            return;
        }

        Long userId = Long.parseLong(userIdStr);
        User loggedInUser = getLoggedInUser(req);

        // Prevent admin from deleting themselves
        if (loggedInUser.getId().equals(userId)) {
            req.setAttribute("error", "Vous ne pouvez pas supprimer votre propre compte");
            listUsers(req, resp);
            return;
        }

        userDAO.delete(userId);
        resp.sendRedirect(req.getContextPath() + "/users?action=list");
    }
}
