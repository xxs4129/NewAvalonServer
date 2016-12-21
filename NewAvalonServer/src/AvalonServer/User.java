package AvalonServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class User {
	private Socket socket=null;		//使用者的socket
	private BufferedWriter bw=null;	//取得網路輸出串流
	private BufferedReader br=null;	// 取得網路輸入串流
	private int id;					//使用者的id
	private String nickname=null;	//使用者暱稱
	private Room room=null;			//使用者所在的房間
	private int order;				//在房間內的順序
	private int card;				//取得自己抽到的卡
	private List<Player> state_table=null;	//開始遊戲後個別玩家所能看到的狀態表
	//user建構子
	public User(Socket socket){
		try{
			this.socket=socket;
			//取得輸出入串流
			this.bw = new BufferedWriter( new OutputStreamWriter(this.socket.getOutputStream(),"UTF-8"));
			this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(),"UTF-8"));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("User建構子崩潰="+e.toString());
			Socket_Server.text.append("\n"+"User建構子崩潰="+e.toString());
		}
	}
	
	//=========================功能方面方法===================================
	
	@Override
	public String toString() {
		String tmp=null;
		try{
			tmp="{";
			tmp+=(room!=null)?("room=有房,"):("room=null,");
			tmp+="id="+id+",";
			tmp+="nickname="+nickname+",";
			tmp+="order="+order+",";
			tmp+="}";
			//等需要再添加
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.toStirng()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.toStirng()崩潰="+e.toString());
		}
		return tmp;
	}
	
	//關閉串流與socket
	public void Close(){
		try {
			if(bw!=null){
				this.bw.close();
				this.bw=null;
			}
			if(br!=null){
				this.br.close();
				this.br=null;
			}
			if(socket!=null){
				this.socket.close();
				this.socket=null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.Close()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.Close()崩潰="+e.toString());
		}
	}
	
	//取消指向
	public void Null(){
		this.bw=null;
		this.br=null;
		this.socket=null;
	}
	
	//送給房間列表的所有人
	public void SendRoomList(JSONObject json){
		try{
			for(Entry<Integer, User> user :Socket_Server.sign_in_list.entrySet()){
				user.getValue().Send(json);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.SendRoomList()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.SendRoomList()崩潰="+e.toString());
		}
	}
	
	//送給房間內所有人,指定不傳給某玩家
	public void SendRoom(JSONObject json){
		try{
			List<User> room_players=this.room.getRoom_Players();
			for(User player:room_players)	//送給房內所有人
				if(!player.getSocket().isClosed())
					player.Send(json);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.SendRoom()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.SendRoom()崩潰="+e.toString());
		}
	}
	
	//送出給該User玩家
	public void Send(JSONObject json){
		try{
			bw.write(json+"\n");
			bw.flush();
			System.out.println("送出="+json);
			Socket_Server.text.append("\n"+"送出="+json);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.Send()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.Send()崩潰="+e.toString());
		}
	}

	//action 0 失敗
	public void Action0(int message){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",0);
			json_write.put("message",message);
			Send(json_write);	//送給該玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.Action0()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.Action0()崩潰="+e.toString());
		}
	}
	
	//action 1 進房認證成功
	public void Action1(int roomid){
		try{
			Socket_Server.sign_in_list.remove(id);					//從房間列表移除
			JSONObject json_write=Socket_Server.AllRoom.get(roomid).ReturnForInRoom();	//取得進房參數
			json_write.put("action",1);
			Send(json_write);	//送給該玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家User.Action1()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家User.Action1()崩潰="+e.toString());
		}
	}
	
	//action 2 更新房間列表
	public void Action2(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",2);
			JSONArray json_array=new JSONArray();
			if(Socket_Server.AllRoom.size()!=0){		//判斷有無房間
				for(Entry<Integer, Room> tmp :Socket_Server.AllRoom.entrySet()){
					JSONObject json_tmp=new JSONObject();
					Room room=tmp.getValue();
					json_tmp.put("id", room.getRoomUser().getId());
					json_tmp.put("number", room.getNumber());
					json_tmp.put("total", room.getTotal());
					json_tmp.put("nickname", room.getRoomUser().getNickname());
					json_tmp.put("roomPassword", room.getYesRoNo_Password());
					json_tmp.put("state", room.getState());
					json_array.put(json_tmp);
				}
			}
			List<String> people_list=new ArrayList<>();
			for(int i:Socket_Server.sign_in_user.keySet())
				people_list.add(Socket_Server.sign_in_user.get(i).getNickname());
			json_write.put("people", people_list);	//送出在線人數
			json_write.put("rooms", json_array);
			SendRoomList(json_write);				//送給房間列表所有人
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action2()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action2()崩潰="+e.toString());
		}
	}
	
	//action 4 	送出跳房祭品
	public void Action4(JSONObject json_write){
		try{
			json_write.put("action",4);
			Send(json_write);	//送給該玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action4()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action4()崩潰="+e.toString());
		}
	}
	
	//action 5  送出房間內列表
	public void Action5(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",5);
			json_write.put("players",room.getAllPlayer());
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action5()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action5()崩潰="+e.toString());
		}
	}
	
	//action 6  送出房主離房
	public void Action6(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",6);
			//送給該房所有玩家
			List<User> room_players=this.room.getRoom_Players();
			for(User player:room_players){		//送給房內所有人
				player.setRoom(null);			//玩家null自身房間
				player.Send(json_write);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action6()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action6()崩潰="+e.toString());
		}
	}
	
	//action 7  遊戲中玩家離房,傳送終止遊戲(app停止所有按鈕)
	public void Action7(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",7);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action7()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action7()崩潰="+e.toString());
		}
	}
	
	//action 8 送出玩家被房主踢出
	public void Action8(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",8);
			Send(json_write);	//送給該玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action8()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action8()崩潰="+e.toString());
		}
	}
	
	//action 9將每個人的角色卡送回給玩家
	public void Action9(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",9);
			List<User> room_players=this.room.getRoom_Players();
			for(User player:room_players){	//送給房內所有人
				json_write.put("card",player.getCard());
				player.Send(json_write);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action8()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action8()崩潰="+e.toString());
		}
	}
	
	//action 10將每個人的狀態表送回給玩家
	public void Action10(boolean update){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",10);
			json_write.put("update",update);
			List<User> room_players=this.room.getRoom_Players();
			for(User player:room_players){	//送給房內所有人
				json_write.put("state_table",player.getState_Table());
				player.Send(json_write);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action10()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action10()崩潰="+e.toString());
		}
	}
	
	//action 11  開始前處理blue規則
	public void Action11(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",11);
			switch(room.getBlue_Slaughter_Rule()){
			case 1:
				if(room.getStart_Game().getTask()>2){	//3.4.5回合
					json_write.put("loyalty",room.getStart_Game().getTaskLoyalty());
					break;
				}else{
					Action12();
					return;
				}
			case 2:
				if(room.getStart_Game().getTask()==1)	//第一回合拿全部，接下來每回合只會呼叫不會傳送
					json_write.put("loyalty",room.getStart_Game().getLoyalty());
				break;
			case 0:case 3:
				Action12();
				return;
			}
			room.getStart_Game().blueChange();			//每次送出忠誠卡牌就會更新一次
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action11()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action11()崩潰="+e.toString());
		}
	}
	
	//action 12  開始遊戲,領導
	public void Action12(){
		try{
			Start_Game start_game=room.getStart_Game(); 
			JSONObject json_write=new JSONObject();
			json_write.put("action",12);
			json_write.put("leader",start_game.getLeader());
			json_write.put("goddess",start_game.getGoddess());
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action12()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action12()崩潰="+e.toString());
		}
	}
	
	//action 13  亞瑟王選完人送給所有人得知
	public void Action13(JSONObject json_write){
		try{
			json_write.put("action",13);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action13()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action13()崩潰="+e.toString());
		}
	}
	
	//action 14 告知玩家誰投完票
	public void Action14(int order){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",14);
			json_write.put("order",order);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action14()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action14()崩潰="+e.toString());
		}
	}
	
	//action 15 送出投票結果
	public void Action15(){
		try{
			JSONObject json_write=new JSONObject();
			JSONArray vote_approve=new JSONArray(room.getStart_Game().getApprove());
			JSONArray vote_opposition=new JSONArray(room.getStart_Game().getOpposition());
			json_write.put("action",15);
			json_write.put("vote_approve",vote_approve);
			json_write.put("vote_opposition",vote_opposition);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action15()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action15()崩潰="+e.toString());
		}
	}
	
	//action 16 送出等待王者之劍玩家抉擇
	public void Action16(){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",16);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action16()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action16()崩潰="+e.toString());
		}
	}
	
	//action 17 送出給所有人得知王者之劍對誰使用
	public void Action17(int order,boolean old){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",17);
			json_write.put("order", order);
			json_write.put("old", old);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action17()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action17()崩潰="+e.toString());
		}
	}
	
	//action 18 送出任務結果
	public void Action18(){
		try{
			JSONObject json_write=room.getStart_Game().getSuccessFail();
			json_write.put("action",18);
			SendRoom(json_write);	//送給該房所有玩家
			room.getStart_Game().ClearTask();		//清除記錄任務成功與失敗
			room.getStart_Game().NextTask();		//下一個任務，一並處理領導
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action18()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action18()崩潰="+e.toString());
		}
	}
	
	//action 19 送出湖中女神結果
	public void Action19(int order,boolean camp){
		try{
			JSONObject json_write=new JSONObject();
			json_write.put("action",19);
			json_write.put("order",order);
			json_write.put("camp",camp);
			SendRoom(json_write);	//送給該房所有玩家
			room.getStart_Game().NextGoddess(order);		//更新湖中女神順序
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action19()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action19()崩潰="+e.toString());
		}
	}
	
	//action 20 公布湖中女神結果
	public void Action20(JSONObject json_write){
		try{
			json_write.put("action",20);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action20()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action20()崩潰="+e.toString());
		}
	}
	
	//action 21 壞人獲勝，公布所有人卡牌
	public void Action21(){
		try{
			room.getStart_Game().setOver();
			JSONObject json_write=new JSONObject();
			JSONArray array=new JSONArray();
			List<User> players=room.getRoom_Players();
			List<Player> start_game=room.getState_Table();
			
			for(User user:players){
				start_game.get(user.getOrder()).setStatus(user.getCard());
				array.put(user.getCard());
			}
			json_write.put("action",21);
			json_write.put("cards", array);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action21()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action21()崩潰="+e.toString());
		}
	}
	
	//action 22 好人獲勝，壞人現身
	public void Action22(){
		try{
			JSONObject json_write=new JSONObject();
			JSONArray array=new JSONArray();
			List<User> players=room.getRoom_Players();
			for(User user:players){
				if(user.getCard()<7||user.getCard()==14)
					array.put(0);
				else
					array.put(user.getCard());
			}
			json_write.put("action",22);
			json_write.put("cards", array);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action22()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action22()崩潰="+e.toString());
		}
	}
	
	//action 23 公布刺殺結果
	public void Action23(JSONObject json_write){
		try{
			json_write.put("action",23);
			json_write.put("card", room.getRoom_Players().get(json_write.getInt("order")).getCard());
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action23()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action23()崩潰="+e.toString());
		}
	}
	
	//action 24 好人獲勝，公布所有人卡牌
	public void Action24(){
		try{
			room.getStart_Game().setOver();
			JSONObject json_write=new JSONObject();
			JSONArray array=new JSONArray();
			List<User> players=room.getRoom_Players();
			List<Player> start_game=room.getState_Table();
			
			for(User user:players){
				start_game.get(user.getOrder()).setStatus(user.getCard());
				array.put(user.getCard());
			}
			json_write.put("action",24);
			json_write.put("cards", array);
			SendRoom(json_write);	//送給該房所有玩家
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Action24()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Action24()崩潰="+e.toString());
		}
	}
	
	//=========================存取參數方面方法================================
	
	//取得socket
	public Socket getSocket(){
		return this.socket;
	}
	
	//設定玩家id、玩家暱稱
	public void set(int id){
		this.id=id;
		this.nickname=Socket_Server.jdbc.select_user_nickname(id);
	}
	
	//設定順序
	public void setOrder(int order){
		this.order=order;
	}
	
	//設定卡片
	public void setCard(int card){
		this.card=card;
	}
		
	//取得卡片
	public int getCard(){
		return this.card;
	}
	
	//取得id
	public int getId(){
		return this.id;
	}
	
	//取得暱稱
	public String getNickname(){
		return this.nickname;
	}
	
	//取得房間內順序
	public int getOrder(){
		return this.order;
	}

	//設定玩家進哪間房
	public void setRoom(Room room){
		this.room=room;
	}
	
	//取得玩家進哪間房
	public Room getRoom(){
		return this.room;
	}
		
	//取得user內的BufferedWriter
	public BufferedWriter getBw(){
		return this.bw;
	}
		
	//取得user內的BufferedReader
	public BufferedReader getBr(){
		return this.br;
	}
	
	//設定自身的State_Table
	public void setState_Table(List<Player> state_tmp) {
		this.state_table=new ArrayList<>();
		for(Player player:state_tmp)
			this.state_table.add(new Player(player.getStatus(),player.getOrder(),player.getNickname()));
	}
	
	//取得自己的State_Table
	public JSONArray getState_Table(){
		JSONArray json_array=new JSONArray();
		try {
			for(int i=0;i<state_table.size();i++)
				json_array.put(i,state_table.get(i).getJSONObject());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(this.nickname+"玩家getState_Table()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家getState_Table()崩潰="+e.toString());
		}
		return json_array;
	}
	
	//從State_Table移除某人
	public void removeState_Table(int order){
		try {
			this.state_table.remove(order);
			for(int i=0;i<state_table.size();i++)
				state_table.get(i).setOrder(i);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(this.nickname+"玩家removeState_Table()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家removeState_Table()崩潰="+e.toString());
		}
	}
	
}
