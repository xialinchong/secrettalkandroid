package com.yidianhulian.secrettalk.activity;

import com.easemob.chat.EMChatManager;
import com.yidianhulian.framework.db.KVHandler;
import com.yidianhulian.secrettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LogoutActivity extends Activity {

	private TextView mTitle;
	private TextView mMessage;
	private Button mCancle;
	private Button mOk;
	private KVHandler mKvh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logout);
		mKvh = new KVHandler(getApplicationContext(), "secrettalk", null, 1);
		
		mTitle = (TextView) findViewById(R.id.logout_title);
		mMessage = (TextView) findViewById(R.id.logout_message);
		mOk = (Button) findViewById(R.id.logout_ok);
		mCancle = (Button) findViewById(R.id.logout_cancel);
		
		mTitle.setText("退出登录");
		mMessage.setText("你确定要退出登录吗？");
		
		mCancle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				mKvh.setValue("loginUser", "");
				EMChatManager.getInstance().logout();// 退出环信
				intent.setClass(LogoutActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
				if(TalkerListActivity.activityInstance != null){
					TalkerListActivity.activityInstance.finish();
				}
			}
		});
	}
}
