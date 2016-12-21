package AvalonServer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Room {
	private User roomUser=null;					//房主
	private boolean state=false;				//此房間狀態(true為可進入,false為遊戲中)
	private List<User> room_players=null;		//存放房間內所有玩家
	private List<Player> state_table=null;		//給房間內所有人看的狀態表,開始遊戲前、刺客暗殺、gameover
	private Start_Game start_game=null;			//負責處理開始遊戲後的雜事
	//建房參數
	private JSONObject json=null;				//用來回傳給進房者的json
	private int total=0;						//房間總人數
	private boolean[] cards=new boolean[16];	//卡片種類
	private boolean ynroompassword=false;		//判斷房間是否有使用密碼
	private String roompassword=null;			//房間密碼
	private boolean goddess=false;				//是否啟用湖中女神
	private boolean excalibur=false;			//是否啟用石中劍
	private int blue_Slaughter_Rule=0;			//蘭斯洛特規則
	
	//建構子,建房者的參數
	public Room(User user,JSONObject json_read){
		try{
			//輸入房間參數
			this.json=json_read;
			this.total=json_read.getInt("total");
			JSONArray array=json_read.getJSONArray("cards");
			for(int i=0;i<array.length();i++)
				this.cards[i]=array.getBoolean(i);
			this.ynroompassword=json_read.getBoolean("ynroompassword");
			if(ynroompassword)
				this.roompassword=json_read.getString("roompassword");
			this.goddess=json_read.getBoolean("goddess");
			this.excalibur=json_read.getBoolean("excalibur");
			this.blue_Slaughter_Rule=json_read.getInt("blue_Slaughter_Rule");
			//設定房主
			roomUser=user;			//設好房主
			this.state=true;		//設定房間進出狀態為等待
			room_players=new ArrayList<User>();		//初始化房間內的玩家表
			state_table=new ArrayList<Player>();	//初始化房間狀態表
			user.setRoom(this);						//設定房間
			room_players.add(room_players.size(),user);								//將玩家添加進玩家表
			state_table.add(new Player(19,state_table.size(),user.getNickname()));	//將玩家添加進狀態表
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Room建構子崩潰="+e.toString());
			Socket_Server.text.append("\n"+"Room建構子崩潰="+e.toString());
		}
	}
	
	//取得進房參數
	public JSONObject ReturnForInRoom(){
		return this.json;
	}
	
	//第一次進房
	public void in_to_Room(User user){
		try{
			for(int i=0;i<room_players.size();i++)
				if(room_players.get(i)==user)
					user.setOrder(i);		//設定順序
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(roomUser.getId()+"號房Room.ReturnForInRoom()崩潰="+e.toString());
			Socket_Server.text.append("\n"+roomUser.getId()+"號房Room.ReturnForInRoom()崩潰="+e.toString());
		}
	}
	
	//結束房間清除
	public void Clear(){
		try{
			json=null;
			roomUser=null;
			if(start_game!=null){
				start_game.Clear();
				start_game=null;
			}
			room_players.clear();				//對所有人設定離房後再清除表
			state_table.clear();
			cards=null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(roomUser.getId()+"號房Room.Clear()崩潰="+e.toString());
            Socket_Server.text.append("\n"+roomUser.getId()+"號房Room.Clear()崩潰="+e.toString());
        }
	}
	
	//取得給所有人看的狀態表
	public JSONArray getAllPlayer(){
		JSONArray json_array=new JSONArray();
		try {
			for(int i=0;i<state_table.size();i++)
				json_array.put(i,state_table.get(i).getJSONObject());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(roomUser.getId()+"號房Room.getAllPlayer()崩潰="+e.toString());
			Socket_Server.text.append("\n"+roomUser.getId()+"號房Room.getAllPlayer()崩潰="+e.toString());
		}
		return json_array;
	}
	
	//當有人離房時，重新設定所有人的order
	public void setPlayersOrder(){
		try {
			for(int i=0;i<room_players.size();i++){
				room_players.get(i).setOrder(i);
				state_table.get(i).setOrder(i);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(roomUser.getId()+"號房Room.setPlayersOrder()崩潰="+e.toString());
			Socket_Server.text.append("\n"+roomUser.getId()+"號房Room.setPlayersOrder()崩潰="+e.toString());
		}
	}
	
	//開始遊戲後
	public void Start(){
		try {
			this.state=false;	//關閉房間
			this.start_game=new Start_Game(Room.this);	//初始化並取得領導與女神順序並分配角色
			if(blue_Slaughter_Rule!=0)	//隨機取得忠誠卡牌
				this.start_game.Random_Loyalty();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(roomUser.getId()+"號房Room.Start()崩潰="+e.toString());
			Socket_Server.text.append("\n"+roomUser.getId()+"號房Room.Start()崩潰="+e.toString());
		}
	}
	
	//=========================存取參數方面方法================================
	
	//取得房主
	public User getRoomUser(){
		return this.roomUser;
	}
	
	//取得房間狀態
	public boolean getState(){
		return this.state;
	}
	
	//取得room_user
	public List<User> getRoom_Players(){
		return this.room_players;
	}
	
	//取得state_table
	public List<Player> getState_Table(){
		return this.state_table;
	}
	
	//取得房間是否有密碼
	public boolean getYesRoNo_Password(){
		return this.ynroompassword;
	}
	
	//取得總人數
	public int getTotal(){
		return this.total;
	}
	
	//取得房間人數
	public int getNumber(){
		return this.room_players.size();
	}
	
	//取得卡片種類
	public boolean[] getCards(){
		return this.cards;
	}
		
	//取得是否啟用女神
	public boolean getGoddess(){
		return this.goddess;
	}
		
	//取得是否使用石中劍
	public boolean getExcalibur(){
		return this.excalibur;
	}
	
	//取得蘭斯洛特規則
	public int getBlue_Slaughter_Rule(){
		return this.blue_Slaughter_Rule;
	}
	
	//取得Start_Game
	public Start_Game getStart_Game(){
		return this.start_game;
	}
	
	//判斷房間是否滿人
	public boolean Over(){
		return (total==room_players.size());
	}
		
	//判斷房間密碼是否正確
	public boolean passwordYesOrNo(String password){
		return (this.roompassword.equals(password));
	}
	
	//=========================功能方面方法===================================
	
	@Override
	public String toString() {
		String re=null;
		try{
			re="{";
			re+="id="+roomUser.getId()+",";
			re+="state="+state+",";
			re+="total="+total+",";
			re+="number="+room_players.size();
			re+="}";
			//等需要再添加
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(roomUser.getId()+"號房Room.toStirng()崩潰="+e.toString());
			Socket_Server.text.append("\n"+roomUser.getId()+"號房Room.toStirng()崩潰="+e.toString());
		}
		return re;
	}
	
}
