<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password'); section>

    <#if section = "header">
        <div class="myapp-brand">
            <div class="myapp-brand__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/>
                    <path d="m9 12 2 2 4-4"/>
                </svg>
            </div>
            <h1 class="myapp-title">${msg("loginAccountTitle")}</h1>
            <p class="myapp-subtitle">${msg("loginAccountSubtitle")}</p>
        </div>

    <#elseif section = "form">

        <#-- Блок ошибки авторизации (неверный логин/пароль) -->
        <#if messagesPerField.existsError('username','password')>
            <div class="myapp-alert myapp-alert--error" id="myapp-login-error">
                <div class="myapp-alert__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                </div>
                <div class="myapp-alert__body">
                    <div class="myapp-alert__title">${msg("loginErrorTitle")}</div>
                    <div class="myapp-alert__desc">${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</div>
                </div>
            </div>
        </#if>

        <form id="kc-form-login" onsubmit="myappSubmit(this); return true;" action="${url.loginAction}" method="post">

            <#-- Логин / email -->
            <#if !usernameHidden??>
                <div class="myapp-field">
                    <label for="username" class="myapp-label">
                        <#if !realm.loginWithEmailAllowed>${msg("username")}
                        <#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}
                        <#else>${msg("email")}
                        </#if>
                    </label>
                    <div class="myapp-input-wrap">
                        <svg class="myapp-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="2" y="4" width="20" height="16" rx="2"/>
                            <path d="m22 6-10 7L2 6"/>
                        </svg>
                        <input tabindex="1" id="username" name="username" type="text"
                               class="myapp-input"
                               value="${(login.username!'')}"
                               autofocus autocomplete="off"
                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" />
                    </div>
                </div>
            </#if>

            <#-- Пароль -->
            <div class="myapp-field">
                <div class="myapp-label-row">
                    <label for="password" class="myapp-label">${msg("password")}</label>
                    <#if realm.resetPasswordAllowed>
                        <a tabindex="5" href="${url.loginResetCredentialsUrl}" class="myapp-link-small">${msg("doForgotPassword")}</a>
                    </#if>
                </div>
                <div class="myapp-input-wrap">
                    <svg class="myapp-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="11" rx="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                    <input tabindex="2" id="password" name="password" type="password"
                           class="myapp-input myapp-input--password"
                           autocomplete="off"
                           aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>" />
                    <button type="button" class="myapp-eye-toggle" data-target="password" tabindex="-1" aria-label="${msg('showPassword')!'Показать пароль'}">
                        <svg class="myapp-eye-icon myapp-eye-icon--show" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8Z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg class="myapp-eye-icon myapp-eye-icon--hide" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display:none">
                            <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a21.6 21.6 0 0 1 5.06-6.06M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a21.4 21.4 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                            <line x1="1" y1="1" x2="23" y2="23"/>
                        </svg>
                    </button>
                </div>
            </div>

            <#-- Запомнить меня -->
            <#if realm.rememberMe && !usernameHidden??>
                <label class="myapp-checkbox-row">
                    <#if login.rememberMe??>
                        <input id="rememberMe" name="rememberMe" type="checkbox" checked>
                    <#else>
                        <input id="rememberMe" name="rememberMe" type="checkbox">
                    </#if>
                    <span>${msg("rememberMe")}</span>
                </label>
            </#if>

            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>

            <button tabindex="4" class="myapp-submit" name="login" id="kc-login" type="submit">
                <svg class="myapp-submit-icon myapp-submit-icon--default" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
                </svg>
                <svg class="myapp-submit-icon myapp-submit-icon--spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="display:none">
                    <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                </svg>
                <span class="myapp-submit-text">${msg("doLogIn")}</span>
            </button>
        </form>

    <#elseif section = "info">
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <p class="myapp-footnote">
                ${msg("noAccount")}
                <a href="${url.registrationUrl}">${msg("doRegister")}</a>
            </p>
        </#if>
    </#if>

</@layout.registrationLayout>
