package com.logmaster.controller;

import com.logmaster.dao.UserDAO;
import com.logmaster.entity.User;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Inject
    private UserDAO userDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");

        if ("logout".equals(action)) {
            // Logout - invalidate session
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login?message=logout");
            return;
        }

        // Check if already logged in
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        // Show login page
        String message = req.getParameter("message");
        if ("logout".equals(message)) {
            req.setAttribute("success", "Vous avez été déconnecté avec succès.");
        }

        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Email et mot de passe requis");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // Find user by email
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            req.setAttribute("error", "Email ou mot de passe incorrect");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        User user = userOpt.get();

        // Simple password check (in production, use BCrypt!)
        if (!user.getPassword().equals(password)) {
            req.setAttribute("error", "Email ou mot de passe incorrect");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // Login successful - create session
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("userName", user.getName());
        session.setAttribute("userRole", user.getRole().toString());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Redirect to dashboard
        resp.sendRedirect(req.getContextPath() + "/dashboard");
    }
}
