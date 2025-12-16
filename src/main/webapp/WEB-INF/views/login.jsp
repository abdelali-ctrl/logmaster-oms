<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!DOCTYPE html>
        <html lang="fr">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Connexion - LogMaster</title>
            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
            <style>
                .login-container {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                }

                .login-box {
                    background: white;
                    padding: 40px;
                    border-radius: 16px;
                    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                    width: 100%;
                    max-width: 400px;
                }

                .login-box h1 {
                    text-align: center;
                    margin-bottom: 10px;
                    color: #333;
                }

                .login-box .subtitle {
                    text-align: center;
                    color: #666;
                    margin-bottom: 30px;
                }

                .login-box .form-group {
                    margin-bottom: 20px;
                }

                .login-box label {
                    display: block;
                    margin-bottom: 8px;
                    font-weight: 600;
                    color: #555;
                }

                .login-box input[type="email"],
                .login-box input[type="password"] {
                    width: 100%;
                    padding: 12px 16px;
                    border: 2px solid #e0e0e0;
                    border-radius: 8px;
                    font-size: 16px;
                    transition: border-color 0.3s;
                    box-sizing: border-box;
                }

                .login-box input:focus {
                    outline: none;
                    border-color: #667eea;
                }

                .login-box .btn-login {
                    width: 100%;
                    padding: 14px;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    border: none;
                    border-radius: 8px;
                    font-size: 16px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: transform 0.2s, box-shadow 0.2s;
                }

                .login-box .btn-login:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
                }

                .alert {
                    padding: 12px 16px;
                    border-radius: 8px;
                    margin-bottom: 20px;
                }

                .alert-error {
                    background: #fee;
                    color: #c00;
                    border: 1px solid #fcc;
                }

                .alert-success {
                    background: #efe;
                    color: #060;
                    border: 1px solid #cfc;
                }

                .demo-credentials {
                    margin-top: 20px;
                    padding: 15px;
                    background: #f5f5f5;
                    border-radius: 8px;
                    font-size: 14px;
                }

                .demo-credentials h4 {
                    margin: 0 0 10px 0;
                    color: #666;
                }

                .demo-credentials code {
                    background: #e0e0e0;
                    padding: 2px 6px;
                    border-radius: 4px;
                }
            </style>
        </head>

        <body>
            <div class="login-container">
                <div class="login-box">
                    <h1>🚀 LogMaster</h1>
                    <p class="subtitle">Dashboard d'Analyse de Logs</p>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <c:if test="${not empty success}">
                        <div class="alert alert-success">${success}</div>
                    </c:if>

                    <c:if test="${param.registered == 'true'}">
                        <div class="alert alert-success">✅ Inscription réussie! Vous pouvez maintenant vous connecter.
                        </div>
                    </c:if>

                    <form method="POST" action="${pageContext.request.contextPath}/login">
                        <div class="form-group">
                            <label for="email">📧 Email</label>
                            <input type="email" id="email" name="email" required placeholder="votre@email.com"
                                autofocus>
                        </div>

                        <div class="form-group">
                            <label for="password">🔒 Mot de passe</label>
                            <input type="password" id="password" name="password" required placeholder="••••••••">
                        </div>

                        <button type="submit" class="btn-login">
                            Se connecter →
                        </button>
                    </form>

                    <div style="text-align: center; margin-top: 20px;">
                        <p style="color: #666;">Pas encore de compte ?
                            <a href="${pageContext.request.contextPath}/register"
                                style="color: #667eea; font-weight: 600; text-decoration: none;">
                                Inscrivez-vous
                            </a>
                        </p>
                    </div>

                    <div class="demo-credentials">
                        <h4>🔑 Identifiants de test:</h4>
                        <p><strong>Admin:</strong> <code>admin@example.com</code> / <code>admin123</code></p>
                        <p><strong>User:</strong> <code>jean@example.com</code> / <code>password123</code></p>
                    </div>
                </div>
            </div>
        </body>

        </html>