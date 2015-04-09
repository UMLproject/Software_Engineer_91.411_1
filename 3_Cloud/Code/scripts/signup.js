$(document).ready(function() {
    _ADMINS = [{"admin@cloud.com": "password"}];
    _USERS = [{"johndoe@cloud.com": "password"}];

    $("#formSignin").submit(function(e) {
        e.preventDefault();

        adminNames = Object.keys(_ADMINS);
        userNames = Object.keys(_USERS);

        console.log(adminNames);
        console.log(userNames);

        authEmail = $("#email").val();
        authPassword = $("#password").val();

        console.log(authEmail);
        console.log(authPassword);

        if ($.inArray(authEmail, adminNames)) {
            if (_ADMINS[authEmail] === authPassword) {
                window.location.href = "admin.html";
                console.log("Auth: admin");
            }
        }
        else if ($.inArray(authEmail, userNames)) {
            if (_USERS[authEmail] === authPassword) {
                window.location.href = "user.html";
                console.log("Auth: user");
            }
        }
        else {
            alert("Unauthorized");
        }
    });
});
