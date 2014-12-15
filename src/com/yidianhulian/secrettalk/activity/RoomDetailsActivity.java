package com.yidianhulian.secrettalk.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import cn.sharesdk.framework.authorize.a;

import com.baidu.platform.comapi.map.q;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.model.ChatRoom;
import com.yidianhulian.secrettalk.utils.Util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RoomDetailsActivity extends Activity implements CallApiListener{
	
	public static final int LOOK_ROOM = 101;

	private ChatRoom mRoom = new ChatRoom();
	private EditText mPasswrod;
	private EditText mRoomName;
	private EditText mQuestion;
	private EditText mAnswer;
	
	private Button mSetBtn;
	private Button mCopyBtn;
	
	private boolean mIsSave = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_details);
		mRoom.initRoomDetails(getIntent().getStringExtra("roomDetails"));
		getActionBar().setTitle(mRoom.getName() + " 房间详情");
		getActionBar().setIcon(R.drawable.return_bar);
		getActionBar().setHomeButtonEnabled(true);
		
		mPasswrod = (EditText) findViewById(R.id.edit_enter_pwd_launch);
		mRoomName = (EditText) findViewById(R.id.edit_room_name);
		mQuestion = (EditText) findViewById(R.id.edit_room_question);
		mAnswer = (EditText) findViewById(R.id.edit_room_answer);
		
		mSetBtn = (Button) findViewById(R.id.edit_set_room);
		mCopyBtn = (Button) findViewById(R.id.edit_copy_password);
		
		mPasswrod.setText(mRoom.getPassword());
		mRoomName.setText(mRoom.getName());
		mQuestion.setText(mRoom.getQuestion());
		mAnswer.setText(mRoom.getAnswer());
		
		mCopyBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				copyPwd(mRoom.getPassword(), RoomDetailsActivity.this);
			}
		});
		
		mSetBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String msg = "";
				String question = mQuestion.getText().toString();
				String answer = mAnswer.getText().toString();
				String name = mRoomName.getText().toString();
				if(name.equals("") || name.length() == 0){
					msg = "房间名称不能为空！";
				}else if("".equals(question) && ! "".equals(answer)){
					msg = "安全问题不能为空！";
				}else if("".equals(answer) && ! "".equals(question)){
					msg = "问题答案不能为空！";
				}
				if(mRoom.getName().equals(name) && mRoom.getQuestion().equals(question) && mRoom.getAnswer().equals(answer)){
					msg = "没有新信息需要修改";
				}
				if(!"".equals(msg)){
					Toast.makeText(RoomDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
				}else{
					Util.showLoading(RoomDetailsActivity.this, "房间正在修改中...");
					loadData(0);
				}
				
				
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		super.onDestroy();
	}
	private void loadData(int what){
		if(Util.getAPNType(RoomDetailsActivity.this) == -1){
			Util.hideLoading();
			Toast.makeText(RoomDetailsActivity.this, "无网络连接", Toast.LENGTH_SHORT).show();
		}else{
			CallApiTask.doCallApi(what, RoomDetailsActivity.this,
					RoomDetailsActivity.this, null);
		}
		
	}
	
	public void copyPwd(String content, Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
		Toast.makeText(context, "密码已经复制到粘贴板中。", Toast.LENGTH_SHORT).show();
	}

	@Override
	public Api getApi(int what, Object... params) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("p_id", mRoom.getId()+"");
		map.put("r_name", mRoomName.getText().toString());
		map.put("q", mQuestion.getText().toString());
		map.put("a", mAnswer.getText().toString());
		return new Api("get", Const.HOST + "room_edit.php", map);
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
		if (isCallApiSuccess(result)) { // 修改成功
			mRoom.setName(mRoomName.getText().toString());
			mRoom.setQuestion(mQuestion.getText().toString());
			mRoom.setAnswer(mAnswer.getText().toString());
			mIsSave = true;
			try {
				EMGroupManager.getInstance().changeGroupName(mRoom.getGroup_id(), mRoom.getName());
			} catch (EaseMobException e) {
				e.printStackTrace();
			}
			Toast.makeText(RoomDetailsActivity.this, "修改成功", Toast.LENGTH_SHORT)
			.show();
			getActionBar().setTitle(mRoom.getName() + " 房间详情");
		}else{
//			String msg = (String) Api.getJSONValue(result, "msg");
			mIsSave = false;
			Toast.makeText(RoomDetailsActivity.this, "修改失败", Toast.LENGTH_SHORT)
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
	
	public void refreshRoomDetail(){
		Intent intent = new Intent();
		intent.putExtra("name", mRoom.getName());
		intent.putExtra("q", mRoom.getQuestion());
		intent.putExtra("a", mRoom.getAnswer());
		setResult(RoomDetailsActivity.LOOK_ROOM, intent);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 && mIsSave) {
			refreshRoomDetail();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			refreshRoomDetail();
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
