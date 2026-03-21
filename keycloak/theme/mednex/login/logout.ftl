<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <!-- Re-using existing styles from login.css plus custom MedNex Logout overrides -->
        <style>
            .login-card {
                background: rgba(30, 41, 59, 0.6) !important;
                backdrop-filter: blur(20px);
                border: 1px solid rgba(255, 255, 255, 0.08) !important;
                border-radius: 32px !important;
                padding: 48px !important;
                text-align: center;
                max-width: 480px;
                margin: 0 auto;
            }
            .success-icon {
                width: 72px;
                height: 72px;
                border-radius: 50%;
                background: rgba(59, 130, 246, 0.1) !important;
                display: flex !important;
                align-items: center;
                justify-content: center;
                margin: 0 auto 24px !important;
                border: 2px solid #3B82F6 !important;
                box-shadow: 0 0 20px rgba(59, 130, 246, 0.3) !important;
            }
            .success-icon span { font-size: 36px; color: #3B82F6; }
            .logout-message {
                background: #111827;
                border: 1px solid rgba(255, 255, 255, 0.05);
                border-radius: 16px;
                padding: 24px;
                margin-bottom: 32px;
            }
            .highlight-text { font-size: 16px; font-weight: 600; color: #E2E8F0; margin-bottom: 8px; }
            .sub-text { font-size: 14px; color: #94A3B8; margin: 0; }
            .return-btn {
                width: 100%;
                height: 56px;
                background: linear-gradient(135deg, #3B82F6, #6366F1);
                border: none;
                border-radius: 12px;
                color: white !important;
                font-weight: 700;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 12px;
                text-decoration: none;
            }
        </style>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@48,400,0,0" />
    <#elseif section = "form">
        <div class="login-card">
            <div class="success-icon">
                <span class="material-symbols-outlined">verified_user</span>
            </div>
            <h2 style="color: white; font-size: 28px; font-weight: 800; margin-bottom: 24px;">Secure Logout Complete</h2>
            
            <div class="logout-message">
                <p class="highlight-text">You've been securely logged out of MedNex Enterprise.</p>
                <p class="sub-text">For your security, please close your browser tab.</p>
            </div>
            
            <a href="${url.loginUrl}" class="return-btn">
                <span class="material-symbols-outlined">arrow_back</span>
                Back to Login
            </a>
        </div>
    </#if>
</@layout.registrationLayout>
