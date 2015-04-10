var alertTimer = 0;
var removeTimer = 0;

function sendAlert(title, message, severity) {
    clearTimeout(alertTimer);
    clearTimeout(removeTimer);
    $('#alertOverlay').remove();

    var str = '';
    str += '<div id="alertOverlay" class="alert ' + severity + '">';
    str += '<a href="#" class="close" data-dismiss="alert">&times;</a>'
    str += '<strong>' + title + '</strong> | ' + message;
    str += '</div>'

    $('#placeholder').append(str);

    alertTimer = setTimeout(function() {
        $("#alertOverlay").fadeOut(1000);

        removeTimer = setTimeout(function() {
            $('#alertOverlay').remove();
        }, 1000);
    }, 2500);
}

function alertSuccess(title, message) {
    sendAlert(title, message, "alert-success");
}

function alertInfo(title, message) {
    sendAlert(title, message, "alert-info");
}

function alertWarning(title, message) {
    sendAlert(title, message, "alert-warning");
}

function alertError(title, message) {
    sendAlert(title, message, "alert-danger");
}
