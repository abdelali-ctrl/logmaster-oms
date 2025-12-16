<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Commande #${order.id} - LogMaster</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            </head>

            <body>
                <div class="container">
                    <div class="detail-header">
                        <h1>📦 Commande #${order.id}</h1>
                        <a href="${pageContext.request.contextPath}/orders?action=${isAdmin ? 'list' : 'myorders'}"
                            class="btn btn-secondary">
                            ← Retour à la liste
                        </a>
                    </div>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <div class="info-grid">
                        <div class="info-box">
                            <h3>👤 Client</h3>
                            <p>${order.user.name}</p>
                            <p class="text-muted">${order.user.email}</p>
                        </div>

                        <div class="info-box">
                            <h3>🛍️ Produit</h3>
                            <p>${order.product.name}</p>
                            <p class="text-muted">
                                <fmt:formatNumber value="${order.product.price}" type="currency" currencySymbol="€" /> ×
                                ${order.quantity}
                            </p>
                        </div>

                        <div class="info-box">
                            <h3>📅 Date de commande</h3>
                            <p>${order.orderDate}</p>
                        </div>

                        <div class="info-box">
                            <h3>💰 Montant Total</h3>
                            <p class="amount">
                                <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="€" />
                            </p>
                        </div>

                        <div class="info-box">
                            <h3>📊 Statut</h3>
                            <p>
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
                            </p>
                        </div>
                    </div>

                    <div class="info-box full-width">
                        <h3>📍 Adresse de livraison</h3>
                        <p>${order.shippingAddress}</p>
                    </div>

                    <c:if test="${not empty order.notes}">
                        <div class="info-box full-width warning-box">
                            <h3>📝 Notes</h3>
                            <p>${order.notes}</p>
                        </div>
                    </c:if>

                    <!-- Actions Section - ADMIN ONLY -->
                    <c:if test="${isAdmin && order.status != 'CANCELLED' && order.status != 'DELIVERED'}">
                        <div class="actions-section">
                            <h3>⚡ Actions disponibles (Admin)</h3>
                            <div class="action-buttons">
                                <c:if test="${order.status == 'PENDING'}">
                                    <form method="POST" action="${pageContext.request.contextPath}/orders"
                                        class="inline-form">
                                        <input type="hidden" name="action" value="updateStatus">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <input type="hidden" name="status" value="CONFIRMED">
                                        <button type="submit" class="btn btn-success">✅ Confirmer</button>
                                    </form>
                                </c:if>

                                <c:if test="${order.status == 'CONFIRMED'}">
                                    <form method="POST" action="${pageContext.request.contextPath}/orders"
                                        class="inline-form">
                                        <input type="hidden" name="action" value="updateStatus">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <input type="hidden" name="status" value="PROCESSING">
                                        <button type="submit" class="btn btn-success">⚙️ Traitement</button>
                                    </form>
                                </c:if>

                                <c:if test="${order.status == 'PROCESSING'}">
                                    <form method="POST" action="${pageContext.request.contextPath}/orders"
                                        class="inline-form">
                                        <input type="hidden" name="action" value="updateStatus">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <input type="hidden" name="status" value="SHIPPED">
                                        <button type="submit" class="btn btn-success">🚚 Expédier</button>
                                    </form>
                                </c:if>

                                <c:if test="${order.status == 'SHIPPED'}">
                                    <form method="POST" action="${pageContext.request.contextPath}/orders"
                                        class="inline-form">
                                        <input type="hidden" name="action" value="updateStatus">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <input type="hidden" name="status" value="DELIVERED">
                                        <button type="submit" class="btn btn-success">🎉 Livrer</button>
                                    </form>
                                </c:if>

                                <c:if test="${order.status != 'SHIPPED' && order.status != 'DELIVERED'}">
                                    <button onclick="cancelOrder()" class="btn btn-danger">❌ Annuler</button>
                                </c:if>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${isAdmin && order.status == 'CANCELLED'}">
                        <div class="actions-section danger-section">
                            <h3>🗑️ Zone de danger (Admin)</h3>
                            <form method="POST" action="${pageContext.request.contextPath}/orders"
                                onsubmit="return confirm('Êtes-vous sûr de vouloir supprimer définitivement cette commande ?')">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="orderId" value="${order.id}">
                                <button type="submit" class="btn btn-danger">🗑️ Supprimer définitivement</button>
                            </form>
                        </div>
                    </c:if>
                </div>

                <script>
                    function cancelOrder() {
                        const reason = prompt("Raison de l'annulation :");
                        if (reason !== null && reason.trim() !== "") {
                            const form = document.createElement('form');
                            form.method = 'POST';
                            form.action = '${pageContext.request.contextPath}/orders';
                            form.innerHTML = `
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <input type="hidden" name="reason" value="">
                `;
                            form.querySelector('[name="reason"]').value = reason;
                            document.body.appendChild(form);
                            form.submit();
                        }
                    }
                </script>
            </body>

            </html>