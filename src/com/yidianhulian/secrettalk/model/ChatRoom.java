package com.yidianhulian.secrettalk.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;

import android.R.integer;
import android.R.string;

/**
 * 房间类
 * 
 * @author Administrator
 * 
 */
public class ChatRoom {
	private int id; // 房间ID
	private int nums = 0; // 房间总人数
	private int lines = 0; // 房间在线总人数
	private int news = 0;// 未读消息
	private String password; // 房间密码
	private String name; // 房间名称
	private String creater = ""; // 房间创建者
	private String created_on = ""; // 房间创建时间
	private String question = "";
	private String answer = "";
	private String group_id = "";
	private List<User> player = new ArrayList<User>(); // 房间参与人\
	private JSONArray playerJson = new JSONArray();

	public ChatRoom() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNums() {
		return nums;
	}

	public void setNums(int nums) {
		this.nums = nums;
	}

	public int getLines() {
		return lines;
	}

	public void setLines(int lines) {
		this.lines = lines;
	}

	public String getCreater() {
		return creater;
	}

	public void setCreater(String creater) {
		this.creater = creater;
	}

	public List<User> getPlayer() {
		return player;
	}

	public void setPlayer(List<User> player) {
		this.player = player;
	}

	public String getCreated_on() {
		return created_on;
	}

	public void setCreated_on(String created_on) {
		this.created_on = created_on;
	}

	public JSONArray getPlayerJson() {
		return playerJson;
	}

	public void setPlayerJson(JSONArray playerJson) {
		this.playerJson = playerJson;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public int getNews() {
		return news;
	}

	public void setNews(int news) {
		this.news = news;
	}

	public void initChatRoom(JSONObject room) {
		try {
			this.id = room.getInt("id");
			this.password = room.getString("password");
			this.name = room.getString("name");
			this.nums = room.getInt("nums");
			this.lines = room.getInt("lines");
			this.creater = room.getString("creater");
			this.created_on = room.getString("created_on");
			this.question = room.getString("question");
			this.answer = room.getString("answer");
			this.group_id = room.getString("group_id");
			this.news = getUnReadMsg(room.getString("group_id"));// 本地房间接收的未阅读（未销毁）的消息
			
//			EMConversation conversation = EMChatManager.getInstance()
//					.getConversation(room.getString("group_id"));
//			this.news = conversation.getUnreadMsgCount();// 获取环信推送的新消息
			
			JSONArray users = room.getJSONArray("player");
			if (users.length() > 0) {
				for (int i = 0; i < users.length(); i++) {
					User user = new User();
					JSONObject u = users.getJSONObject(i);
					user.setId(u.getLong("id"));
					user.setOpenid(u.getString("openid"));
					user.setLogin_type(u.getString("login_type"));
					user.setCreated_on(u.getString("created_on"));
					user.setNick_name(u.getString("nick_name"));
					user.setHx_pwd(u.getString("hx_pwd"));
					user.setHx_pwd("");
					user.makeJson();
					this.playerJson.put(u.getString("openid"));
					this.player.add(user);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取房间信息JSON字符串
	 * @return
	 */
	public String getRoomDetails(){
		JSONObject roomJson = new JSONObject();
		try {
			roomJson.put("id", this.id);
			roomJson.put("password", this.password);
			roomJson.put("name", this.name);
			roomJson.put("nums", getPlayer().size());
			roomJson.put("lines", this.lines);
			roomJson.put("creater", this.creater);
			roomJson.put("created_on", this.created_on);
			roomJson.put("question", this.question);
			roomJson.put("answer", this.answer);
			roomJson.put("group_id", this.group_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return roomJson.toString();
	}
	
	/**
	 * 获取对象
	 * @param roomDetails
	 */
	public void initRoomDetails(String roomDetails){
		JSONObject room;
		try {
			room = new JSONObject(roomDetails);
			this.id = room.getInt("id");
			this.password = room.getString("password");
			this.name = room.getString("name");
			this.nums = room.getInt("nums");
			this.lines = room.getInt("lines");
			this.creater = room.getString("creater");
			this.created_on = room.getString("created_on");
			this.question = room.getString("question");
			this.answer = room.getString("answer");
			this.group_id = room.getString("group_id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private int getUnReadMsg(String gid){
		int nums = 0;
		EMConversation conversation = EMChatManager.getInstance().getConversation(gid);;
		Iterator<EMMessage> Imsg = conversation.getAllMessages().iterator();
		while(Imsg.hasNext()){
			EMMessage msg = Imsg.next();
			if(msg.direct == EMMessage.Direct.RECEIVE){
				nums ++;
			}
		}
		return nums;
	}
}
