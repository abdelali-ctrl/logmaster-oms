<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="fr">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>LogMaster Dashboard</title>
                <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                    rel="stylesheet">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
            </head>

            <body>
                <div class="container">
                    <header>
                        <div class="header-top">
                            <h1>🚀 LogMaster Dashboard</h1>
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
                            <a href="${pageContext.request.contextPath}/dashboard" class="active">🏠 Dashboard</a>
                            <c:if test="${sessionScope.userRole == 'ADMIN'}">
                                <a href="${pageContext.request.contextPath}/orders?action=list">📦 Toutes les
                                    Commandes</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/orders?action=myorders">🛒 Mes Commandes</a>
                            <a href="${pageContext.request.contextPath}/orders?action=create">➕ Commander</a>
                            <a href="${pageContext.request.contextPath}/products?action=list">🛍️ Produits</a>
                            <c:if test="${sessionScope.userRole == 'ADMIN'}">
                                <a href="${pageContext.request.contextPath}/users?action=list">👥 Utilisateurs</a>
                                <a href="${pageContext.request.contextPath}/logs?action=list">📋 Logs</a>
                            </c:if>
                            <a href="${pageContext.request.contextPath}/users?action=myaccount">⚙️ Mon Compte</a>
                        </nav>
                    </header>

                    <!-- ORDER STATISTICS (PostgreSQL) -->
                    <section class="section">
                        <h2>📊 Statistiques des Commandes (PostgreSQL)</h2>
                        <div class="stats-grid">
                            <div class="stat-card">
                                <h3>${stats.totalOrders}</h3>
                                <p>Total Commandes</p>
                            </div>
                            <div class="stat-card pending">
                                <h3>${stats.pendingOrders}</h3>
                                <p>⏳ En Attente</p>
                            </div>
                            <div class="stat-card confirmed">
                                <h3>${stats.confirmedOrders}</h3>
                                <p>✅ Confirmées</p>
                            </div>
                            <div class="stat-card processing">
                                <h3>${stats.processingOrders}</h3>
                                <p>⚙️ En Cours</p>
                            </div>
                            <div class="stat-card shipped">
                                <h3>${stats.shippedOrders}</h3>
                                <p>🚚 Expédiées</p>
                            </div>
                            <div class="stat-card delivered">
                                <h3>${stats.deliveredOrders}</h3>
                                <p>🎉 Livrées</p>
                            </div>
                            <div class="stat-card cancelled">
                                <h3>${stats.cancelledOrders}</h3>
                                <p>❌ Annulées</p>
                            </div>
                        </div>
                        <div class="stats-grid rates">
                            <div class="stat-card success">
                                <h3>
                                    <fmt:formatNumber value="${stats.successRate}" maxFractionDigits="1" />%
                                </h3>
                                <p>Taux de Succès</p>
                            </div>
                            <div class="stat-card warning">
                                <h3>
                                    <fmt:formatNumber value="${stats.cancellationRate}" maxFractionDigits="1" />%
                                </h3>
                                <p>Taux d'Annulation</p>
                            </div>
                        </div>
                    </section>

                    <!-- LOG STATISTICS (MongoDB) - ADMIN ONLY -->
                    <c:if test="${sessionScope.userRole == 'ADMIN'}">
                        <section class="section">
                            <h2>📈 Statistiques des Logs (MongoDB)</h2>
                            <div class="stats-grid">
                                <div class="stat-card error">
                                    <h3>${stats.errorCount}</h3>
                                    <p>❌ Erreurs</p>
                                </div>
                                <div class="stat-card warning">
                                    <h3>${stats.warningCount}</h3>
                                    <p>⚠️ Avertissements</p>
                                </div>
                            </div>
                        </section>

                        <!-- 24H ERROR CHART (MongoDB Aggregation - BONUS) - ADMIN ONLY -->
                        <section class="section">
                            <h2>📉 Graphique des Erreurs sur 24h (MongoDB Aggregation)</h2>
                            <div class="chart-container"
                                style="position: relative; height: 300px; width: 100%; background: #f8f9fa; border-radius: 10px; padding: 20px;">
                                <canvas id="errorChart"></canvas>
                            </div>
                        </section>

                        <!-- TOP ACTIVE USERS (MongoDB Aggregation) - ADMIN ONLY -->
                        <section class="section">
                            <h2>👥 Top 5 Utilisateurs Actifs (MongoDB Aggregation)</h2>
                            <c:choose>
                                <c:when test="${not empty stats.topActiveUsers}">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Position</th>
                                                <th>Utilisateur</th>
                                                <th>Nombre de Commandes</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="user" items="${stats.topActiveUsers}" varStatus="status">
                                                <tr>
                                                    <td><strong>#${status.index + 1}</strong></td>
                                                    <td>${user.get("_id")}</td>
                                                    <td>${user.get("order_count")} commandes</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state">Aucune donnée disponible</div>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <!-- EVENT STATISTICS (MongoDB Aggregation) -->
                        <section class="section">
                            <h2>📊 Répartition des Événements (MongoDB Aggregation)</h2>
                            <c:choose>
                                <c:when test="${not empty stats.eventStatistics}">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Type d'Événement</th>
                                                <th>Nombre</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="event" items="${stats.eventStatistics}">
                                                <tr>
                                                    <td>${event.get("_id")}</td>
                                                    <td><strong>${event.get("count")}</strong></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state">Aucune donnée disponible</div>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <!-- RECENT LOGS -->
                        <section class="section">
                            <h2>📋 Logs Récents</h2>
                            <c:choose>
                                <c:when test="${not empty stats.recentLogs}">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Timestamp</th>
                                                <th>Niveau</th>
                                                <th>Service</th>
                                                <th>Message</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="log" items="${stats.recentLogs}">
                                                <tr>
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
                                                    <td>${log.get("service")}</td>
                                                    <td>${log.get("message")}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                    <div class="text-center" style="margin-top: 20px;">
                                        <a href="${pageContext.request.contextPath}/logs?action=list"
                                            class="btn btn-primary">
                                            Voir tous les logs →
                                        </a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state">Aucun log disponible</div>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </c:if>
                    <!-- END ADMIN-ONLY LOG SECTIONS -->
                </div>

                <!-- Chart.js Script for 24h Error Chart -->
                <script>
                    // Prepare data from server
                    const hourlyData = [];
                    <c:if test="${not empty stats.hourlyErrorData}">
                        <c:forEach var="item" items="${stats.hourlyErrorData}">
                            hourlyData.push({
                                hour: ${item.get("_id").get("hour")},
                            level: '${item.get("_id").get("level")}',
                            count: ${item.get("count")}
                });
                        </c:forEach>
                    </c:if>

                    // Process data for chart (24 hours)
                    const hours = Array.from({ length: 24 }, (_, i) => i + 'h');
                    const errorData = new Array(24).fill(0);
                    const warningData = new Array(24).fill(0);

                    hourlyData.forEach(item => {
                        if (item.level === 'ERROR') {
                            errorData[item.hour] = item.count;
                        } else if (item.level === 'WARNING') {
                            warningData[item.hour] = item.count;
                        }
                    });

                    // Create Chart
                    const ctx = document.getElementById('errorChart').getContext('2d');
                    new Chart(ctx, {
                        type: 'bar',
                        data: {
                            labels: hours,
                            datasets: [
                                {
                                    label: 'Erreurs',
                                    data: errorData,
                                    backgroundColor: 'rgba(220, 53, 69, 0.8)',
                                    borderColor: 'rgba(220, 53, 69, 1)',
                                    borderWidth: 1
                                },
                                {
                                    label: 'Avertissements',
                                    data: warningData,
                                    backgroundColor: 'rgba(255, 193, 7, 0.8)',
                                    borderColor: 'rgba(255, 193, 7, 1)',
                                    borderWidth: 1
                                }
                            ]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                                title: {
                                    display: true,
                                    text: 'Distribution des Erreurs et Avertissements par Heure (24h)',
                                    font: { size: 16 }
                                },
                                legend: {
                                    position: 'top'
                                }
                            },
                            scales: {
                                x: {
                                    title: {
                                        display: true,
                                        text: 'Heure'
                                    }
                                },
                                y: {
                                    beginAtZero: true,
                                    title: {
                                        display: true,
                                        text: 'Nombre'
                                    },
                                    ticks: {
                                        stepSize: 1
                                    }
                                }
                            }
                        }
                    });
                </script>
            </body>

            </html>