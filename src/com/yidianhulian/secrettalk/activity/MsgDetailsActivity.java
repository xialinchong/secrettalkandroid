package com.yidianhulian.secrettalk.activity;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.network.h;

import com.easemob.chat.EMChatConfig;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Status;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.util.ImageUtils;
import com.easemob.util.PathUtil;
import com.yidianhulian.secrettalk.R.string;
import com.yidianhulian.secrettalk.adapter.VoicePlayClickListener;
import com.yidianhulian.secrettalk.task.LoadImageTask;
import com.yidianhulian.secrettalk.task.LoadLocalBigImgTask;
import com.yidianhulian.secrettalk.utils.SmileUtils;
import com.yidianhulian.secrettalk.video.util.ImageCache;
import com.yidianhulian.secrettalk.widget.PhotoView;
import com.yidianhulian.secrettalk.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.BufferType;

public class MsgDetailsActivity extends Activity {
	/** 文本消息  */
	public static final int MESSAGE_TYPE_TXT = 1;
	/** 图片消息  */
	public static final int MESSAGE_TYPE_IMAGE = 2;
	/** 录音消息  */
	public static final int MESSAGE_TYPE_VOICE = 3;
	
	Intent intent = null;

	private int mClose = 0;
	private int mPosition;
	private int mMsgType;
	
	public boolean mIsDelete = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msg_details);

		getActionBar().setTitle(R.string.look_msg);
		getActionBar().setIcon(R.drawable.return_bar);
		getActionBar().setHomeButtonEnabled(true);

		initView();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			deleteMsg();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mClose != 0) {
			deleteMsg();
		} else {
			mClose = 1;
		}
	}

	public void deleteMsg() {
		if(mIsDelete){
			setResult(ChatActivity.RESULT_CODE_DELETE,
				new Intent().putExtra("position", mPosition));
		}
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			deleteMsg();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initView(){
		intent = getIntent();
		MsgView msgView = new MsgView();
		Bundle bundle = intent.getExtras();
		
		mPosition = bundle.getInt("position");
		mMsgType = bundle.getInt("type");
		
		if(mMsgType == MESSAGE_TYPE_TXT){ // 文本消息
			msgView.textView = (TextView) findViewById(R.id.msg_text);
			msgView.textView.setVisibility(View.VISIBLE);
			Spannable span = SmileUtils.getSmiledText(MsgDetailsActivity.this, bundle.getString("msg") );
			msgView.textView.setText(span, BufferType.SPANNABLE);// 设置内容
			
		}else if(mMsgType == MESSAGE_TYPE_IMAGE){ // 图片消息
			msgView.pictureView = (PhotoView) findViewById(R.id.msg_image);
			msgView.pictureView.setVisibility(View.VISIBLE);
			
			Uri uri = getIntent().getParcelableExtra("uri");
			boolean showAvator = getIntent().getExtras().getBoolean("showAvator");
			String remotepath = getIntent().getExtras().getString("remotepath");
			String secret = getIntent().getExtras().getString("secret");
			String localFilePath = "";
			if(uri != null &&  new File(uri.getPath()).exists()){ // 直接读取本地文件
				Bitmap bitmap = ImageCache.getInstance().get(uri.getPath());
				if(bitmap == null){
					LoadLocalBigImgTask task = new LoadLocalBigImgTask(this, uri.getPath(), msgView.pictureView, ImageUtils.SCALE_IMAGE_WIDTH, ImageUtils.SCALE_IMAGE_HEIGHT);
					if(android.os.Build.VERSION.SDK_INT > 10){
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}else{
						task.execute();
					}
				}else{
					msgView.pictureView.setImageBitmap(bitmap);
				}
			}else if(remotepath != null ){ // 从服务器上下载图片
				Toast.makeText(MsgDetailsActivity.this, "图片获取中...", Toast.LENGTH_SHORT).show();
				Map<String, String> maps = new HashMap<String, String>();
				String accessToken = EMChatManager.getInstance().getAccessToken();
				maps.put("Authorization", "Bearer" + accessToken);
				maps.put("Accept", "application/octet-stream");
				if(!TextUtils.isEmpty(secret)){
					maps.put("share-secret", secret);
				}
				
				downloadImage(msgView, remotepath, localFilePath, maps, showAvator);
			}
			
		}else if(mMsgType == MESSAGE_TYPE_VOICE){ // 录音消息
			msgView.voiceView = (ImageView) findViewById(R.id.msg_voice);
			msgView.voiceView.setVisibility(View.VISIBLE);
			EMMessage message = (EMMessage) getIntent().getExtras().get("message");
			VoicePlayClickListener voice = new VoicePlayClickListener(message, msgView.voiceView, MsgDetailsActivity.this);
			msgView.voiceView.setOnClickListener(voice);
			voice.auto_play();
		}
		

	}
	
	class MsgView{
		public TextView textView;
		public PhotoView pictureView;
		public ImageView voiceView;
	}
	
	/**
	 * 下载图片
	 * @param remoteFilePath
	 * @param headers
	 */
	private void downloadImage(final MsgView msgView, final String remoteFilePath,String localFilePath, final Map<String, String> headers, boolean showAvator){
		
		if(!showAvator){
			if(remoteFilePath.contains("/")){
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath()+"/"+remoteFilePath.substring(remoteFilePath.lastIndexOf("/")+1);
			}else{
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath()+"/"+remoteFilePath;
			}
		}else{
			if(remoteFilePath.contains("/")){
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath()+"/"+remoteFilePath.substring(remoteFilePath.lastIndexOf("/")+1);
			}else{
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath()+"/"+remoteFilePath;
			}
		}
		final String filePath = localFilePath;
		final HttpFileManager httpFileMgr = new HttpFileManager(this, EMChatConfig.getInstance().getStorageUrl());
		final CloudOperationCallback callback = new CloudOperationCallback() {
			
			@Override
			public void onSuccess(String arg0) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						DisplayMetrics metrics = new DisplayMetrics();
						getWindowManager().getDefaultDisplay().getMetrics(metrics);
						int screenWidth = metrics.widthPixels;
						int screenHeight = metrics.heightPixels;
						Bitmap bitmap = ImageUtils.decodeScaleImage(filePath, screenWidth, screenHeight);
						if(bitmap != null){
							msgView.pictureView.setImageBitmap(bitmap);
							ImageCache.getInstance().put(filePath, bitmap);
						}
					}
				});
				
			}
			
			@Override
			public void onProgress(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(String arg0) {
				File file = new File(filePath);
				if(file.exists()){
					file.delete();
				}
				
			}
		};
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				httpFileMgr.downloadFile(remoteFilePath, filePath, EMChatConfig.getInstance().APPKEY, headers, callback);
			}
		}).start();
	}

	
}
