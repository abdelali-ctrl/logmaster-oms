package com.logmaster.controller;

import com.logmaster.dao.LogDAO;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

@WebServlet("/logs")
public class LogServlet extends HttpServlet {

    @Inject
    private LogDAO logDAO;

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
                    listLogs(req, resp);
                    break;
                case "filter":
                    filterLogs(req, resp);
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

    private void listLogs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String limitParam = req.getParameter("limit");
        int limit = (limitParam != null && !limitParam.isEmpty()) ? Integer.parseInt(limitParam) : 50;

        List<Document> logs = logDAO.getRecentLogs(limit);

        req.setAttribute("logs", logs);
        req.setAttribute("currentLimit", limit);
        req.getRequestDispatcher("/WEB-INF/views/logList.jsp").forward(req, resp);
    }

    private void filterLogs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String level = req.getParameter("level");
        String service = req.getParameter("service");
        String limitParam = req.getParameter("limit");
        int limit = (limitParam != null && !limitParam.isEmpty()) ? Integer.parseInt(limitParam) : 50;

        List<Document> logs;

        if (level != null && !level.isEmpty()) {
            logs = logDAO.getLogsByLevel(level, limit);
            req.setAttribute("filterType", "level");
            req.setAttribute("filterValue", level);
        } else if (service != null && !service.isEmpty()) {
            logs = logDAO.getLogsByService(service, limit);
            req.setAttribute("filterType", "service");
            req.setAttribute("filterValue", service);
        } else {
            logs = logDAO.getRecentLogs(limit);
        }

        req.setAttribute("logs", logs);
        req.setAttribute("currentLimit", limit);
        req.getRequestDispatcher("/WEB-INF/views/logList.jsp").forward(req, resp);
    }
}
