<%@ taglib prefix="c" uri="jakarta.tags.core" %>

    <%-- Common Header - Include this in all pages --%>
        <header>
            <div class="header-top">
                <h1>${param.pageTitle}</h1>
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
                <a href="${pageContext.request.contextPath}/dashboard"
                    class="${param.activePage == 'dashboard' ? 'active' : ''}">🏠 Dashboard</a>
                <c:if test="${sessionScope.userRole == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/orders?action=list"
                        class="${param.activePage == 'allorders' ? 'active' : ''}">📦 Toutes les Commandes</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/orders?action=myorders"
                    class="${param.activePage == 'myorders' ? 'active' : ''}">🛒 Mes Commandes</a>
                <a href="${pageContext.request.contextPath}/orders?action=create"
                    class="${param.activePage == 'neworder' ? 'active' : ''}">➕ Commander</a>
                <a href="${pageContext.request.contextPath}/products?action=list"
                    class="${param.activePage == 'products' ? 'active' : ''}">🛍️ Produits</a>
                <c:if test="${sessionScope.userRole == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/users?action=list"
                        class="${param.activePage == 'users' ? 'active' : ''}">👥 Utilisateurs</a>
                    <a href="${pageContext.request.contextPath}/logs?action=list"
                        class="${param.activePage == 'logs' ? 'active' : ''}">📋 Logs</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/users?action=myaccount"
                    class="${param.activePage == 'myaccount' ? 'active' : ''}">⚙️ Mon Compte</a>
            </nav>
        </header>