<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!DOCTYPE html>
        <html lang="fr">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Erreur - LogMaster</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        </head>

        <body>
            <div class="container">
                <div class="error-page">
                    <h1>❌ Oups ! Une erreur est survenue</h1>

                    <c:choose>
                        <c:when test="${not empty error}">
                            <div class="error-message">
                                <p>${error}</p>
                            </div>
                        </c:when>
                        <c:when test="${not empty exception}">
                            <div class="error-message">
                                <p>${exception.message}</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="error-message">
                                <p>Une erreur inattendue s'est produite.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="error-actions">
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">
                            🏠 Retour au Dashboard
                        </a>
                        <a href="javascript:history.back()" class="btn btn-secondary">
                            ← Retour à la page précédente
                        </a>
                    </div>

                    <!-- Stack trace for development -->
                    <c:if test="${not empty exception}">
                        <details class="stack-trace">
                            <summary>Détails techniques (cliquez pour développer)</summary>
                            <pre><c:forEach var="trace" items="${exception.stackTrace}">${trace}
</c:forEach></pre>
                        </details>
                    </c:if>
                </div>
            </div>
        </body>

        </html>