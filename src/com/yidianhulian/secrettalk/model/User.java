package com.yidianhulian.secrettalk.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;

public class User {

	private long id;
	private String openid;
	private String login_type;
	private String created_on;
	private String nick_name;
	private String hx_pwd;
	private int status = 0;
	private JSONObject userInfo;

	public User() {
		super();
		this.userInfo = new JSONObject();
	}

	public User(JSONObject userInfo) {
		super();
		this.userInfo = new JSONObject();
		try {
			this.id = Integer.valueOf(userInfo.getString("id"));
			this.openid = userInfo.getString("openid");
			this.login_type = userInfo.getString("login_type");
			this.nick_name = userInfo.getString("nick_name");
			this.created_on = userInfo.getString("created_on");
			this.hx_pwd = userInfo.getString("hx_pwd");
			makeJson();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public User(JSONObject userInfo, boolean isStatus) {
		super();
		this.userInfo = new JSONObject();
		try {
			this.id = Integer.valueOf(userInfo.getString("id"));
			this.openid = userInfo.getString("openid");
			this.login_type = userInfo.getString("login_type");
			this.nick_name = userInfo.getString("nick_name");
			this.created_on = userInfo.getString("created_on");
			this.hx_pwd = userInfo.getString("hx_pwd");
			if(isStatus){
				if(userInfo.getString("status") != null){
					this.status = Integer.valueOf(userInfo.getString("status"));
				}
			}
			makeJson();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public User(long id, String openid, String login_type, String created_on,
			String nick_name, String hx_pwd) {
		super();
		this.id = id;
		this.openid = openid;
		this.login_type = login_type;
		this.created_on = created_on;
		this.nick_name = nick_name;
		this.hx_pwd = hx_pwd;
		this.userInfo = new JSONObject();
	}

	public JSONObject makeJson() {
		try {
			this.userInfo.put("id", this.id);
			this.userInfo.put("openid", this.openid);
			this.userInfo.put("login_type", this.login_type);
			this.userInfo.put("nick_name", this.nick_name);
			this.userInfo.put("created_on", this.created_on);
			this.userInfo.put("hx_pwd", this.hx_pwd);
			this.userInfo.put("status", this.status);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return userInfo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOpenid() {
		return openid.toLowerCase();
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getType() {
		return login_type;
	}

	public void setType(String type) {
		this.login_type = type;
	}

	public String getCreated_on() {
		return created_on;
	}

	public void setCreated_on(String created_on) {
		this.created_on = created_on;
	}

	public JSONObject getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(JSONObject userInfo) {
		this.userInfo = userInfo;
	}

	public String getNickname() {
		return nick_name;
	}

	public void setNickname(String nickname) {
		this.nick_name = nickname;
	}

	public String getHx_pwd() {
		return hx_pwd;
	}

	public void setHx_pwd(String hx_pwd) {
		this.hx_pwd = hx_pwd;
	}

	public String getNick_name() {
		return nick_name;
	}

	public void setNick_name(String nick_name) {
		this.nick_name = nick_name;
	}

	public String getLogin_type() {
		return login_type;
	}

	public void setLogin_type(String login_type) {
		this.login_type = login_type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return userInfo.toString();
	}
}
