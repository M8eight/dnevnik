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

function myappSubmit(form) {
    var btn = document.getElementById('kc-login');
    if (!btn || btn.disabled) return;

    btn.disabled = true;
    btn.querySelector('.myapp-submit-icon--default').style.display = 'none';
    btn.querySelector('.myapp-submit-icon--spinner').style.display = '';
    btn.querySelector('.myapp-submit-text').textContent = 'Входим...';
}
