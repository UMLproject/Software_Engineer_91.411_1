$(document).ready(function() {
    _ADMINS = {"admin@cloud.com": "password"};
    _USERS = {"johndoe@cloud.com": "password"};

    $("#formSignin").submit(function(e) {
        e.preventDefault();

        authEmail = $("#email").val();
        authPassword = $("#password").val();

        console.log(authEmail);
        console.log(authPassword);

        if (_ADMINS[authEmail] === authPassword) {
            window.location.href = "admin.html";
            console.log("Auth: admin");
        }
        else if (_USERS[authEmail] === authPassword) {
            window.location.href = "user.html";
            console.log("Auth: user");
        }
        else {
            alert("Unauthorized");
        }
    });

    $("#formSignup").submit(function(e) {
        e.preventDefault();

        alert("Registration is unimplemented.\nTry johndoe@cloud.com, password");
    });
});
