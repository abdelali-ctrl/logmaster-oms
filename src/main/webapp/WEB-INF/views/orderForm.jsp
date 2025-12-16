<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nouvelle Commande - LogMaster</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            </head>

            <body>
                <div class="container">
                    <header>
                        <div class="header-top">
                            <h1>➕ Nouvelle Commande</h1>
                            <div class="user-info">
                                <span class="user-name">👤 ${sessionScope.userName}</span>
                                <span
                                    class="user-role ${sessionScope.userRole == 'ADMIN' ? 'role-admin' : 'role-user'}">
                                    ${sessionScope.userRole}
                                </span>
                            </div>
                        </div>
                        <nav class="nav-links">
                            <a href="${pageContext.request.contextPath}/dashboard">🏠 Dashboard</a>
                            <a href="${pageContext.request.contextPath}/orders?action=myorders" class="active">🛒 Mes
                                Commandes</a>
                            <a href="${pageContext.request.contextPath}/products?action=list">🛍️ Produits</a>
                            <a href="${pageContext.request.contextPath}/users?action=myaccount">⚙️ Mon Compte</a>
                        </nav>
                    </header>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <div class="form-container">
                        <form method="POST" action="${pageContext.request.contextPath}/orders" class="order-form"
                            id="orderForm">
                            <input type="hidden" name="action" value="create">

                            <c:choose>
                                <c:when test="${isAdmin && not empty users}">
                                    <div class="form-group">
                                        <label for="userId">👤 Client *</label>
                                        <select name="userId" id="userId" required>
                                            <option value="">Sélectionner un client</option>
                                            <c:forEach var="user" items="${users}">
                                                <option value="${user.id}">${user.name} (${user.email})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="form-group">
                                        <label>👤 Client</label>
                                        <div class="info-box" style="margin: 0; padding: 12px;">
                                            <strong>${sessionScope.userName}</strong>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>

                            <div class="form-group">
                                <label for="productId">🛍️ Produit *</label>
                                <c:choose>
                                    <c:when test="${not empty products}">
                                        <select name="productId" id="productId" required onchange="updatePrice()">
                                            <option value="" data-price="0" data-stock="0">Sélectionner un produit
                                            </option>
                                            <c:forEach var="product" items="${products}">
                                                <option value="${product.id}" data-price="${product.price}"
                                                    data-stock="${product.stock}">
                                                    ${product.name} -
                                                    <fmt:formatNumber value="${product.price}" type="currency"
                                                        currencySymbol="€" />
                                                    (Stock: ${product.stock})
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-warning">
                                            Aucun produit en stock.
                                            <c:if test="${isAdmin}">
                                                <a href="${pageContext.request.contextPath}/products?action=create">Ajouter
                                                    un produit</a>
                                            </c:if>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="form-group">
                                <label for="quantity">📦 Quantité *</label>
                                <input type="number" name="quantity" id="quantity" min="1" required value="1"
                                    onchange="updatePrice()">
                                <small id="stockInfo" style="color: #666;"></small>
                            </div>

                            <div class="form-group">
                                <label>💰 Total Estimé</label>
                                <div class="info-box" style="margin: 0;">
                                    <p class="amount" id="totalAmount">0.00 €</p>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="shippingAddress">📍 Adresse de livraison *</label>
                                <textarea name="shippingAddress" id="shippingAddress" rows="3" required
                                    placeholder="Entrez l'adresse complète..."></textarea>
                            </div>

                            <div class="form-group">
                                <label for="notes">📝 Notes (optionnel)</label>
                                <textarea name="notes" id="notes" rows="2"
                                    placeholder="Instructions spéciales, commentaires..."></textarea>
                            </div>

                            <div class="form-actions">
                                <a href="${pageContext.request.contextPath}/orders?action=myorders"
                                    class="btn btn-secondary">
                                    Annuler
                                </a>
                                <button type="submit" class="btn btn-success" id="submitBtn">
                                    ✅ Créer la commande
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <script>
                    function updatePrice() {
                        const productSelect = document.getElementById('productId');
                        const quantityInput = document.getElementById('quantity');
                        const totalDisplay = document.getElementById('totalAmount');
                        const stockInfo = document.getElementById('stockInfo');
                        const submitBtn = document.getElementById('submitBtn');

                        const selectedOption = productSelect.options[productSelect.selectedIndex];
                        const price = parseFloat(selectedOption.dataset.price) || 0;
                        const stock = parseInt(selectedOption.dataset.stock) || 0;
                        const quantity = parseInt(quantityInput.value) || 0;

                        quantityInput.max = stock;

                        if (stock > 0) {
                            stockInfo.textContent = 'Stock disponible: ' + stock + ' unités';
                            stockInfo.style.color = quantity > stock ? '#dc3545' : '#666';
                        } else {
                            stockInfo.textContent = '';
                        }

                        const total = price * quantity;
                        totalDisplay.textContent = total.toFixed(2) + ' €';

                        if (quantity > stock && stock > 0) {
                            stockInfo.textContent = '⚠️ Stock insuffisant! Maximum: ' + stock;
                            stockInfo.style.color = '#dc3545';
                            submitBtn.disabled = true;
                        } else {
                            submitBtn.disabled = false;
                        }
                    }

                    document.addEventListener('DOMContentLoaded', updatePrice);
                </script>
            </body>

            </html>