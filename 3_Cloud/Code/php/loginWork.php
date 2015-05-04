<?php

include 'connector.php';

$email = $_POST['email'];
$password = $_POST['password'];

$query = mysqli_query($link, "select * from users where '$email' = email");
if (!$query) {
    printf("Error: %s\n", mysqli_error($link));
    exit();
}
while ($res = mysqli_fetch_array($query)) {
    $stored_ps = $res['password'];
    $isAdmin = $res['isAdmin'];
    $stored_email = $res['email'];

    if ($stored_email == $email) {
        if ($stored_ps == $password) {
            if ($isAdmin == 0) {
                //to user page
                header('Location: http://localhost/SE_A4/user.php');
            } else if ($isAdmin == 1) {
                //to admin page
                header('Location: http://localhost/SE_A4/admin.php');
            }
        } else {
            //wrong password
            header('Location: http://localhost/SE_A4/signup.php');
        }
    } else {
        //wrong email
        header('Location: http://localhost/SE_A4/signup.php');
    } 
}
?>
