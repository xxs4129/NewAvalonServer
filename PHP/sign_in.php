<?php
header("Content-Type:text/html; charset=utf-8");
$response = array();
 
include('connect.php');
$db = new DB_CONNECT();

    $username = $_POST['username'];
    $password = $_POST['password'];
    $result = mysql_query("select *from user where username = '$username' and password = '$password' and status = 0");
    if (!empty($result)) {
        if (mysql_num_rows($result) > 0) {
            $result = @mysql_fetch_array($result);
			$id =$result["id"];
            $response["id"] = $result["id"];
            $response["nickname"] = $result["nickname"];
 
            $response["success"] = true;

            echo json_encode($response);
        } else {
            $response["success"] = false;
 
            echo json_encode($response);
        }
    } else {
        $response["success"] = false;
 
        echo json_encode($response);
    }

?>