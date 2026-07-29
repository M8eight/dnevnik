document.querySelectorAll('.myapp-eye-toggle').forEach(function (btn) {
    btn.addEventListener('click', function () {
        var input = document.getElementById(btn.dataset.target);
        var showIcon = btn.querySelector('.myapp-eye-icon--show');
        var hideIcon = btn.querySelector('.myapp-eye-icon--hide');
        var isPassword = input.type === 'password';
 
        input.type = isPassword ? 'text' : 'password';
        showIcon.style.display = isPassword ? 'none' : '';
        hideIcon.style.display = isPassword ? '' : 'none';
    });
});
 
function myappSubmitButton(btn, loadingText) {
    if (!btn || btn.disabled) return;
 
    btn.disabled = true;
    var defaultIcon = btn.querySelector('.myapp-submit-icon--default');
    var spinnerIcon = btn.querySelector('.myapp-submit-icon--spinner');
    var text = btn.querySelector('.myapp-submit-text');
 
    if (defaultIcon) defaultIcon.style.display = 'none';
    if (spinnerIcon) spinnerIcon.style.display = '';
    if (text) text.textContent = loadingText;
}
 
function myappSubmit(form) {
    myappSubmitButton(document.getElementById('kc-login'), 'Входим...');
}
 
function myappSubmitUpdate(form) {
    myappSubmitButton(document.getElementById('kc-update-submit'), 'Сохраняем...');
}