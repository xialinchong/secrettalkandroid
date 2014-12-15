package com.yidianhulian.secrettalk.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.easemob.chat.EMGroupManager;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;

import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.utils.Util;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ExitTalkActivity extends Activity implements CallApiListener {

	public static final int ROOM_EXIT = 10000;
	public static final int EXIT_ROOM = 10001;
	public static final int DELETE_ROOM = 10002;

	private Button mCancel;
	private Button mOk;
	private TextView mTitle;
	private TextView mMessage;

	private int type = 0;
	private String uid;
	private String gid;
	private String pid;
	private String openid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit_talk);

		mCancel = (Button) findViewById(R.id.exit_cancel);
		mOk = (Button) findViewById(R.id.exit_ok);
		mTitle = (TextView) findViewById(R.id.exit_title);
		mMessage = (TextView) findViewById(R.id.exit_message);

		type = getIntent().getIntExtra("type", 0);
		uid = getIntent().getStringExtra("uid");
		gid = getIntent().getStringExtra("gid");
		pid = getIntent().getStringExtra("pid");
		openid = getIntent().getStringExtra("openid");
		if (type == EXIT_ROOM) {
			mTitle.setText("退出房间");
			mMessage.setText("小伙伴，您确定要退出该房间吗？");
		} else if (type == DELETE_ROOM) {
			mTitle.setText("删除房间");
			mMessage.setText("房主，您确定要删除该房间吗？");
		} else {
			finish();
		}

		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Util.showLoading(ExitTalkActivity.this, "处理中,请稍后...");
				loadData(type);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		super.onDestroy();
	}
	
	private void loadData(int what){
		if(Util.getAPNType(ExitTalkActivity.this) == -1){
			Util.hideLoading();
			Toast.makeText(ExitTalkActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
		}else{
			CallApiTask.doCallApi(what, ExitTalkActivity.this,
				ExitTalkActivity.this, null);
		}
	}

	@Override
	public Api getApi(int what, Object... params) {
		Map<String, String> map = new HashMap<String, String>();
		if (what == DELETE_ROOM) {
			map.put("type", "g");
		} else if (what == EXIT_ROOM) {
			map.put("type", "u");
		} else {
			return null;
		}
		map.put("pid", pid);
		map.put("gid", gid);
		map.put("uid", uid);
		map.put("openid", openid);
		return new Api("get", Const.HOST + "room_exit.php", map);
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
		// TODO Auto-generated method stub

	}

	@Override
	public String getCacheKey(int what, Object... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleResult(int what, JSONObject result, boolean isDone,
			Object... params) {
		Util.hideLoading();
		if (isCallApiSuccess(result)) {
			if(type == ExitTalkActivity.DELETE_ROOM){
				deleteGroup();
			}
			// setResult(type);
			finish();
			if(ChatActivity.activityInstance != null){
				ChatActivity.activityInstance.finish();
			}
		} else {
//			String msg = (String) Api.getJSONValue(result, "msg");
			Toast.makeText(ExitTalkActivity.this, "退出房间失败", Toast.LENGTH_SHORT)
					.show();
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

	public void deleteGroup() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					EMGroupManager.getInstance().exitAndDeleteGroup(gid);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							setResult(RESULT_OK);
//							ChatActivity.activityInstance.finish();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(getApplicationContext(),
									"解散群聊失败: " + e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}

			}
		});
	}
}
