package com.yidianhulian.secrettalk.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.R.layout;
import com.yidianhulian.secrettalk.YDSecretTalkApplication;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.Util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LaunchActivity extends Activity implements CallApiListener {

	private static final int GETPWD = 1;
	private static final int ADDPWD = 2;

	YDSecretTalkApplication mApp;

	Button mAgainGet = null;
	Button mCopyPwd = null;
	Button mCreateRoom = null;
	EditText mRoomPwd = null;
	EditText mRoomName = null;
	EditText mQuestion = null;
	EditText mAnswer = null;
	// ImageButton mQQbtn = null;
	// ImageButton mSinaBtn = null;
	// EditText mVIPID = null;
	String mPasswrod = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launch);

		getActionBar().setTitle(R.string.create_room);
		getActionBar().setIcon(R.drawable.return_bar);
		getActionBar().setHomeButtonEnabled(true);

		mApp = (YDSecretTalkApplication) getApplication();

		// mQQbtn = (ImageButton) findViewById(R.id.vip_qq);
		// mSinaBtn = (ImageButton) findViewById(R.id.vip_sina);
		// mVIPID = (EditText) findViewById(R.id.vip_id);
		mAgainGet = (Button) findViewById(R.id.again_get);
		mCopyPwd = (Button) findViewById(R.id.copy_pwd);
		mCreateRoom = (Button) findViewById(R.id.create_room);
		mRoomPwd = (EditText) findViewById(R.id.enter_pwd_launch);
		mRoomName = (EditText) findViewById(R.id.room_name);
		mQuestion = (EditText) findViewById(R.id.room_question);
		mAnswer = (EditText) findViewById(R.id.room_answer);

		mAgainGet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.closeKeyboard(getWindow().peekDecorView(),
						getApplication());
				Util.showLoading(LaunchActivity.this, "密码生成中。。。");
				loadData(GETPWD);
			}
		});

		mCopyPwd.setVisibility(View.GONE);
		mCopyPwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.closeKeyboard(getWindow().peekDecorView(),
						getApplication());
				copyPwd(mPasswrod, LaunchActivity.this);
			}
		});

		mCreateRoom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = "";
				if (mRoomName.getText().toString().equals("")) {
					msg = "房间名称不能为空！";
				} else if (mPasswrod.equals("")) {
					msg = "房间密码没有生成，请单击刷新按钮！";
				} else if (mQuestion.getText().toString().equals("")) {
					msg = "安全问题不能为空！";
				} else if (mAnswer.getText().toString().equals("")) {
					msg = "安全回答不能为空！";
				}
				if (!msg.equals("")) {
					Toast.makeText(LaunchActivity.this, msg, Toast.LENGTH_SHORT)
							.show();
				} else {
					Util.showLoading(LaunchActivity.this, "房间正在创建中...");
					loadData(ADDPWD);
				}
			}
		});
		Util.showLoading(LaunchActivity.this, "密码生成中。。。");
		loadData(GETPWD);
		
	}
	
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		super.onDestroy();
	}
	
	private void loadData(int what){
		if(Util.getAPNType(LaunchActivity.this) == -1){
			Util.hideLoading();
			Toast.makeText(LaunchActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
		}else{
			CallApiTask.doCallApi(what, LaunchActivity.this, LaunchActivity.this,
				null);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.launch_menu, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Api getApi(int what, Object... params) {
		Map<String, String> map = new HashMap<String, String>();
		User user = mApp.loginUser();
		switch (what) {
		case GETPWD:
			map.put("user_id", user.getId() + "");
			return new Api("get", Const.HOST + "get_password.php", map);
		case ADDPWD:
			map.put("pwd", mPasswrod);
			map.put("uid", user.getId() + "");
			map.put("name", mRoomName.getText().toString());// 群名称
			map.put("q", mQuestion.getText().toString());
			map.put("a", mAnswer.getText().toString());
			map.put("type", "group");// 聊天类型
			map.put("openid", user.getOpenid()); // 群管理员
			map.put("desc", ""); // 群描述
//			map.put("public", "0");// 是否公开
//			map.put("approval", "0");// 加入时是否需要批准
			return new Api("get", Const.HOST + "room_create.php", map);
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
		Util.hideLoading();
		switch (what) {
		case GETPWD:
			if (isCallApiSuccess(result)) { // 获取密码成功
				JSONObject data = (JSONObject) Api.getJSONValue(result, "data");
				try {
					mPasswrod = data.getString("password");
					mRoomPwd.setText(mPasswrod);
					mCopyPwd.setVisibility(View.VISIBLE);
				} catch (JSONException e) {
					e.printStackTrace();
					mCopyPwd.setVisibility(View.GONE);
					Toast.makeText(LaunchActivity.this, "系统异常！",
							Toast.LENGTH_SHORT).show();
				}
			} else {// 获取密码失败
				mCopyPwd.setVisibility(View.GONE);
//				String msg = (String) Api.getJSONValue(result, "msg");
				Toast.makeText(LaunchActivity.this, "获取密码失败", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case ADDPWD:
			if (isCallApiSuccess(result)) {
				Toast.makeText(LaunchActivity.this, "邀请码申请成功",
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
//				String msgString = (String) Api.getJSONValue(result, "msg");
				Toast.makeText(LaunchActivity.this, "添加房间失败",
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public JSONObject appendResult(int what, JSONObject from, JSONObject to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject prependResult(int what, JSONObject from, JSONObject to) {
		// TODO Auto-generated method stub
		return null;
	}

	public void copyPwd(String content, Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
		Toast.makeText(context, "密码已经复制到粘贴板中。", Toast.LENGTH_SHORT).show();
	}

}
