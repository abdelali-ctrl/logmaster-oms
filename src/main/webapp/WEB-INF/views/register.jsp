<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>

        <!DOCTYPE html>
        <html lang="fr">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Inscription - LogMaster</title>
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

                .login-box input {
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

                .login-link {
                    text-align: center;
                    margin-top: 20px;
                }

                .login-link a {
                    color: #667eea;
                    text-decoration: none;
                    font-weight: 600;
                }

                .login-link a:hover {
                    text-decoration: underline;
                }
            </style>
        </head>

        <body>
            <div class="login-container">
                <div class="login-box">
                    <h1>📝 Inscription</h1>
                    <p class="subtitle">Créez votre compte LogMaster</p>

                    <c:if test="${not empty error}">
                        <div class="alert alert-error">${error}</div>
                    </c:if>

                    <form method="POST" action="${pageContext.request.contextPath}/register">
                        <div class="form-group">
                            <label for="name">👤 Nom complet</label>
                            <input type="text" id="name" name="name" required value="${name}"
                                placeholder="Votre nom complet">
                        </div>

                        <div class="form-group">
                            <label for="email">📧 Email</label>
                            <input type="email" id="email" name="email" required value="${email}"
                                placeholder="votre@email.com">
                        </div>

                        <div class="form-group">
                            <label for="password">🔒 Mot de passe</label>
                            <input type="password" id="password" name="password" required
                                placeholder="Minimum 6 caractères">
                        </div>

                        <div class="form-group">
                            <label for="confirmPassword">🔒 Confirmer le mot de passe</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" required
                                placeholder="Répétez le mot de passe">
                        </div>

                        <button type="submit" class="btn-login">
                            Créer mon compte →
                        </button>
                    </form>

                    <div class="login-link">
                        <p>Déjà inscrit ? <a href="${pageContext.request.contextPath}/login">Se connecter</a></p>
                    </div>
                </div>
            </div>
        </body>

        </html>