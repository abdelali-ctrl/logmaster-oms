<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${isMyOrders ? 'Mes Commandes' : 'Commandes'} - LogMaster</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            </head>

            <body>
                <div class="container">
                    <header>
                        <div class="header-top">
                            <h1>📦 ${isMyOrders ? 'Mes Commandes' : 'Gestion des Commandes'}</h1>
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
                                <a href="${pageContext.request.contextPath}/orders?action=list" class="active">📦 Toutes
                                    les Commandes</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/orders?action=myorders"
                                class="${isMyOrders ? 'active' : ''}">🛒 Mes Commandes</a>
                            <a href="${pageContext.request.contextPath}/products?action=list">🛍️ Produits</a>
                            <c:if test="${sessionScope.userRole == 'ADMIN'}">
                                <a href="${pageContext.request.contextPath}/users?action=list">👥 Utilisateurs</a>
                                <a href="${pageContext.request.contextPath}/logs?action=list">📋 Logs</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/users?action=myaccount">⚙️ Mon Compte</a>
                        </nav>
                    </header>

                    <div class="actions-bar">
                        <div class="filter-form">
                            <c:if test="${isAdmin && !isMyOrders}">
                                <label for="status">Filtrer par statut:</label>
                                <select id="status" onchange="filterByStatus(this.value)">
                                    <option value="">Tous</option>
                                    <c:forEach var="status" items="${statuses}">
                                        <option value="${status}" ${currentStatus==status ? 'selected' : '' }>${status}
                                        </option>
                                    </c:forEach>
                                </select>
                            </c:if>
                        </div>
                        <a href="${pageContext.request.contextPath}/orders?action=create" class="btn btn-success">
                            ➕ Nouvelle Commande
                        </a>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <c:choose>
                        <c:when test="${not empty orders}">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <c:if test="${isAdmin && !isMyOrders}">
                                            <th>Client</th>
                                        </c:if>
                                        <th>Produit</th>
                                        <th>Qté</th>
                                        <th>Total</th>
                                        <th>Date</th>
                                        <th>Statut</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="order" items="${orders}">
                                        <tr>
                                            <td>#${order.id}</td>
                                            <c:if test="${isAdmin && !isMyOrders}">
                                                <td>
                                                    <strong>${order.user.name}</strong><br>
                                                    <small>${order.user.email}</small>
                                                </td>
                                            </c:if>
                                            <td>${order.product.name}</td>
                                            <td>${order.quantity}</td>
                                            <td>
                                                <fmt:formatNumber value="${order.totalAmount}" type="currency"
                                                    currencySymbol="€" />
                                            </td>
                                            <td>${order.orderDate}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${order.status == 'PENDING'}">
                                                        <span class="status status-pending">⏳ En attente</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'CONFIRMED'}">
                                                        <span class="status status-confirmed">✅ Confirmée</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'PROCESSING'}">
                                                        <span class="status status-processing">⚙️ En cours</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'SHIPPED'}">
                                                        <span class="status status-shipped">🚚 Expédiée</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'DELIVERED'}">
                                                        <span class="status status-delivered">🎉 Livrée</span>
                                                    </c:when>
                                                    <c:when test="${order.status == 'CANCELLED'}">
                                                        <span class="status status-cancelled">❌ Annulée</span>
                                                    </c:when>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/orders?action=detail&id=${order.id}"
                                                    class="btn btn-primary btn-sm">Voir</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">
                                <h3>Aucune commande</h3>
                                <p>${isMyOrders ? 'Vous n\'avez pas encore de commandes.' : 'Il n\'y a pas encore de
                                    commandes.'}</p>
                                <a href="${pageContext.request.contextPath}/orders?action=create"
                                    class="btn btn-success">
                                    ➕ Créer une commande
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <script>
                    function filterByStatus(status) {
                        if (status) {
                            window.location.href = '${pageContext.request.contextPath}/orders?action=list&status=' + status;
                        } else {
                            window.location.href = '${pageContext.request.contextPath}/orders?action=list';
                        }
                    }
                </script>
            </body>

            </html>