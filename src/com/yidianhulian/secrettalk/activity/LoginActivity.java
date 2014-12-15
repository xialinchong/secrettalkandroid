package com.yidianhulian.secrettalk.activity;


import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import cn.sharesdk.onekeyshare.ThirdLogin;

import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.utils.Util;

public class LoginActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		setContentView(R.layout.login);

		ImageButton mQQBtn = (ImageButton) findViewById(R.id.qq_btn);
		ImageButton mSinaBtn = (ImageButton) findViewById(R.id.sina_btn);
		mQQBtn.setOnClickListener(this);
		mSinaBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		ThirdLogin tLogin = null;
		Util.showLoading(LoginActivity.this, "第三方登录中...");
		switch (v.getId()) {
		case R.id.qq_btn:
			tLogin = new ThirdLogin(LoginActivity.this, "qq");
			tLogin.startThirdLogin();
			break;
		case R.id.sina_btn:
			tLogin = new ThirdLogin(LoginActivity.this, "sina");
			tLogin.startThirdLogin();
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)  
	       {    
	           Util.exitBy2Click(LoginActivity.this);      //调用双击退出函数  
	       }  
		return false;
	}
	
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		super.onDestroy();
	}
	
}
