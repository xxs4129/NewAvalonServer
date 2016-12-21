package AvalonServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
public class Start_Game {
	private Room room=null;		//房間
	private int leader=0;		//亞瑟王順序
	private int goddess=0;		//女神順序
	private int task=0;			//目前第幾次任務
	private boolean over=false;	//判斷遊戲結束
	private List<Integer> count=null;				//計數
	private List<Integer> vote_approve=null;		//投票贊成者
	private List<Integer> vote_opposition=null;		//投票反對者
	private Map<Integer,Boolean> task_success_fail=null;	//任務成功與失敗
	private boolean[] loyalty=null;					//儲存忠誠卡牌
	private int [][] task_total={{2,3,2,3,3},{2,3,4,3,4},{2,3,3,4,4},{3,4,4,5,5},{3,4,4,5,5},{3,4,4,5,5}};	//各任務人數
	//建構子
	public Start_Game(Room room){
		try{
			this.room=room;
			this.leader=(int)(Math.random()*room.getRoom_Players().size());				//隨機亞瑟王
			if(room.getGoddess())
				this.goddess=((leader==0)?(room.getRoom_Players().size()):(leader))-1;	//取得女神順序
			Random_Cards();			//分配角色
			task=1;					//第一回合任務
			vote_approve=new ArrayList<>();
			vote_opposition=new ArrayList<>();
			task_success_fail=new HashMap<>();
			count=new ArrayList<>();
			for(int i=0;i<10;i++)		//記得依ok數更改
				count.add(0);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Start_Game建構子崩潰="+e.toString());
            Socket_Server.text.append("\n"+"Start_Game建構子崩潰="+e.toString());
        }
	}
	
	//=========================功能方面方法===================================
	
	//隨機分發角色
	public void Random_Cards(){
		try{
			List<Integer> cards_tmp=new ArrayList<Integer>();
			int index=0;
			for(boolean b:room.getCards()){	//存進被選中的卡種
				if(b)
					cards_tmp.add(index);
				index++;
			}
			index=0;
			//隨機分發給玩家
			List<User> room_user=room.getRoom_Players();
			while(index<room_user.size()){
				int random=(int)(Math.random()*cards_tmp.size());
				room_user.get(index).setCard(cards_tmp.get(random));	//存進各別玩家的card
				index++;
				cards_tmp.remove(random);
			}
			
			//釋放空間
			room_user=null;
			cards_tmp.clear();
			cards_tmp=null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Start_Game.Random_Cards()崩潰="+e.toString());
            Socket_Server.text.append("\n"+"Start_Game.Random_Cards()崩潰="+e.toString());
        }
	}
	
	//隨機忠誠卡牌
	public void Random_Loyalty(){
		try{
			int index=0;
			List<Boolean> loyalty_tmp=new ArrayList<Boolean>();
			switch(room.getBlue_Slaughter_Rule()){
			case 1:		//規則1
				loyalty_tmp.add(true);
				loyalty_tmp.add(true);
				loyalty_tmp.add(false);
				loyalty_tmp.add(false);
				loyalty_tmp.add(false);
				loyalty=new boolean[3];
				break;
			case 2:		//規則2
				loyalty_tmp.add(true);
				loyalty_tmp.add(true);
				loyalty_tmp.add(false);
				loyalty_tmp.add(false);
				loyalty_tmp.add(false);
				loyalty_tmp.add(false);
				loyalty_tmp.add(false);
				//只取5個值
				loyalty=new boolean[5];
				break;
			case 3:		//規則3
				return;
			}
			//取值
			while(index<loyalty.length){
				int random=(int)(Math.random()*loyalty_tmp.size());	
				loyalty[index]=loyalty_tmp.get(random);
				index++;
				loyalty_tmp.remove(random);
			}
			//釋放空間
			loyalty_tmp.clear();
			loyalty_tmp=null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Start_Game.Clear()崩潰="+e.toString());
            Socket_Server.text.append("\n"+"Start_Game.Clear()崩潰="+e.toString());
        }
	}
	
	//給所有人看的狀態表各別處理成玩家各自的狀態表
	public void setPlayers_State_Table(){
		try {
			boolean blue=room.getBlue_Slaughter_Rule()==3;	//是否選用藍斯洛特且有添加c規則
			List<User> room_user=room.getRoom_Players();
			List<Player> state_table=room.getState_Table();
			List<Player> state_tmp=null;
			for(User user:room_user){
				state_tmp=new ArrayList<Player>();
				state_tmp.addAll(state_table);
				int card=user.getCard();		//本人卡片
				int order=user.getOrder();		//自己的順序
				for(User player:room_user){
					int playerCard=player.getCard();//對方卡片
					int playOrder=player.getOrder();//別人的順序
					if(player!=user){				//判斷別人的狀態
						switch(card){
						case 0:			//本身為梅林,判斷其他人
							if((playerCard<7)||(playerCard==14))
								state_tmp.get(playOrder).setStatus(16);	//好人未知
							else
								state_tmp.get(playOrder).setStatus(18);	//壞人
							if(playerCard==9)
								state_tmp.get(playOrder).setStatus(16);	//莫德雷德未知
							break;
						case 1:			//身為派西維爾,判斷其他人
							if((playerCard==0)||(playerCard==8)){
									state_tmp.get(playOrder).setStatus(0);	//梅林與莫甘娜
							}else{
								state_tmp.get(playOrder).setStatus(16);		//其他人都是未知
							}
							break;
						case 2:case 3:case 4:case 5:case 6:case 10:	//村民、奧伯龍
							state_tmp.get(playOrder).setStatus(16);	//看所有人都是未知
							break;
						case 7:case 8:case 9:case 11:case 12:case 13:	//壞人(除奧伯龍)
							if((playerCard>6)&&(playerCard<14))
								state_tmp.get(playOrder).setStatus(18);	//看到壞人方
							else
								state_tmp.get(playOrder).setStatus(16);	//其他人未知
							if(playerCard==10)
								state_tmp.get(playOrder).setStatus(16);	//奧伯龍未知
							if(playerCard==15)
								state_tmp.get(playOrder).setStatus(15);	//看的到壞藍斯
							break;
						case 14:			//好人藍斯洛特
							if((blue)&&(playerCard==15))
								state_tmp.get(playOrder).setStatus(15);	//看的到壞藍斯(有選c規則)
							else
								state_tmp.get(playOrder).setStatus(16);	//其他人未知
							break;
						case 15:			//壞人藍斯洛特
							if((blue)&&(playerCard==14))
								state_tmp.get(playOrder).setStatus(14);	//看的到好藍斯(有選c規則)
							else
								state_tmp.get(playOrder).setStatus(16);	//其他人未知
							break;
						}
					}else{							//判斷自己的狀態
						state_tmp.get(order).setStatus(card);
					}
				}
				user.setState_Table(state_tmp); 	//設定完成後交給各自的user
			}
			room_user=null;
			state_table=null;
			state_tmp=null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Start_Game.setPlayers_State_Table()崩潰="+e.toString());
			Socket_Server.text.append("\n"+"Start_Game.setPlayers_State_Table()崩潰="+e.toString());
		}
	}
	
	//領導換下一位
	public void NextLeader(){
		this.leader=(leader+1)%room.getRoom_Players().size();
	}
	
	//湖中女神持有人換下一位
	public void NextGoddess(int order){
		this.goddess=order;
	}
	
	//下一個任務
	public void NextTask(){
		this.task++;
		NextLeader();
	}
	
	//清除
	public void Clear(){
		try{
			room=null;
			count=null;
			vote_approve.clear();
			vote_approve=null;
			vote_opposition.clear();
			vote_opposition=null;
			loyalty=null;
			task_total=null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Start_Game.Clear()崩潰="+e.toString());
            Socket_Server.text.append("\n"+"Start_Game.Clear()崩潰="+e.toString());
        }
	}
	
	//取得陣營
	public boolean getCamp(int order){
		int card=room.getRoom_Players().get(order).getCard();
		if(card<7||card==14)
			return true;
		else
			return false;
	}
	
	//=========================存取參數方面方法================================
	
	//取得回合數
	public int getTask(){
		return this.task;
	}
	
	//取得忠誠卡牌
	public boolean[] getLoyalty(){
		return this.loyalty;
	}
	
	//取得忠誠卡牌(僅限規則1取法)
	public boolean getTaskLoyalty(){
			return this.loyalty[task-3];
	}
	
	//每回合蘭斯洛特卡轉變
	public void blueChange(){
		boolean change=false;
		int blue=this.room.getBlue_Slaughter_Rule();
		if(blue==1)
			change=this.loyalty[task-3];
		else if(blue==2)
			change=this.loyalty[task-1];
		if(change){		//有改變則更新好壞藍斯
			List <User> users=this.room.getRoom_Players();
			for(User user :users)
				switch(user.getCard()){
				case 14:user.setCard(15);break;
				case 15:user.setCard(14);break;
				}
		}
	}
	
	//取得領導
	public int getLeader(){			
		return this.leader;
	}
	
	//取得女神順序
	public int getGoddess(){
		return this.goddess;
	}
	
	//取得計數
	public int getCount(int num){
		return this.count.get(num);
	}
	
	//觸發計數
	public void TouchCount(int num){
		this.count.set(num,count.get(num)+1);
	}
	
	//計數歸零
	public void Count_To_Zero(int num){
		this.count.set(num,0);
	}
	
	//取得該任務人數
	public int getTaskNumberPeople(){
		return this.task_total[this.room.getTotal()-5][this.task-1];
	}
	
	//取得投票贊成者
	public List<Integer> getApprove(){
		return this.vote_approve;
	}
	
	//取得投票反對者
	public List<Integer> getOpposition(){
		return this.vote_opposition;
	}
	
	//計數贊成反對
	public void CountVote(boolean approve,int order){
		if(approve)
			this.vote_approve.add(order);
		else
			this.vote_opposition.add(order);
	}
	
	//清除投票贊成失敗者
	public void ClearVote(){
		this.vote_approve.clear();
		this.vote_opposition.clear();
	}
	
	//取得任務成功與失敗玩家
	public JSONObject getSuccessFail(){
		JSONObject json_write=new JSONObject();
		try{
			int success=0;
			int fail=0;
			for(int order:task_success_fail.keySet()){
				if(task_success_fail.get(order))
					success++;
				else
					fail++;
			}
			json_write.put("success", success);
			json_write.put("fail", fail);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Start_Game.getSuccessFail()崩潰="+e.toString());
            Socket_Server.text.append("\n"+"Start_Game.getSuccessFail()崩潰="+e.toString());
        }
		return json_write;
	}
	
	
	//計數任務成功與失敗
	public void CountTask(boolean success,int order){
		this.task_success_fail.put(order, success);
	}
	
	public boolean TaskExcalibur(int order){
		boolean old=this.task_success_fail.get(order);
		this.task_success_fail.put(order,!old);
		return old;
	}
	
	//清除任務成功失敗者
	public void ClearTask(){
		this.task_success_fail.clear();
	}
	
	//設定遊戲結束
	public void setOver(){
		this.over=true;
	}
	
	//取得遊戲結束
	public boolean getOver(){
		return this.over;
	}
}
