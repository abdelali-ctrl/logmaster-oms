<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>🛒 Panier - LogMaster</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                    rel="stylesheet">
            </head>

            <body>
                <div class="container">
                    <header>
                        <div class="header-top">
                            <h1>🛒 Mon Panier</h1>
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
                            <a href="${pageContext.request.contextPath}/orders?action=myorders">🛒 Mes Commandes</a>
                            <a href="${pageContext.request.contextPath}/orders?action=create" class="active">➕
                                Commander</a>
                            <a href="${pageContext.request.contextPath}/products?action=list">🛍️ Produits</a>
                            <a href="${pageContext.request.contextPath}/users?action=myaccount">⚙️ Mon Compte</a>
                        </nav>
                    </header>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <div class="form-container" style="max-width: 900px;">
                        <form method="POST" action="${pageContext.request.contextPath}/orders" id="cartForm">
                            <input type="hidden" name="action" value="createMulti">

                            <!-- Product Selection -->
                            <section class="section">
                                <h2>🛍️ Sélectionner des produits</h2>

                                <div id="cartItems" class="cart-items">
                                    <div class="empty-state" id="emptyCart">
                                        <h3>Panier vide</h3>
                                        <p>Ajoutez des produits depuis la liste ci-dessous</p>
                                    </div>
                                </div>

                                <div class="cart-total" id="cartTotal" style="display: none;">
                                    <span>Total:</span>
                                    <span class="cart-total-amount" id="totalAmount">0.00 €</span>
                                </div>
                            </section>

                            <!-- Available Products -->
                            <section class="section">
                                <h2>📦 Produits disponibles</h2>
                                <div
                                    style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 16px;">
                                    <c:forEach var="product" items="${products}">
                                        <div class="info-box" style="cursor: pointer; transition: all 0.3s;"
                                            onclick="addToCart(${product.id}, '${product.name}', ${product.price}, ${product.stock})"
                                            id="product-${product.id}">
                                            <h3>${product.category}</h3>
                                            <p style="font-size: 1.1rem; font-weight: 600; margin-bottom: 8px;">
                                                ${product.name}</p>
                                            <p class="amount" style="font-size: 1.3rem;">
                                                <fmt:formatNumber value="${product.price}" type="currency"
                                                    currencySymbol="€" />
                                            </p>
                                            <p class="text-muted">Stock: ${product.stock} unités</p>
                                            <button type="button" class="btn btn-success btn-sm"
                                                style="margin-top: 10px; width: 100%;">
                                                ➕ Ajouter
                                            </button>
                                        </div>
                                    </c:forEach>
                                </div>
                            </section>

                            <!-- Shipping Info -->
                            <section class="section">
                                <h2>📍 Livraison</h2>
                                <div class="order-form">
                                    <div class="form-group">
                                        <label for="shippingAddress">Adresse de livraison *</label>
                                        <textarea name="shippingAddress" id="shippingAddress" rows="3" required
                                            placeholder="Entrez votre adresse complète..."></textarea>
                                    </div>
                                    <div class="form-group">
                                        <label for="notes">Notes (optionnel)</label>
                                        <textarea name="notes" id="notes" rows="2"
                                            placeholder="Instructions spéciales..."></textarea>
                                    </div>
                                </div>
                            </section>

                            <div class="form-actions">
                                <a href="${pageContext.request.contextPath}/products?action=list"
                                    class="btn btn-secondary">
                                    Continuer les achats
                                </a>
                                <button type="submit" class="btn btn-success" id="submitBtn" disabled>
                                    ✅ Passer la commande
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <script>
                    let cart = {};

                    function addToCart(productId, name, price, stock) {
                        if (cart[productId]) {
                            if (cart[productId].quantity < stock) {
                                cart[productId].quantity++;
                            } else {
                                alert('Stock maximum atteint!');
                                return;
                            }
                        } else {
                            cart[productId] = {
                                id: productId,
                                name: name,
                                price: price,
                                stock: stock,
                                quantity: 1
                            };
                        }
                        renderCart();
                    }

                    function updateQuantity(productId, delta) {
                        if (!cart[productId]) return;

                        const newQty = cart[productId].quantity + delta;
                        if (newQty <= 0) {
                            delete cart[productId];
                        } else if (newQty <= cart[productId].stock) {
                            cart[productId].quantity = newQty;
                        } else {
                            alert('Stock insuffisant!');
                            return;
                        }
                        renderCart();
                    }

                    function removeFromCart(productId) {
                        delete cart[productId];
                        renderCart();
                    }

                    function renderCart() {
                        const container = document.getElementById('cartItems');
                        const cartTotal = document.getElementById('cartTotal');
                        const totalAmount = document.getElementById('totalAmount');
                        const submitBtn = document.getElementById('submitBtn');

                        const items = Object.values(cart);

                        if (items.length === 0) {
                            container.innerHTML = '<div class="empty-state"><h3>Panier vide</h3><p>Ajoutez des produits depuis la liste ci-dessous</p></div>';
                            cartTotal.style.display = 'none';
                            submitBtn.disabled = true;
                            return;
                        }

                        let html = '';
                        let total = 0;

                        for (let i = 0; i < items.length; i++) {
                            const item = items[i];
                            const subtotal = item.price * item.quantity;
                            total += subtotal;

                            html += '<div class="cart-item">';
                            html += '  <div class="cart-item-info">';
                            html += '    <span class="cart-item-name">' + item.name + '</span>';
                            html += '    <span class="cart-item-price">' + item.price.toFixed(2) + ' € / unité</span>';
                            html += '  </div>';
                            html += '  <div class="cart-item-quantity">';
                            html += '    <button type="button" class="btn btn-secondary btn-sm" onclick="updateQuantity(' + item.id + ', -1)">−</button>';
                            html += '    <input type="number" name="items[' + item.id + ']" value="' + item.quantity + '" readonly style="width: 60px; text-align: center;">';
                            html += '    <button type="button" class="btn btn-secondary btn-sm" onclick="updateQuantity(' + item.id + ', 1)">+</button>';
                            html += '    <span style="margin-left: 15px; font-weight: 600; min-width: 80px;">' + subtotal.toFixed(2) + ' €</span>';
                            html += '    <button type="button" class="btn btn-danger btn-sm" onclick="removeFromCart(' + item.id + ')" style="margin-left: 10px;">🗑️</button>';
                            html += '  </div>';
                            html += '</div>';
                        }

                        container.innerHTML = html;
                        cartTotal.style.display = 'flex';
                        totalAmount.textContent = total.toFixed(2) + ' €';
                        submitBtn.disabled = false;
                    }
                </script>
            </body>

            </html>