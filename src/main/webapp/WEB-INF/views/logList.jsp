<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Logs - LogMaster</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            </head>

            <body>
                <div class="container">
                    <header>
                        <h1>📋 Logs Applicatifs</h1>
                        <nav class="nav-links">
                            <a href="${pageContext.request.contextPath}/dashboard">🏠 Dashboard</a>
                            <a href="${pageContext.request.contextPath}/orders?action=list">📦 Commandes</a>
                            <a href="${pageContext.request.contextPath}/logs?action=list" class="active">📋 Logs</a>
                        </nav>
                    </header>

                    <!-- Filter Bar -->
                    <div class="filter-bar">
                        <form method="GET" action="${pageContext.request.contextPath}/logs" class="filter-form">
                            <input type="hidden" name="action" value="filter">

                            <div class="filter-group">
                                <label for="level">Niveau :</label>
                                <select name="level" id="level">
                                    <option value="">Tous</option>
                                    <option value="INFO" ${filterValue=='INFO' ? 'selected' : '' }>INFO</option>
                                    <option value="WARNING" ${filterValue=='WARNING' ? 'selected' : '' }>WARNING
                                    </option>
                                    <option value="ERROR" ${filterValue=='ERROR' ? 'selected' : '' }>ERROR</option>
                                </select>
                            </div>

                            <div class="filter-group">
                                <label for="service">Service :</label>
                                <select name="service" id="service">
                                    <option value="">Tous</option>
                                    <option value="order-service" ${filterValue=='order-service' ? 'selected' : '' }>
                                        order-service</option>
                                    <option value="auth-service" ${filterValue=='auth-service' ? 'selected' : '' }>
                                        auth-service</option>
                                </select>
                            </div>

                            <div class="filter-group">
                                <label for="limit">Limite :</label>
                                <select name="limit" id="limit">
                                    <option value="25" ${currentLimit==25 ? 'selected' : '' }>25</option>
                                    <option value="50" ${currentLimit==50 ? 'selected' : '' }>50</option>
                                    <option value="100" ${currentLimit==100 ? 'selected' : '' }>100</option>
                                </select>
                            </div>

                            <button type="submit" class="btn btn-primary">🔍 Filtrer</button>
                            <a href="${pageContext.request.contextPath}/logs?action=list" class="btn btn-secondary">↻
                                Réinitialiser</a>
                        </form>
                    </div>

                    <!-- Current Filter -->
                    <c:if test="${not empty filterType}">
                        <div class="alert alert-info">
                            Filtré par ${filterType}: <strong>${filterValue}</strong>
                        </div>
                    </c:if>

                    <!-- Logs Table -->
                    <c:choose>
                        <c:when test="${not empty logs}">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Timestamp</th>
                                        <th>Niveau</th>
                                        <th>Service</th>
                                        <th>Type</th>
                                        <th>Message</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="log" items="${logs}">
                                        <tr class="log-row log-${log.get('level').toString().toLowerCase()}">
                                            <td>
                                                <fmt:formatDate value="${log.get('timestamp')}"
                                                    pattern="dd/MM/yyyy HH:mm:ss" />
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${log.get('level') == 'ERROR'}">
                                                        <span class="log-level log-error">ERROR</span>
                                                    </c:when>
                                                    <c:when test="${log.get('level') == 'WARNING'}">
                                                        <span class="log-level log-warning">WARNING</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="log-level log-info">INFO</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><code>${log.get("service")}</code></td>
                                            <td><code>${log.get("event_type")}</code></td>
                                            <td>${log.get("message")}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>

                            <div class="pagination-info">
                                Affichage de ${logs.size()} logs
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">
                                <h3>Aucun log trouvé</h3>
                                <p>Les logs apparaîtront ici lorsque des actions seront effectuées.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </body>

            </html>