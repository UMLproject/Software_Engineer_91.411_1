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
            window.location.href = "#admin";
            console.log("Auth: admin");
        }
        else if (_USERS[authEmail] === authPassword) {
            window.location.href = "#user";
            console.log("Auth: user");
        }
        else {
            alertError("Error", "Incorrect username or password.");
        }
    });

    $("#formSignup").submit(function(e) {
        e.preventDefault();

        alert("Registration is unimplemented.\nTry johndoe@cloud.com, password");
    });
});
