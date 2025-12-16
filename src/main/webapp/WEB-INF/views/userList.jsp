<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!DOCTYPE html>
        <html lang="fr">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Utilisateurs - LogMaster</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <div class="container">
                <header>
                    <h1>👥 Gestion des Utilisateurs</h1>
                    <nav class="nav-links">
                        <a href="${pageContext.request.contextPath}/dashboard">🏠 Dashboard</a>
                        <a href="${pageContext.request.contextPath}/orders?action=list">📦 Commandes</a>
                        <a href="${pageContext.request.contextPath}/products?action=list">🛍️ Produits</a>
                        <a href="${pageContext.request.contextPath}/users?action=list" class="active">👥
                            Utilisateurs</a>
                        <a href="${pageContext.request.contextPath}/logs?action=list">📋 Logs</a>
                    </nav>
                </header>

                <div class="actions-bar">
                    <h2>Liste des Utilisateurs</h2>
                    <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-success">
                        ➕ Nouvel Utilisateur
                    </a>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-error">${error}</div>
                </c:if>

                <c:choose>
                    <c:when test="${not empty users}">
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nom</th>
                                    <th>Email</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="user" items="${users}">
                                    <tr>
                                        <td>#${user.id}</td>
                                        <td><strong>${user.name}</strong></td>
                                        <td>${user.email}</td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/users?action=edit&id=${user.id}"
                                                class="btn btn-primary btn-sm">✏️ Modifier</a>
                                            <form method="POST" action="${pageContext.request.contextPath}/users"
                                                style="display:inline"
                                                onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="userId" value="${user.id}">
                                                <button type="submit" class="btn btn-danger btn-sm">🗑️
                                                    Supprimer</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <h3>Aucun utilisateur</h3>
                            <p>Commencez par ajouter des utilisateurs.</p>
                            <a href="${pageContext.request.contextPath}/users?action=create" class="btn btn-success">
                                ➕ Ajouter un utilisateur
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </body>

        </html>