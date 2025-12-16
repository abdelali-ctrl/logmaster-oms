package com.logmaster.filter;

import com.logmaster.entity.User;
import com.logmaster.entity.UserRole;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication and Authorization filter
 * Protects routes based on user roles
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        String queryString = httpRequest.getQueryString();
        String fullPath = queryString != null ? path + "?" + queryString : path;

        // Allow access to public resources without authentication
        if (isPublicResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if user is logged in
        HttpSession session = httpRequest.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (loggedInUser == null) {
            // Not authenticated, redirect to login
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        // Check role-based access
        if (requiresAdmin(path, fullPath) && loggedInUser.getRole() != UserRole.ADMIN) {
            // User trying to access admin-only resource
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Accès réservé aux administrateurs");
            return;
        }

        // User is authenticated and authorized
        chain.doFilter(request, response);
    }

    private boolean isPublicResource(String path) {
        return path.equals("/login") ||
                path.equals("/register") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/api/");
    }

    /**
     * Paths that require ADMIN role
     */
    private boolean requiresAdmin(String path, String fullPath) {
        // Users list and create (not myaccount or edit own)
        if (path.equals("/users")) {
            // Allow myaccount for all users
            if (fullPath.contains("action=myaccount")) {
                return false;
            }
            // Allow edit for own account (checked in servlet)
            if (fullPath.contains("action=edit")) {
                return false;
            }
            // List and create require admin
            if (fullPath.contains("action=list") || fullPath.contains("action=create") ||
                    fullPath.contains("action=delete") || !fullPath.contains("action=")) {
                return true;
            }
        }

        // Logs are admin-only
        if (path.equals("/logs") || path.startsWith("/logs?")) {
            return true;
        }

        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
