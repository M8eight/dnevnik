<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false showAnotherWayIfPresent=true>
<!DOCTYPE html>
<html lang="${(locale.currentLanguageTag)!'ru'}">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${msg("loginTitleHtml",(realm.displayNameHtml!''))?no_esc}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <#list properties.styles?split(' ') as style>
        <link rel="stylesheet" href="${url.resourcesPath}/${style}">
    </#list>
</head>
<body class="myapp-body">

<div class="myapp-page">

    <div class="myapp-bg-accent myapp-bg-accent--top"></div>
    <div class="myapp-bg-accent myapp-bg-accent--bottom"></div>

    <div class="myapp-wrap">

        <#nested "header">

        <div class="myapp-card">

            <#-- глобальное сообщение (ошибки конфигурации, редиректы и т.п.) -->
            <#if displayMessage && message?has_content && message.type != 'warning'>
                <div class="myapp-alert myapp-alert--${message.type}">
                    <div class="myapp-alert__icon">
                        <#if message.type = 'error'>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                        <#else>
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 12l2 2 4-4"/><circle cx="12" cy="12" r="10"/></svg>
                        </#if>
                    </div>
                    <div class="myapp-alert__body">
                        <div class="myapp-alert__title">
                            <#if message.type = 'error'>${msg("loginErrorTitle")}<#else>${msg("loginInfoTitle")}</#if>
                        </div>
                        <div class="myapp-alert__desc">${kcSanitize(message.summary)?no_esc}</div>
                    </div>
                </div>
            </#if>

            <#nested "form">
        </div>

        <#nested "info">

    </div>
</div>

<script src="${url.resourcesPath}/js/login.js"></script>
</body>
</html>
</#macro>
