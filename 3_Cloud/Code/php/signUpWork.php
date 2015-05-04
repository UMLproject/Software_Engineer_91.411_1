<?php

include 'connector.php';

$nameF = $_POST['nameFirst'];
$nameL = $_POST['nameLast'];
$email = $_POST['email'];
$passwordS = $_POST['passwordSignup'];
$passwordC = $_POST['passwordConfirm'];

if ($passwordS == $passwordC) {
    //if signing up as user
    if ($_POST['accountType'] == 'User') {
        $query = mysqli_query($link, "insert into users (nameFirst, nameLast, email, password, isAdmin) values ('$nameF','$nameL','$email','$passwordS','0')");
        if (!$query) {
            printf("Error: %s\n", mysqli_error($link));
            exit();
        }
        //to user page
        header('Location: http://localhost/SE_A4/user.php');
        
        //if signing up as admin
    } else if ($_POST['accountType'] == 'Admin') {
        $query = mysqli_query($link, "insert into users (nameFirst, nameLast, email, password, isAdmin) values ('$nameF','$nameL','$email','$passwordS','1')");
        if (!$query) {
            printf("Error: %s\n", mysqli_error($link));
            exit();
        }
        //to admin page
        header('Location: http://localhost/SE_A4/admin.php');
    }
} else {
    //passwords did not match
    header('Location: http://localhost/SE_A4/signup.php');
}
?>
