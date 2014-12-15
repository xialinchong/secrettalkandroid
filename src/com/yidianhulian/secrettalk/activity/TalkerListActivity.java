package com.yidianhulian.secrettalk.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ad.android.sdk.api.AdSdk;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.GroupReomveListener;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.framework.db.KVHandler;
import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.YDSecretTalkApplication;
import com.yidianhulian.secrettalk.adapter.TalkerListAdapter;
import com.yidianhulian.secrettalk.model.ChatRoom;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.Util;

public class TalkerListActivity extends Activity implements CallApiListener {

	private List<User> mTalkers = new ArrayList<User>();
	private List<ChatRoom> mRooms = new ArrayList<ChatRoom>();
	private RelativeLayout mNoTalker = null;
	private RelativeLayout mNoNet = null;
	private ListView mTalkerList = null;
	private TalkerListAdapter mAdapter;
	private KVHandler mKvh;
	private GroupListener groupListener;
	public static TalkerListActivity activityInstance = null;
	
	YDSecretTalkApplication mApp;
	User user;

	private static final int LOAD_TALKER = 0;
	private static final int START_CHAT = 1;
	private static final int LOAD_ROOM = 2;

	private NewMessageBroadcastReceiver msgReceiver;

	// private String toChatUsername;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talker_list);
		mKvh = new KVHandler(getApplicationContext(), "secrettalk", null, 1);
		mApp = (YDSecretTalkApplication) getApplication();
		user = mApp.loginUser();
		if(user == null){
			Intent intent = new Intent(TalkerListActivity.this, LoginActivity.class);
			startActivity(intent);
		}

		activityInstance = this;
		groupListener = new GroupListener();
		EMGroupManager.getInstance().addGroupChangeListener(groupListener);
		
		mNoNet = (RelativeLayout) findViewById(R.id.no_net_msg);
		mNoTalker = (RelativeLayout) findViewById(R.id.no_talker);
		mTalkerList = (ListView) findViewById(R.id.talker_list);

		// mAdapter = new TalkerListAdapter(mTalkers, TalkerListActivity.this,
		// R.layout.talker_item);
		// mTalkerList.setAdapter(mAdapter);

		// guoxingcai 2014-11-17
		mAdapter = new TalkerListAdapter(mRooms, TalkerListActivity.this,
				R.layout.talker_item);
		mTalkerList.setAdapter(mAdapter);

		mTalkerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.setClass(TalkerListActivity.this, ChatActivity.class);
				intent.putExtra("roomDetails", mRooms.get(position).getRoomDetails());
				intent.putExtra("from", ChatActivity.FROM_TALKER_LIST);
				startActivity(intent);
			}
		});

		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager
				.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		getApplicationContext().registerReceiver(msgReceiver, intentFilter);

		EMChat.getInstance().setAppInited();
		
		// 鹰眼广告
		AdSdk ad = AdSdk.getInstace(getApplicationContext());// 获取广告实例
		ad.setBannerAdPid("39fabd95b405df2b8b81a2d52bc7ec1a");// 设置banner广告账号
		
		FrameLayout layout = new FrameLayout(getApplicationContext());
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.TOP | Gravity.CENTER;// 广告显示的位置
		((ViewGroup)findViewById(R.id.advert)).addView(layout, params);// 将layout追加到当前activity的LinearLayout
//		addContentView(layout, params);
		ad.showBannerAd(layout);// 显示 banner 广告
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.enter_talk_menu, menu);
		getMenuInflater().inflate(R.menu.add_talker_menu, menu);
		getMenuInflater().inflate(R.menu.logout, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		if (item.getItemId() == R.id.add_talker) {
			intent.setClass(TalkerListActivity.this, LaunchActivity.class);
			TalkerListActivity.this.startActivity(intent);
		} else if (item.getItemId() == R.id.enter_talk) {
			intent.setClass(TalkerListActivity.this, EnterPwdActivity.class);
			startActivity(intent);
		} else if (item.getItemId() == R.id.logout_menu){
			intent.setClass(TalkerListActivity.this, LogoutActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// if (mApp.fromUser != null && !mApp.fromUser.isEmpty()) {
		// loadData(START_CHAT);
		// mApp.fromUser.clear();
		// } else {
		// loadData(LOAD_TALKER);
		// }
		// guoxingcai 2014-12-02
		Util.showLoading(TalkerListActivity.this, "加载中...");
		loadData(LOAD_ROOM);
	}
	
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		activityInstance = null;
		getApplicationContext().unregisterReceiver(msgReceiver);
		super.onDestroy();
	}

	private void loadData(int what) {
		if(Util.getAPNType(TalkerListActivity.this) == -1){
			Util.hideLoading();
			mNoNet.setVisibility(View.VISIBLE);
		}else{
			mNoNet.setVisibility(View.GONE);
			CallApiTask.doCallApi(what, TalkerListActivity.this,
					TalkerListActivity.this, CacheType.REPLACE,
					FetchType.FETCH_API_ELSE_CACHE);
		}
	}

	@Override
	public Api getApi(int what, Object... params) {
		HashMap<String, String> search_params = new HashMap<String, String>();
		// 传什么参数，get地址是什么
		// search_params.put("uid", user.getId() + "");
		// if (what == START_CHAT) {
		// search_params.put("openid", mApp.fromUser.get(1));
		// }
		// return new Api("get", Const.HOST + "load_talkers.php",
		// search_params);
		// guoxingcai 2014-11-17
		search_params.put("uid", user.getId() + "");
		return new Api("get", Const.HOST + "room_load.php", search_params);
	}

	@Override
	public boolean isCallApiSuccess(JSONObject result) {
		if (result == null)
			return false;
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
		return "talkerList";
	}

	@Override
	public void handleResult(int what, JSONObject result, boolean isDone,
			Object... params) {
		if (isDone) {
			// 隐藏加载框
			Util.hideLoading();
		}
		switch (what) {
		case LOAD_TALKER:
//			try {
//				JSONArray users = (JSONArray) Api.getJSONValue(result, "data");
//				mTalkers.clear();
//				if (isCallApiSuccess(result) || users.length() > 0) {
//					mNoTalker.setVisibility(View.GONE);
//					for (int i = 0; i < users.length(); i++) {
//						JSONObject obj = (JSONObject) users.get(i);
//						User user = new User(obj);
//						mTalkers.add(user);
//					}
//				} else {
//					mNoTalker.setVisibility(View.VISIBLE);
//				}
//
//				mAdapter.notifyDataSetChanged();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
			break;
		case START_CHAT:
//			JSONArray users = (JSONArray) Api.getJSONValue(result, "data");
//			try {
//				if (isCallApiSuccess(result) || users.length() > 0) {
//					JSONObject obj = (JSONObject) users.get(0);
//					User fromuser = new User(obj);
//					Intent intent = new Intent();
//					intent.setClass(TalkerListActivity.this, ChatActivity.class);
//					intent.putExtra("User", fromuser.toString());
//					startActivity(intent);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}

			break;
		case LOAD_ROOM:
			// guoxingcai 2014-11-17
			if (isCallApiSuccess(result)) {
				JSONObject rooms = (JSONObject) Api
						.getJSONValue(result, "data");
				mRooms.clear();
				for (Iterator iter = rooms.keys(); iter.hasNext();) { // 先遍历整个
					
					// people  对象
					String key = (String) iter.next();
					try {
						JSONObject room = rooms.getJSONObject(key);
						ChatRoom chatRoom = new ChatRoom();
						chatRoom.initChatRoom(room);
						mRooms.add(chatRoom);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (mRooms.size() > 0) {
					mNoTalker.setVisibility(View.GONE);
				} else {
					mNoTalker.setVisibility(View.VISIBLE);
				}
				mAdapter.notifyDataSetChanged();
			} else {
				mNoTalker.setVisibility(View.VISIBLE);
			}

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

	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看

			// 消息id
			String username = intent.getStringExtra("from");
			String msgId = intent.getStringExtra("msgid");
			// 收到这个广播的时候，message已经在db和内存里了，可以通过id获取mesage对象
			EMMessage message = EMChatManager.getInstance().getMessage(msgId);

			// EMConversation conversation =
			// EMChatManager.getInstance().getConversation(username);
			// conversation.addMessage(message);

			// 注销广播，否则在ChatActivity中会收到这个广播
			loadData(LOAD_ROOM);
//			loadData(LOAD_TALKER);
			abortBroadcast();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)  
	       {    
	           Util.exitBy2Click(TalkerListActivity.this);      //调用双击退出函数  
	       }  
		return false;
	}
	
	class GroupListener extends GroupReomveListener {

		@Override
		public void onUserRemoved(final String groupId, String groupName) {
//			runOnUiThread(new Runnable() {
//				public void run() {
//					if (toChatUsername.equals(groupId)) {
//						Toast.makeText(ChatActivity.this, "你被群创建者从此群中移除", Toast.LENGTH_SHORT).show();
//						finish();
//					}
//				}
//			});
		}

		@Override
		public void onGroupDestroy(final String groupId, final String groupName) {
			// 群组解散正好在此页面，提示群组被解散，并finish此页面
			runOnUiThread(new Runnable() {
				public void run() {
//					if (toChatUsername.equals(groupId)) {
//						Toast.makeText(ChatActivity.this, "“"+mRoom.getName()+"” 房间被房主关闭", Toast.LENGTH_SHORT).show();
//						finish();
//					}
					if(groupName != null && !"".equals(groupName)){
						Toast.makeText(TalkerListActivity.this, "“"+groupName+"” 房间被房主关闭", Toast.LENGTH_SHORT).show();
					}
					loadData(LOAD_ROOM);
				}
			});
		}

	}

}
