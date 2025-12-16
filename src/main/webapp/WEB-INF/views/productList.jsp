<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Produits - LogMaster</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            </head>

            <body>
                <div class="container">
                    <header>
                        <div class="header-top">
                            <h1>📦 Catalogue Produits</h1>
                            <div class="user-info">
                                <span class="user-name">👤 ${sessionScope.userName}</span>
                                <span
                                    class="user-role ${sessionScope.userRole == 'ADMIN' ? 'role-admin' : 'role-user'}">
                                    ${sessionScope.userRole}
                                </span>
                                <a href="${pageContext.request.contextPath}/login?action=logout" class="btn btn-logout">
                                    🚪 Déconnexion
                                </a>
                            </div>
                        </div>
                        <nav class="nav-links">
                            <a href="${pageContext.request.contextPath}/dashboard">🏠 Dashboard</a>
                            <c:if test="${sessionScope.userRole == 'ADMIN'}">
                                <a href="${pageContext.request.contextPath}/orders?action=list">📦 Toutes les
                                    Commandes</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/orders?action=myorders">🛒 Mes Commandes</a>
                            <a href="${pageContext.request.contextPath}/products?action=list" class="active">🛍️
                                Produits</a>
                            <c:if test="${sessionScope.userRole == 'ADMIN'}">
                                <a href="${pageContext.request.contextPath}/users?action=list">👥 Utilisateurs</a>
                                <a href="${pageContext.request.contextPath}/logs?action=list">📋 Logs</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/users?action=myaccount">⚙️ Mon Compte</a>
                        </nav>
                    </header>

                    <div class="actions-bar">
                        <h2>Liste des Produits</h2>
                        <c:if test="${isAdmin}">
                            <a href="${pageContext.request.contextPath}/products?action=create" class="btn btn-success">
                                ➕ Nouveau Produit
                            </a>
                        </c:if>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <c:choose>
                        <c:when test="${not empty products}">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Nom</th>
                                        <th>Prix</th>
                                        <th>Stock</th>
                                        <th>Catégorie</th>
                                        <c:if test="${isAdmin}">
                                            <th>Actions</th>
                                        </c:if>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="product" items="${products}">
                                        <tr>
                                            <td>#${product.id}</td>
                                            <td><strong>${product.name}</strong></td>
                                            <td>
                                                <fmt:formatNumber value="${product.price}" type="currency"
                                                    currencySymbol="€" />
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${product.stock == 0}">
                                                        <span class="status status-cancelled">Rupture</span>
                                                    </c:when>
                                                    <c:when test="${product.stock < 10}">
                                                        <span class="status status-pending">${product.stock}
                                                            unités</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status status-delivered">${product.stock}
                                                            unités</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><code>${product.category}</code></td>
                                            <c:if test="${isAdmin}">
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/products?action=edit&id=${product.id}"
                                                        class="btn btn-primary btn-sm">✏️ Modifier</a>
                                                    <form method="POST"
                                                        action="${pageContext.request.contextPath}/products"
                                                        style="display:inline"
                                                        onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer ce produit ?')">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="productId" value="${product.id}">
                                                        <button type="submit" class="btn btn-danger btn-sm">🗑️
                                                            Supprimer</button>
                                                    </form>
                                                </td>
                                            </c:if>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">
                                <h3>Aucun produit</h3>
                                <p>Le catalogue est vide.</p>
                                <c:if test="${isAdmin}">
                                    <a href="${pageContext.request.contextPath}/products?action=create"
                                        class="btn btn-success">
                                        ➕ Ajouter un produit
                                    </a>
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </body>

            </html>