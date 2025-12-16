<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!DOCTYPE html>
        <html lang="fr">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${isMyAccount ? 'Mon Compte' : (editMode ? 'Modifier' : 'Nouvel')} Utilisateur - LogMaster</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <div class="container">
                <header>
                    <div class="header-top">
                        <h1>${isMyAccount ? '⚙️ Mon Compte' : (editMode ? '✏️ Modifier' : '➕ Nouvel')} ${isMyAccount ?
                            '' : 'Utilisateur'}</h1>
                        <div class="user-info">
                            <span class="user-name">👤 ${sessionScope.userName}</span>
                            <span class="user-role ${sessionScope.userRole == 'ADMIN' ? 'role-admin' : 'role-user'}">
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
                        <a href="${pageContext.request.contextPath}/products?action=list">🛍️ Produits</a>
                        <c:if test="${sessionScope.userRole == 'ADMIN'}">
                            <a href="${pageContext.request.contextPath}/users?action=list">👥 Utilisateurs</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/users?action=myaccount"
                            class="${isMyAccount ? 'active' : ''}">⚙️ Mon Compte</a>
                    </nav>
                </header>

                <c:if test="${param.success == 'true'}">
                    <div class="alert alert-success">✅ Compte mis à jour avec succès!</div>
                </c:if>

                <c:if test="${not empty error}">
                    <div class="alert alert-error">${error}</div>
                </c:if>

                <div class="form-container">
                    <form method="POST" action="${pageContext.request.contextPath}/users" class="order-form">
                        <input type="hidden" name="action" value="${editMode ? 'update' : 'create'}">
                        <c:if test="${editMode}">
                            <input type="hidden" name="userId" value="${user.id}">
                        </c:if>

                        <div class="form-group">
                            <label for="name">👤 Nom complet *</label>
                            <input type="text" name="name" id="name" required value="${editMode ? user.name : ''}"
                                placeholder="Ex: Jean Dupont">
                        </div>

                        <div class="form-group">
                            <label for="email">📧 Email *</label>
                            <input type="email" name="email" id="email" required value="${editMode ? user.email : ''}"
                                placeholder="Ex: jean@example.com">
                        </div>

                        <div class="form-group">
                            <label for="password">🔒 Mot de passe ${editMode ? '(laisser vide pour ne pas changer)' :
                                '*'}</label>
                            <input type="password" name="password" id="password" ${editMode ? '' : 'required' }
                                placeholder="••••••••">
                        </div>

                        <c:if test="${isAdmin && !isMyAccount}">
                            <div class="form-group">
                                <label for="role">🎭 Rôle</label>
                                <select name="role" id="role">
                                    <option value="USER" ${editMode && user.role=='USER' ? 'selected' : '' }>Utilisateur
                                    </option>
                                    <option value="ADMIN" ${editMode && user.role=='ADMIN' ? 'selected' : '' }>
                                        Administrateur</option>
                                </select>
                            </div>
                        </c:if>

                        <div class="form-actions">
                            <c:choose>
                                <c:when test="${isMyAccount}">
                                    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">
                                        Retour
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/users?action=list"
                                        class="btn btn-secondary">
                                        Annuler
                                    </a>
                                </c:otherwise>
                            </c:choose>
                            <button type="submit" class="btn btn-success">
                                ${editMode ? '💾 Enregistrer' : '✅ Créer'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </body>

        </html>