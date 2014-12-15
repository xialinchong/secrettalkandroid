package com.yidianhulian.secrettalk.activity;

import com.yidianhulian.secrettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class NoNetActivity extends Activity {
	private ImageButton mImageBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_net);
		getActionBar().hide();
		mImageBtn = (ImageButton) findViewById(R.id.refresh_net);
		
		mImageBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NoNetActivity.this, Splash.class);
				startActivity(intent);
				finish();
			}
		});
	}
}
