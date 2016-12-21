package AvalonServer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.json.JSONObject;

public class Socket_Server {
	private static ServerSocket serverSocket=null;	//ServerSocket
	private static int serverport = 8888;			//port
	public static Map<Integer,Room> AllRoom=new HashMap<>();			//所有房間<房間id,房間>
	public static Map<Integer,User> sign_in_list=new HashMap<>();		//在房間列表的玩家
	public static Map<Integer,User> sign_in_user=new HashMap<>();		//使用者登入表
	public static JDBC_Test jdbc=new JDBC_Test();						//連接上本機的資料庫
	public static JTextArea text=null;				//文字區塊
	// 程式進入點
    public static void main(String[] args) {
    	try {
    		//視窗
    		JFrame f=new JFrame("Avalon Server");
    		f.setBounds(0,0,700,500);							//設定大小
    		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	//點擊X關閉程式
    		
    		//文字區塊
    		text=new JTextArea("text");
    		text.setSize(650, 450);								//設定大小
    		text.setLineWrap(true);								//文字區塊超過寬度換行
    		Font x = new Font("Serif",0,20);					//設定文字大小
    		text.setFont(x);									//添加進文字區塊
    		//滑動桿
    		JScrollPane ScrollPane = new JScrollPane(text);		//設定滑動
    		f.getContentPane().add(BorderLayout.CENTER, ScrollPane);	//添加進視窗內(放置中間)
    		//呈現視窗
    		f.setVisible(true);									//呈現視窗
    		
    		jdbc.select_status_1();								//開始前將伺服器內所有狀態為1的改成0(避免卡帳)
    		
    		serverSocket = new ServerSocket(serverport);
    		System.out.println("Server開始執行");
    		text.setText("Server開始執行");
    		// 當Server運作中時
            while (!serverSocket.isClosed()) {
                // 呼叫等待接受客戶端連接
                waitNewPlayer();
            }
    	}catch(Exception e){
    		e.printStackTrace();
    		System.out.println("Socket_Server.main()崩潰="+e.toString());
    		text.append("\n"+"Socket_Server.main()崩潰="+e.toString());
    	}
    }
    
    // 等待接受客戶端連接
    public static void waitNewPlayer() {
        try {
            Socket socket = serverSocket.accept();	//等待Socket連進來
            socket.setSoTimeout(10000);				//設置Client10秒內沒回應(送心跳包)就斷開
            socket.setOOBInline(true);				//開啟接收緊急數據(才能讓伺服器收到Client送來的心跳包)
            User user=new User(socket);				//將連進來的Socket封裝進User
            // 創造新的Thread各別處理user
            createNewThread(user);
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Socket_Server.waitNewPlayer()崩潰="+e.toString());
            text.append("\n"+"Socket_Server.waitNewPlayer()崩潰="+e.toString());
        }
    }
    
    // 創造新的Thread各別處理user
    public static void createNewThread(User user) {
        // 以新的執行緒來執行
        Thread Player_Thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String tmp;				//用來接收的緩存
                    JSONObject json_read;	//將緩存轉存進json做拆解
                    
                    // 當Socket已連接時連續執行
                    while (user.getSocket().isConnected()) {
                    	try{
                        	tmp = user.getBr().readLine();        //緩衝從br串流讀取值
                            // 如果不是空訊息
                            if(tmp!=null){
                                //將取到的String抓取{}範圍資料
                                tmp=tmp.substring(tmp.indexOf("{"), tmp.lastIndexOf("}") + 1);
                                json_read=new JSONObject(tmp);
                                System.out.println("接收到="+tmp.toString());
                                text.append("\n"+"接收到="+tmp.toString());
                                //從客戶端取得值後做拆解,可使用switch做不同動作的處理與回應
                                int action=json_read.getInt("action");
                                switch(action){
                                case 0:	//玩家退出連線
                                	Break(user);return;
                                case 1:	//進到房間列表
                                	into_the_lise(json_read,user);break;
                                case 2:	//進房申請
                                	into_the_room_application(json_read,user);break;
                                case 3: //建房畫面申請
                                	into_the_build_application(json_read,user);break;
                                case 4: //房間列表內聊天送出
                                	GetListSpeak(json_read,user);break;
                                case 5: //申請跳頁祭品
                                	user.Action4(json_read);break;
                                case 6: //建房申請
                                	Build_Room(json_read,user);break;
                                case 7: //呼叫伺服器更新房內列表
                                	UpdataList(json_read,user);break;
                                case 8: //退出房間
                                	ExitRoom(user);break;
                                case 9:	//踢人
                                	RemovePlayer(json_read,user);break;
                                case 10://準備
                                	Ready(user);break;
                                case 11://開始遊戲
                                	StartGame(user);break;
                                case 12://確認計數
                                	OK(json_read,user);break;
                                case 13://亞瑟王選完人
                                	user.Action13(json_read);break;
                                case 14://取得玩家送來的投票
                                	GetVote(json_read,user);break;
                                case 15://取得玩家送來的任務成功或失敗
                                	GetTask(json_read,user);break;
                                case 16://取得王者之劍抉擇
                                	GetExcaliburSelect(json_read,user);break;
                                case 17://取得湖中女神抉擇
                                	GetGoddessSelect(json_read,user);break;
                                case 18://公布湖中女神結果
                                	user.Action20(json_read);break;
                                case 19://好壞人獲勝(送出自己角色卡給伺服器統計)
                                	BadWin(json_read,user);break;
                                case 20://刺客送來刺殺人選
                                	user.Action23(json_read);break;
                                }
                            }else{	//當使用強制關閉app時會不斷向Server傳null
                            	System.out.println(user.getNickname()+"玩家強制關閉app");
                            	text.append("\n"+user.getNickname()+"玩家強制關閉app");
                            	break;	//跳出while
                            }
                    	}catch(Exception e){	//Client連線中斷(wifi斷線),現在抓很猛(所有斷線都直接跳這個)
                    		System.out.println(user.getNickname()+"玩家連線中斷(wifi斷線)");
                    		text.append("\n"+user.getNickname()+"玩家連線中斷(wifi斷線)");
                    		break;		//跳出while
                    	}
                    }
                    Break(user);	//結束執行緒時跳至Break做離開伺服器處理
                } catch (Exception e) {
                	e.printStackTrace();
                    System.out.println(user.getNickname()+"Socket_Server.Player_Thread()崩潰="+e.toString());
                    text.append("\n"+user.getNickname()+"Socket_Server.Player_Thread()崩潰="+e.toString());
                }  
            }
        });
        // 啟動執行緒
        Player_Thread.start();
    }
    
    //玩家離開遊戲時,從玩家登入表、房間列表移除,在資料庫設定該玩家下線
  	public static void Break(User user){
  		try {
  			System.out.println(user.getNickname()+"玩家Break");
  			text.append("\n"+user.getNickname()+"玩家Break");
  			
  			sign_in_user.remove(user.getId());		//從登入表移除
  			sign_in_list.remove(user.getId());		//從房間列表移除
  			if(user.getId()!=0)						//不能有id等於0的玩家,避免未set就跳掉了
  				jdbc.update_status(0, user.getId());	//將資料庫的狀態登出
  			if(user.getRoom()!=null)				//代表從房間斷線，做退出房間
  				ExitRoom(user);
  			user.Action2();							//有人登出時也要更新房間列表為了讓大家知道目前線上幾個人
  			user.Close();							//關閉該使用者串流與socket
  		} catch (Exception e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  			System.out.println(user.getNickname()+"玩家Socket_Server.Break()崩潰="+e.toString());
  			text.append("\n"+user.getNickname()+"玩家Socket_Server.Break()崩潰="+e.toString());
  		}
      	
  	}
    
  	//玩家登入
  	public static void SignIn(User user,int id){
  		try{
  			user.set(id);								//設定id、暱稱
    		sign_in_user.put(id,user);					//加進登入名單
    		jdbc.update_status(1, user.getId());		//將資料庫的狀態登入
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("某玩家Socket_Server.SignIn()崩潰="+e.toString());
            text.append("\n"+"某玩家Socket_Server.SignIn()崩潰="+e.toString());
        }
  	}
  	
  	//進房間列表
    public static void into_the_lise(JSONObject json_read,User user){
    	try {
    		int id=json_read.getInt("userid");
    		if(sign_in_user.get(id)==null)		//如果該玩家未在登入名單內則做登入
    			SignIn(user,id);				
    		sign_in_list.put(id, user);		//加進房間列表名單
			user.Action2();					//更新所有房間列表內容
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
			System.out.println(user.getNickname()+"玩家Socket_Server.into_the_lise()崩潰="+e.toString());
			text.append("\n"+user.getNickname()+"玩家Socket_Server.into_the_lise()崩潰="+e.toString());
    	}
    }
    
  	//進房申請
    public static void into_the_room_application(JSONObject json_read,User user){
    	try {
			int roomid=json_read.getInt("roomid");
			Room room=AllRoom.get(roomid);			//找到指定的該房間
			if(user.getRoom()!=null){				//避免威志
				System.out.println(user.getNickname()+"玩家重複申請");
				text.append("\n"+user.getNickname()+"玩家重複申請");
				return;
			}else if(room==null){					//判斷是否有此房間
				user.Action0(2);return;
			}else if(room.Over()){					//判斷是否滿人
				user.Action0(0);return;
			}else if(!room.getState()){				//判斷房間是否開始遊戲
				user.Action0(3);return;
			}else if(room.getYesRoNo_Password()){	//有密碼的話判斷密碼是否正確
				String password=json_read.getString("roompassword");
				if(!room.passwordYesOrNo(password)){	//判斷密碼不正確
					user.Action0(1);return;
				}
			}else{
				//通過判斷後最後留下來的才可進房
				user.setRoom(room);						//設定房間
				room.getRoom_Players().add(room.getRoom_Players().size(),user);			//將玩家添加進玩家表
				room.getState_Table().add(new Player(19,room.getState_Table().size(),user.getNickname()));	//將玩家添加進狀態表
				user.Action1(roomid);					//送出進房成功
			}
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
			System.out.println(user.getNickname()+"玩家Socket_Server.into_the_room_application()崩潰="+e.toString());
			text.append("\n"+user.getNickname()+"玩家Socket_Server.into_the_room_application()崩潰="+e.toString());
    	}
    }
  	
    //建房畫面申請
    public static void into_the_build_application(JSONObject json_read,User user){
    	try {
    		int id=json_read.getInt("userid");
    		if(sign_in_user.get(id)==null)		//如果該玩家未在登入名單內則做登入
    			SignIn(user,id);
    		else								//如果已經在名單內代表是從房間列表跳過去的
    			sign_in_list.remove(user.getId());			//從房間列表移除
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
			System.out.println(user.getNickname()+"玩家Socket_Server.into_the_build_application()崩潰="+e.toString());
			text.append("\n"+user.getNickname()+"玩家Socket_Server.into_the_build_application()崩潰="+e.toString());
    	}
    }
    
    //接收說話再轉傳給所有人(聊天列表、房內)
    public static void GetListSpeak(JSONObject json_read,User user){
    	try {
    		json_read.put("action", 3);
    		if(user.getRoom()==null){					//該玩家無房間則傳至房間列表聊天室
    			user.SendRoomList(json_read);		
    		}else{										//該玩家有房間則傳至該房聊天室
    			user.SendRoom(json_read);				
    		}
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
			System.out.println(user.getNickname()+"玩家Socket_Server.GetListSpeak()崩潰="+e.toString());
			text.append("\n"+user.getNickname()+"玩家Socket_Server.GetListSpeak()崩潰="+e.toString());
		}
    }
    
    //建房申請
    public static void Build_Room(JSONObject json_read,User user){
    	try {
    		Room room=new Room(user,json_read);	//建房
    		AllRoom.put(user.getId(), room);	//加進所有房間表
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
			System.out.println(user.getNickname()+"玩家Socket_Server.Build_Room()崩潰="+e.toString());
			text.append("\n"+user.getNickname()+"玩家Socket_Server.Build_Room()崩潰="+e.toString());
		}
    }
    
    //呼叫伺服器更新房間列表
    public static void UpdataList(JSONObject json_read,User user){
    	try {
    		int roomid=json_read.getInt("roomid");
    		AllRoom.get(roomid).in_to_Room(user);
    		user.Action5();						//更新玩家列表
    		user.Action2();						//建房或進房都需讓房間列表玩家更新列表
    		JSONObject json=new JSONObject();	
    		json.put("text", "[系統] 玩家:"+user.getNickname()+" 加入房間。");
    		json.put("color", 0);
    		GetListSpeak(json,user);
    	} catch (Exception e) {
			// TODO Auto-generated catch block
    		e.printStackTrace();
			System.out.println(user.getNickname()+"玩家Socket_Server.UpdataList()崩潰="+e.toString());
			text.append("\n"+user.getNickname()+"玩家Socket_Server.UpdataList()崩潰="+e.toString());
    	}
    }
    
    //退房處理
    public static void ExitRoom(User user){
    	try{
    		boolean state=user.getRoom().getState();	//取得房間狀態
    		User roomUser=user.getRoom().getRoomUser();	//取得房主
    		List<User> room_players=user.getRoom().getRoom_Players();	//取得房內玩家名單
    		List<Player> state_table=user.getRoom().getState_Table();	//取得房間狀態表
    		room_players.remove(user.getOrder());		//玩家從房內玩家名單釋放
    		state_table.remove(user.getOrder());		//玩家從房間狀態表釋放
    		user.getRoom().setPlayersOrder();			//更新房內所有人順序
    		user.Action5();								//更新房內資訊
    		JSONObject json=new JSONObject();			//離房通知
    		json.put("text", "[系統] 玩家:"+user.getNickname()+" 離開房間。");
    		json.put("color", 0);
    		GetListSpeak(json,user);
    		if(roomUser==user){					//判斷退房者是否為房主
    			AllRoom.remove(user.getId());	//從所有房間表退房
    			user.Action6();					//送給房內所有人退房
    			user.getRoom().Clear();			//退房清除room
    			System.gc();					//只要關放就做一次gc
    		}else{								//退房者為普通玩家
    			if(!state){				//遊戲中房間退出
    				user.Action7();		//傳送終止遊戲(app停止所有按鈕)
    				for(User player:room_players){		//開始遊戲後只會看個人狀態表，移除個人狀態表跳GAME的玩家，併重整順序
    					player.removeState_Table(user.getOrder());	
    				}
    				if(user.getRoom().getStart_Game().getOver())
    					user.Action5();					//遊戲結束送更新房內資訊
    				else
    					user.Action10(true);			//送出更新個人狀態表
    			}
    		}
    		user.Action2();					//更新所有房間列表
    		user.setRoom(null);			//玩家null自身房間(Action5、6、7需要room所以需要擺在後面)
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.ExitRoom()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.ExitRoom()崩潰="+e.toString());
        }
    }
    
    //踢人
    public static void RemovePlayer(JSONObject json_read,User user){
    	try{
    		int order=json_read.getInt("order");
    		user.getRoom().getRoom_Players().get(order).Action8();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.RemovePlayer()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.RemovePlayer()崩潰="+e.toString());
        }
    }
    
    //準備
    public static void Ready(User user){
    	try{
    		Player player=user.getRoom().getState_Table().get(user.getOrder());
    		if(player.getStatus()==20)	//對調準備與等待
    			player.setStatus(19);
    		else
    			player.setStatus(20);
    		user.Action5();				//更新房內資訊
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.Ready()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.Ready()崩潰="+e.toString());
        }
    }
    
    //開始遊戲
    public static void StartGame(User user){
    	try{
    		user.getRoom().Start();		//初始化Start_Game
    		user.Action2();				//更新房間列表給在列表的人知道此房已經開始遊戲
    		user.Action9();				//將每個人的角色卡送回給玩家
    		user.getRoom().getStart_Game().setPlayers_State_Table();	//先將相認模式洗好，等計數結束送出
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.StartGame()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.StartGame()崩潰="+e.toString());
        }
    }
    
    //確認計數
    public static void OK(JSONObject json_read,User user){
    	try{
    		int ok=json_read.getInt("ok");
    		Start_Game start_game=user.getRoom().getStart_Game();
    		switch(ok){
    		case 0:		//計數抽卡ok
    			start_game.TouchCount(0);
    			if(start_game.getCount(0)==user.getRoom().getRoom_Players().size()){	//人數到達進入相認階段
    				user.Action10(false);
    				start_game.Count_To_Zero(0);
    			}
    			break;
    		case 1:		//相認階段ok
    			start_game.TouchCount(1);
    			if(start_game.getCount(1)==user.getRoom().getRoom_Players().size()){	//人數到達進入回合開始前blue規則
    				user.Action11();
    				start_game.Count_To_Zero(1);
    			}
    			break;
    		case 2:		//blue規則ok
    			start_game.TouchCount(2);
    			if(start_game.getCount(2)==user.getRoom().getRoom_Players().size()){	//人數到達進入開始第x回合
    				user.Action12();
    				start_game.Count_To_Zero(2);
    			}
    			break;
    		case 3:		//投票ok
    			start_game.TouchCount(3);
    			if(start_game.getCount(3)==user.getRoom().getRoom_Players().size()){	//人數到達依投票結果分岐
    				user.Action15();
    				start_game.ClearVote();			//投票結束送出結果後做清除
    				start_game.Count_To_Zero(3);
    			}
    			break;
    		case 4:		//投票失敗ok
    			start_game.TouchCount(4);
    			if(start_game.getCount(4)==user.getRoom().getRoom_Players().size()){	//人數到達換下一位亞瑟王開始第x回合
    				start_game.NextLeader();
    				user.Action12();
    				start_game.Count_To_Zero(4);
    			}
    			break;
    		case 5:		//出任務ok
    			start_game.TouchCount(5);
    			if(start_game.getCount(5)==start_game.getTaskNumberPeople()){		//人數到達依是否使用石中劍分岐
    			//if(start_game.getCount(5)==user.getRoom().getRoom_Players().size()){	//測試用，正式版需使用上面那行
    				if(user.getRoom().getExcalibur())
    					user.Action16();	//送出等待王者之劍玩家抉擇
    				else
    					user.Action18();	//送出任務結果
    				start_game.Count_To_Zero(5);
    			}
    			break;
    		case 6:		//王者之劍ok
    			start_game.TouchCount(6);
    			if(start_game.getCount(6)==user.getRoom().getRoom_Players().size()){	//人數到達丟出結果
    				user.Action18();
    				start_game.Count_To_Zero(6);
    			}
    			break;
    		case 7:		//壞人獲勝ok
    			start_game.TouchCount(7);
    			if(start_game.getCount(7)==user.getRoom().getRoom_Players().size()){	//人數到達壞人獲勝公布所有人卡牌
    				user.Action21();
    				start_game.Count_To_Zero(7);
    			}
    			break;
    		case 8:		//好人獲勝3success ok
    			start_game.TouchCount(8);
    			if(start_game.getCount(8)==user.getRoom().getRoom_Players().size()){	//人數到達壞人現身
    				user.Action22();
    				start_game.Count_To_Zero(8);
    			}
    			break;
    		case 9:		//好人逃過刺殺獲勝 ok
    			start_game.TouchCount(9);
    			if(start_game.getCount(9)==user.getRoom().getRoom_Players().size()){	//人數到達好人獲勝公布所有人卡牌
    				user.Action24();
    				start_game.Count_To_Zero(9);
    			}
    			break;
    		}
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.OK()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.OK()崩潰="+e.toString());
        }
    }
    
    //接收玩家送來的投票
    public static void GetVote(JSONObject json_read,User user){
    	try{
    		Start_Game start_game=user.getRoom().getStart_Game();
    		boolean vote=json_read.getBoolean("vote");
    		start_game.CountVote(vote, user.getOrder());
    		user.Action14(user.getOrder());
    		json_read.put("ok", 3);
    		OK(json_read,user);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.GetVote()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.GetVote()崩潰="+e.toString());
        }
    }
    
    //接收玩家送來的任務成功或失敗
    public static void GetTask(JSONObject json_read,User user){
    	try{
    		Start_Game start_game=user.getRoom().getStart_Game();
    		boolean task=json_read.getBoolean("task");
    		start_game.CountTask(task, user.getOrder());
    		json_read.put("ok", 5);
    		OK(json_read,user);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.GetTask()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.GetTask()崩潰="+e.toString());
        }
    }
    
    //接收王者之劍抉擇
    public static void GetExcaliburSelect(JSONObject json_read,User user){
    	try{
    		Start_Game start_game=user.getRoom().getStart_Game();
    		int order=json_read.getInt("order");
    		if(order==-1){	//如果不使用王者之劍
    			user.Action17(order, false);
    		}else{
    			user.Action17(order, start_game.TaskExcalibur(order));
    		}
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.GetExcaliburSelect()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.GetExcaliburSelect()崩潰="+e.toString());
        }
    }
    
    //接收湖中女神抉擇
    public static void GetGoddessSelect(JSONObject json_read,User user){
    	try{
    		int order=json_read.getInt("order");
    		user.Action19(order, user.getRoom().getStart_Game().getCamp(order));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.GetGoddessSelect()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.GetGoddessSelect()崩潰="+e.toString());
        }
    }
    
    //好壞人獲勝(送出自己角色卡給伺服器統計)
    public static void BadWin(JSONObject json_read,User user){
    	try{
    		user.setCard(json_read.getInt("card"));
    		if(json_read.getBoolean("win"))
    			json_read.put("ok", 8);
    		else
    			json_read.put("ok", 7);
    		OK(json_read,user);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println(user.getNickname()+"玩家Socket_Server.GetGoddessSelect()崩潰="+e.toString());
            text.append("\n"+user.getNickname()+"玩家Socket_Server.GetGoddessSelect()崩潰="+e.toString());
        }
    }
    
    //=========================存取參數方面方法================================
  	
  	//查看各項資訊
    public static void see(){
    	try{
    		System.out.println("AllRoom 所有房間="+AllRoom);
    		System.out.println("sign_in_list 房間列表所有人="+sign_in_list);
    		System.out.println("sign_in_user 使用者登入表="+sign_in_user);
    		text.append("\n"+"AllRoom 所有房間="+AllRoom);
    		text.append("\n"+"sign_in_list 房間列表所有人="+sign_in_list);
    		text.append("\n"+"sign_in_user 使用者登入表="+sign_in_user);
    	}catch(Exception e){
    		e.printStackTrace();
    		System.out.println("玩家Socket_Server.see()崩潰="+e.toString());
    		text.append("\n"+"玩家Socket_Server.see()崩潰="+e.toString());
    	}
    }
  	
}
