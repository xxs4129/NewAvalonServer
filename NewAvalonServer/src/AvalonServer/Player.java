package AvalonServer;

import org.json.JSONObject;

public class Player {
	private int statuse=0;						//玩家狀態(0~8)
	private int order=0;						//玩家順序
	private String nickname=null;				//玩家暱稱
	
	//建構子
	public Player(int status,int order,String nickname){	//設定初始值,狀態,順序,暱稱
		try{
			this.statuse=status;
			this.order=order;
			this.nickname=nickname;
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("某玩家Player建構子崩潰="+e.toString());
			Socket_Server.text.append("\n"+"某玩家Player建構子崩潰="+e.toString());
		}
	}		
	
	//=========================存取參數方面方法================================
	
	public void setStatus(int statuse){			//設定狀態
		this.statuse=statuse;
	}
	
	public void setOrder(int order){			//設定順序
		this.order=order;
	}
	
	public void setNickname(String nickname){	//設定暱稱
		this.nickname=nickname;
	}
	
	public int getStatus(){			//取得狀態
		return this.statuse;
	}
	
	public int getOrder(){			//取得順序
		return this.order;
	}
	
	public String getNickname(){	//取得暱稱
		return this.nickname;
	}
	
	public JSONObject getJSONObject(){
		JSONObject json=new JSONObject();
		try{
			json.put("status",this.statuse);
			json.put("order",this.order);
			json.put("nickname",this.nickname);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(this.nickname+"玩家Player.getJSONObject()崩潰="+e.toString());
            Socket_Server.text.append("\n"+this.nickname+"玩家Player.getJSONObject()崩潰="+e.toString());
        }
		return json;
	}
	//=========================功能方面方法===================================
	
	@Override
	public String toString() {
		String re=null;
		try{
			re="{";
			re+="statuse="+statuse+",";
			re+="order="+order+",";
			re+="nickname="+nickname;
			re+="}";
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(this.nickname+"玩家Player.toString()崩潰="+e.toString());
			Socket_Server.text.append("\n"+this.nickname+"玩家Player.toString()崩潰="+e.toString());
		}
		return re;
	}
}
