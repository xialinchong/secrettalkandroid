package com.yidianhulian.secrettalk.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CacheType;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.CallApiTask.FetchType;
import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.adapter.MemberAdapter;
import com.yidianhulian.secrettalk.model.ChatRoom;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.Util;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MemberActivity extends Activity implements CallApiListener{
	private static final int GET_MEMBERS = 1;
	private List<User> mMembers = new ArrayList<User>();
	private ListView mMembersView;
	private ChatRoom mRoom = new ChatRoom();
	private MemberAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_activity);
		getActionBar().setTitle("房间成员");
		getActionBar().setIcon(R.drawable.return_bar);
		getActionBar().setHomeButtonEnabled(true);
		
		mRoom.initRoomDetails(getIntent().getStringExtra("roomDetails"));
		mMembersView = (ListView) findViewById(R.id.list_members);
		mAdapter = new MemberAdapter(mMembers, MemberActivity.this, R.layout.member_item);
		mMembersView.setAdapter(mAdapter);
		
		Util.showLoading(MemberActivity.this, "正在获取成员信息...");
		loadData(GET_MEMBERS);
	}
	@Override
	protected void onDestroy() {
		Util.hideLoading();
		super.onDestroy();
	}
	
	private void loadData(int what) {
		CallApiTask.doCallApi(what, MemberActivity.this, MemberActivity.this, CacheType.REPLACE, FetchType.FETCH_API_ELSE_CACHE);
	}

	@Override
	public Api getApi(int what, Object... params) {
		Map<String, String> map = new HashMap<String, String>();
		switch (what) {
			case GET_MEMBERS:
				map.put("pid", mRoom.getId() + "");
				return new Api("get", Const.HOST + "room_members.php", map);
			default:
				return null;
		}
	}

	@Override
	public boolean isCallApiSuccess(JSONObject result) {
		if(result == null){
			return false;
		}
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
		if(isCallApiSuccess(result)){
			try {
				JSONArray users = (JSONArray) Api.getJSONValue(result, "data");
				for(int i = 0; i < users.length(); i++){
					JSONObject json = (JSONObject) users.get(i);
					User user = new User(json, true);
					mMembers.add(user);
					mAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			Toast.makeText(MemberActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
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
		if(item.getItemId() == android.R.id.home){
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
