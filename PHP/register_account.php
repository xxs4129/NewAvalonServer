
<?php
header("Content-Type:text/html; charset=utf-8");
$response = array();
 
// include db connect class
//require_once __DIR__ . '/db_connect.php';
include('connect.php');
// connecting to db
$db = new DB_CONNECT();

$username = $_POST['username'];
$password = $_POST['password'];
$nickname = $_POST['nickname'];

$result = mysql_query("select *from user where username = '$username'");
$row = @mysql_fetch_row($result);
$result_2 =mysql_query("select * from user where nickname = '$nickname'");
$row_2 = @mysql_fetch_row($result_2);
if ( $row != null)		//帳號重複
{
		$response["success"] = false;
		$response["repeat"] = true;
}
else if ( $row_2 != null)	//暱稱重複
{
		$response["success"] = false;
		$response["repeat"] = false;
}
else if($username != null && $password != null && $nickname != null)	//帳號、密碼、暱稱不為null
{
        $sql = "INSERT INTO user (id, username, password, nickname, status) VALUES (null,'$username','$password','$nickname',0)";
        if(mysql_query($sql))
        {
		$response["success"] = true;
        }
}
echo json_encode($response);








 ?>