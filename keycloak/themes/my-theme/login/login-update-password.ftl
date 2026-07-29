<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>

    <#if section = "header">
        <div class="myapp-brand">
            <div class="myapp-brand__icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/>
                    <path d="m9 12 2 2 4-4"/>
                </svg>
            </div>
            <h1 class="myapp-title">${msg("updatePasswordTitle")}</h1>
            <p class="myapp-subtitle">${msg("updatePasswordSubtitle")}</p>
        </div>

    <#elseif section = "form">

        <#-- Блок общей ошибки формы -->
        <#if messagesPerField.existsError('password','password-confirm')>
            <div class="myapp-alert myapp-alert--error" id="myapp-update-error">
                <div class="myapp-alert__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                </div>
                <div class="myapp-alert__body">
                    <div class="myapp-alert__title">${msg("loginErrorTitle")}</div>
                    <div class="myapp-alert__desc">${kcSanitize(messagesPerField.getFirstError('password','password-confirm'))?no_esc}</div>
                </div>
            </div>
        </#if>

        <form id="kc-passwd-update-form" onsubmit="myappSubmitUpdate(this); return true;" action="${url.loginAction}" method="post">

            <#-- скрытые поля, помогают менеджерам паролей корректно определить форму смены пароля -->
            <input type="text" id="username" name="username" value="${username!''}" autocomplete="username" readonly="readonly" style="display:none;"/>
            <input type="password" id="password" name="password" autocomplete="current-password" style="display:none;"/>

            <#-- Новый пароль -->
            <div class="myapp-field">
                <label for="password-new" class="myapp-label">${msg("passwordNew")}</label>
                <div class="myapp-input-wrap">
                    <svg class="myapp-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="11" rx="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                    <input tabindex="1" id="password-new" name="password-new" type="password"
                           class="myapp-input myapp-input--password"
                           autofocus autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password')>true</#if>" />
                    <button type="button" class="myapp-eye-toggle" data-target="password-new" tabindex="-1" aria-label="${msg('showPassword')!'Показать пароль'}">
                        <svg class="myapp-eye-icon myapp-eye-icon--show" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8Z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg class="myapp-eye-icon myapp-eye-icon--hide" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display:none">
                            <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a21.6 21.6 0 0 1 5.06-6.06M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a21.4 21.4 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                            <line x1="1" y1="1" x2="23" y2="23"/>
                        </svg>
                    </button>
                </div>
                <#if messagesPerField.existsError('password')>
                    <span class="myapp-field-error">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                </#if>
            </div>

            <#-- Подтверждение пароля -->
            <div class="myapp-field">
                <label for="password-confirm" class="myapp-label">${msg("passwordConfirm")}</label>
                <div class="myapp-input-wrap">
                    <svg class="myapp-input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="11" width="18" height="11" rx="2"/>
                        <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                    <input tabindex="2" id="password-confirm" name="password-confirm" type="password"
                           class="myapp-input myapp-input--password"
                           autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>" />
                    <button type="button" class="myapp-eye-toggle" data-target="password-confirm" tabindex="-1" aria-label="${msg('showPassword')!'Показать пароль'}">
                        <svg class="myapp-eye-icon myapp-eye-icon--show" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8Z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg class="myapp-eye-icon myapp-eye-icon--hide" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display:none">
                            <path d="M17.94 17.94A10.94 10.94 0 0 1 12 20c-7 0-11-8-11-8a21.6 21.6 0 0 1 5.06-6.06M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a21.4 21.4 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                            <line x1="1" y1="1" x2="23" y2="23"/>
                        </svg>
                    </button>
                </div>
                <#if messagesPerField.existsError('password-confirm')>
                    <span class="myapp-field-error">${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}</span>
                </#if>
            </div>

            <#-- Завершить остальные сессии (показывается только если действие вызвано из аккаунта, не при первом входе) -->
            <#if isAppInitiatedAction??>
                <label class="myapp-checkbox-row">
                    <input id="logout-sessions" name="logout-sessions" type="checkbox" value="on" checked>
                    <span>${msg("logoutOtherSessions")}</span>
                </label>
            </#if>

            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>

            <#if isAppInitiatedAction??>
                <div class="myapp-button-row">
                    <button tabindex="3" class="myapp-submit" name="submit" id="kc-update-submit" type="submit">
                        <svg class="myapp-submit-icon myapp-submit-icon--default" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
                        </svg>
                        <svg class="myapp-submit-icon myapp-submit-icon--spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="display:none">
                            <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                        </svg>
                        <span class="myapp-submit-text">${msg("doSubmit")}</span>
                    </button>
                    <button tabindex="4" class="myapp-submit myapp-submit--secondary" type="submit" name="cancel-aia" value="true">
                        ${msg("doCancel")}
                    </button>
                </div>
            <#else>
                <button tabindex="3" class="myapp-submit" name="submit" id="kc-update-submit" type="submit">
                    <svg class="myapp-submit-icon myapp-submit-icon--default" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
                    </svg>
                    <svg class="myapp-submit-icon myapp-submit-icon--spinner" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" style="display:none">
                        <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
                    </svg>
                    <span class="myapp-submit-text">${msg("doSubmit")}</span>
                </button>
            </#if>
        </form>

    </#if>

</@layout.registrationLayout>
