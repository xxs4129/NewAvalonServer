package AvalonServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBC_Test {
	private static String url ="jdbc:mysql://127.0.0.1:3306/avalon?useUnicode=true&characterEncoding=utf8";	
	//路徑  jdbc:mysql://  +  ip(這裡是連本機就使用127.0.0.1):mqsql的port  +  /資料庫名稱  +  ?useUnicode=true&characterEncoding=utf8   用來表明使用utf-8

	private static String databaseUser="root";		//資料庫使用者帳號
	private static String password="xxs4129";		//資料庫使用者密碼
	/*
	//user表格名稱
	private static String TABLE_USER="user";
	//欄位
	private static String USER_ID="id";	
	private static String USER_USERNAME="username";
	private static String USER_PASSWORD="password";
	private static String USER_NICKNAME="nickname";
	private static String USER_STATUS="status";
	*/
	//以ID搜尋USER表格內的該行暱稱
	private static String SELECT_STATUS =
		"SELECT id FROM user WHERE status=1";
	
	//以ID搜尋USER表格內的該行暱稱
	private static String SELECT_USER =
		"SELECT nickname FROM user WHERE id= ?";
	/*private static String SELECT_USER =
		"SELECT "+USER_ID+","+USER_NICKNAME+" from "+TABLE_USER+
		" where "+USER_ID+"= ?";*/
	//更改玩家的狀態
	private static String UPDATE_STATUS =
		"UPDATE user SET status= ? WHERE id= ?";
		
	private Connection con= null;	//用來連接的物件Database Object
	private Statement st=null;		//用來執行傳入SQL語法
	private ResultSet rs=null;		//回傳的結果集
	private PreparedStatement pst = null;	//用來執行傳入SQL語法,並且可以改變傳入變數,需先用?來做標示
	
	public JDBC_Test(){		//建構子,用來加載驅動並連接資料庫
		try{
			//加載MySQL驅動
			Class.forName("com.mysql.jdbc.Driver");
			//一個Connection代表一個資料庫連線
			con=(Connection) DriverManager.getConnection(url, databaseUser, password);
	    }catch(Exception e){
			e.printStackTrace();
			System.out.println("JDBC_Text建構子崩潰="+e.toString());
			Socket_Server.text.append("\n"+"JDBC_Text建構子崩潰="+e.toString());
		}
	}
	
	//=========================功能方面方法===================================
	
	//以status搜尋id把狀態為1的都改為0
	public void select_status_1(){
		try{
			pst = con.prepareStatement(SELECT_STATUS);
			rs=pst.executeQuery();
			while(rs.next()){
				pst = con.prepareStatement(UPDATE_STATUS);
				pst.setInt(1,0);
				pst.setInt(2,rs.getInt("id"));
				pst.executeUpdate();
            }
			Close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("JDBC_Test.select_status_1()崩潰="+e.toString());
			Socket_Server.text.append("\n"+"JDBC_Test.select_status_1()崩潰="+e.toString());
			Close();
		}
	}
	
	//以ID搜尋USER表格內的該行暱稱
	public String select_user_nickname(int id){
		String nickname=null;
		try{
			pst = con.prepareStatement(SELECT_USER);
			pst.setInt(1,id);
			rs=pst.executeQuery();
			if(rs.next()){
				nickname=rs.getString("nickname");
            }
			Close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("JDBC_Test.select_user()崩潰="+e.toString());
			Socket_Server.text.append("\n"+"JDBC_Test.select_user()崩潰="+e.toString());
			Close();
		}
		return nickname;
	}
	
	//更改玩家狀態(線上或離線)
	public void update_status(int status,int id){
		try{
			pst = con.prepareStatement(UPDATE_STATUS);
			pst.setInt(1,status);
			pst.setInt(2,id);
			pst.executeUpdate();
			Close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("JDBC_Test.update_status()崩潰="+e.toString());
			Socket_Server.text.append("\n"+"JDBC_Test.update_status()崩潰="+e.toString());
			Close();
		}
	}
	
	//關閉除了連接資料庫以外的參數
	private void Close(){
		try{
			if(rs!=null){
				rs.close();
				rs=null;
			}
			if(st!=null){
				st.close();
				st=null;
			}
			if(pst!=null){
				pst.close();
				pst=null;
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("JDBC_Test.Close()崩潰="+e.toString());
			Socket_Server.text.append("\n"+"JDBC_Test.Close()崩潰="+e.toString());
		}
	}
}
