package com.yidianhulian.secrettalk.activity;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import cn.sharesdk.framework.m;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.YDSecretTalkApplication;
import com.yidianhulian.secrettalk.model.ChatRoom;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.Util;

public class EnterPwdActivity extends Activity implements CallApiListener {
	YDSecretTalkApplication mApp;

	private static final int GETROOM = 1;
	private static final int ENTERROOM = 2;

	// guoxingcai 2014-11-17
	private Button mNext = null;
	private Button mEnter = null;
	private EditText mAnswer = null;
	private EditText mEnterPwd = null;
	private EditText mQuestion = null;
	private LinearLayout mSafety = null;

	private String password = "";
	private ChatRoom mRoom = null;
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_pwd);
		getActionBar().setIcon(R.drawable.return_bar);
		getActionBar().setTitle(R.string.enter_room);
		getActionBar().setHomeButtonEnabled(true);

		mApp = (YDSecretTalkApplication) getApplication();
		user = mApp.loginUser();

		// guoxingcai 2014-11-17
		mNext = (Button) findViewById(R.id.enter_next);
		mEnter = (Button) findViewById(R.id.enter_pwd_btn);
		mEnterPwd = (EditText) findViewById(R.id.enter_password);
		mAnswer = (EditText) findViewById(R.id.enter_room_answer);
		mQuestion = (EditText) findViewById(R.id.enter_room_question);
		mSafety = (LinearLayout) findViewById(R.id.room_safety);

		mSafety.setVisibility(View.GONE);
		mEnter.setVisibility(View.GONE);

		mNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Util.closeKeyboard(getWindow().peekDecorView(),
						getApplication());
				password = mEnterPwd.getText().toString();
				if ("".equals(password) || password.length() < 1) {
					Toast.makeText(EnterPwdActivity.this, "邀请密码不能为空！",
							Toast.LENGTH_SHORT).show();
				} else {
					Util.showLoading(EnterPwdActivity.this, "密码匹配中");
					loadData(GETROOM);
				}
			}
		});
		mEnter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Util.closeKeyboard(getWindow().peekDecorView(),
						getApplication());
				if (!mEnterPwd.getText().toString().equals(mRoom.getPassword())) {
					Toast.makeText(EnterPwdActivity.this,
							"你还没有搜索房间！输完密码，单击密码先...", Toast.LENGTH_LONG).show();
					return;
				}
				if (!"".equals(mRoom.getQuestion().toString())) {
					if (mAnswer.getText().toString().equals("")) {
						Toast.makeText(EnterPwdActivity.this, "答案不能为空！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					if (!mRoom.getQuestion().equals(
							mQuestion.getText().toString().trim())
							|| !mRoom.getAnswer().equals(
									mAnswer.getText().toString().trim())) {
						Toast.makeText(EnterPwdActivity.this, "答案错误！",
								Toast.LENGTH_SHORT).show();
						return;
					}
				}

				Util.showLoading(EnterPwdActivity.this, "正在进入房间...");
				loadData(ENTERROOM);
			}
		});

	}
	
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		super.onDestroy();
	}
	
	private void loadData(int what){
		if(Util.getAPNType(EnterPwdActivity.this) == -1){
			Util.hideLoading();
			Toast.makeText(EnterPwdActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
		}else{
			CallApiTask.doCallApi(what, EnterPwdActivity.this,
				EnterPwdActivity.this, CacheType.REPLACE,
				FetchType.FETCH_API_ELSE_CACHE);
		}
	}

	@Override
	public Api getApi(int what, Object... params) {
		HashMap<String, String> search_params = new HashMap<String, String>();
		// 传什么参数，get地址是什么
		switch (what) {
		case GETROOM:
			search_params.put("uid", "" + user.getId());
			search_params.put("pwd", password);
			search_params.put("action", "get");
			return new Api("get", Const.HOST + "room_enter.php", search_params);
		case ENTERROOM:
			search_params.put("uid", "" + user.getId());
			search_params.put("pid", mRoom.getId() + "");
			search_params.put("gid", mRoom.getGroup_id());
			search_params.put("openid", user.getOpenid());
			search_params.put("action", "enter");
			return new Api("get", Const.HOST + "room_enter.php", search_params);
		default:
			return null;
		}
	}

	@Override
	public boolean isCallApiSuccess(JSONObject result) {
		if (result == null) return false;
		boolean success = (Boolean) Api.getJSONValue(result, "success");
		if (!success)
			return false;
		return true;
	}

	@Override
	public void apiNetworkException(Exception e) {

	}

	@Override
	public String getCacheKey(int what, Object... params) {
		return null;
	}

	@Override
	public void handleResult(int what, JSONObject result, boolean isDone,
			Object... params) {
		if (isDone) {
			// 隐藏加载框
		}
		Util.hideLoading();
		switch (what) {
		case GETROOM:
			if (!isCallApiSuccess(result)) {
				Toast.makeText(EnterPwdActivity.this, "密码匹配失败！",
						Toast.LENGTH_LONG).show();
				return;
			}
			JSONObject obj = (JSONObject) Api.getJSONValue(result, "data");
			mRoom = new ChatRoom();
			mRoom.initChatRoom(obj);
			// 如果当前用户就是房主，直接进入房间
			if(mRoom.getCreater().toString().equalsIgnoreCase(user.getOpenid().toString())){
				Intent intent = new Intent();
				intent.setClass(EnterPwdActivity.this, ChatActivity.class);
				intent.putExtra("roomDetails", mRoom.getRoomDetails());
				startActivity(intent);
				finish();
				break;
			}
			if (!mRoom.getQuestion().equals("")) {
				mQuestion.setText(mRoom.getQuestion());
				mAnswer.setText("");
				mSafety.setVisibility(View.VISIBLE);
			} else {
				mSafety.setVisibility(View.GONE);
			}
			mEnter.setVisibility(View.VISIBLE);
			break;

		case ENTERROOM:
			if (!isCallApiSuccess(result)) {
				Toast.makeText(EnterPwdActivity.this, "进入房间失败",
						Toast.LENGTH_LONG).show();
				return;
			}
			Intent intent = new Intent();
			intent.setClass(EnterPwdActivity.this, ChatActivity.class);
			EMGroup group = null;
			try {
				group = EMGroupManager.getInstance().getGroupFromServer(mRoom.getGroup_id());
			} catch (EaseMobException e) {
				e.printStackTrace();
			}
			if(group != null){
				int nums = group.getMembers().size();
				mRoom.setNums(nums - 1);
			}else{
				mRoom.setNums(1);// 至少房间中有你
			}
			intent.putExtra("roomDetails", mRoom.getRoomDetails());
			intent.putExtra("from", ChatActivity.FROM_ENTER_PWD);
			startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public JSONObject appendResult(int what, JSONObject from, JSONObject to) {
		return null;
	}

	@Override
	public JSONObject prependResult(int what, JSONObject from, JSONObject to) {
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
