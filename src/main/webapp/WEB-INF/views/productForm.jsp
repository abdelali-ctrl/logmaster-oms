<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!DOCTYPE html>
        <html lang="fr">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${editMode ? 'Modifier' : 'Nouveau'} Produit - LogMaster</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <div class="container">
                <header>
                    <h1>${editMode ? '✏️ Modifier' : '➕ Nouveau'} Produit</h1>
                    <nav class="nav-links">
                        <a href="${pageContext.request.contextPath}/dashboard">🏠 Dashboard</a>
                        <a href="${pageContext.request.contextPath}/products?action=list" class="active">🛍️
                            Produits</a>
                        <a href="${pageContext.request.contextPath}/users?action=list">👥 Utilisateurs</a>
                    </nav>
                </header>

                <c:if test="${not empty error}">
                    <div class="alert alert-error">${error}</div>
                </c:if>

                <div class="form-container">
                    <form method="POST" action="${pageContext.request.contextPath}/products" class="order-form">
                        <input type="hidden" name="action" value="${editMode ? 'update' : 'create'}">
                        <c:if test="${editMode}">
                            <input type="hidden" name="productId" value="${product.id}">
                        </c:if>

                        <div class="form-group">
                            <label for="name">🏷️ Nom du produit *</label>
                            <input type="text" name="name" id="name" required value="${editMode ? product.name : ''}"
                                placeholder="Ex: iPhone 15 Pro">
                        </div>

                        <div class="form-group">
                            <label for="price">💰 Prix (€) *</label>
                            <input type="number" name="price" id="price" step="0.01" min="0" required
                                value="${editMode ? product.price : ''}" placeholder="0.00">
                        </div>

                        <div class="form-group">
                            <label for="stock">📦 Stock *</label>
                            <input type="number" name="stock" id="stock" min="0" required
                                value="${editMode ? product.stock : ''}" placeholder="0">
                        </div>

                        <div class="form-group">
                            <label for="category">📂 Catégorie *</label>
                            <input type="text" name="category" id="category" required
                                value="${editMode ? product.category : ''}" placeholder="Ex: Électronique"
                                list="categories">
                            <datalist id="categories">
                                <option value="Électronique">
                                <option value="Vêtements">
                                <option value="Maison">
                                <option value="Sport">
                                <option value="Alimentation">
                            </datalist>
                        </div>

                        <div class="form-actions">
                            <a href="${pageContext.request.contextPath}/products?action=list" class="btn btn-secondary">
                                Annuler
                            </a>
                            <button type="submit" class="btn btn-success">
                                ${editMode ? '💾 Enregistrer' : '✅ Créer'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </body>

        </html>