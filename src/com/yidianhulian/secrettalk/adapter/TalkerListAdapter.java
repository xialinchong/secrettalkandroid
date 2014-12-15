package com.yidianhulian.secrettalk.adapter;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.model.ChatRoom;
import com.yidianhulian.secrettalk.model.User;

public class TalkerListAdapter extends BaseAdapter {

	private List<User> mTalkers;
	private List<ChatRoom> mRooms;
	private Activity mContext;
	private int mResid;

	// public TalkerListAdapter(List<User> talkers, Activity context, int resid)
	// {
	//
	// this.mTalkers = talkers;
	// this.mResid = resid;
	// this.mContext = context;
	// }
	public TalkerListAdapter(List<ChatRoom> rooms, Activity context, int resid) {

		this.mRooms = rooms;
		this.mResid = resid;
		this.mContext = context;
	}

	@Override
	public int getCount() {
		// return mTalkers.size();
		return mRooms.size();
	}

	@Override
	public Object getItem(int position) {
		// return mTalkers.get(position);
		return mRooms.get(position);
	}

	@Override
	public long getItemId(int position) {
		// return mTalkers.get(position).getId();
		return mRooms.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// guoxingcai 2014-11-17
		ChatRoom room = mRooms.get(position);
		TalkerPlaceHolder placeHolder;
		if (convertView == null
				|| /* empty view */convertView.getTag() == null) {
			convertView = LayoutInflater.from(mContext).inflate(mResid, null);
			placeHolder = new TalkerPlaceHolder();
			placeHolder.talker_name = (TextView) convertView
					.findViewById(R.id.talker_name);

			 placeHolder.msg_has_news = (TextView) convertView.findViewById(R.id.msg_has_news);
			 placeHolder.msg_no_talker = (TextView) convertView.findViewById(R.id.msg_no_talker);

			convertView.setTag(placeHolder);

		} else {
			placeHolder = (TalkerPlaceHolder) convertView.getTag();
		}
		placeHolder.talker_name.setText(room.getName());
		if (room.getPlayer().size() <= 0) {
			placeHolder.msg_has_news.setVisibility(View.GONE);
			placeHolder.msg_no_talker.setVisibility(View.VISIBLE);
//			convertView.findViewById(R.id.msg_no_talker).setVisibility(View.VISIBLE);
//			convertView.findViewById(R.id.msg_has_news).setVisibility(View.GONE);
		} else {
			room.setNews(getUnReadMsg(room.getGroup_id()));
			if(room.getNews() == 0){
				placeHolder.msg_has_news.setVisibility(View.GONE);
//				convertView.findViewById(R.id.msg_has_news).setVisibility(View.GONE);
			}else{
				int news = room.getNews();
				if(news >= 99){
					news = 99;
					placeHolder.msg_has_news.setBackgroundResource(R.drawable.news_l);
				}else{
					placeHolder.msg_has_news.setBackgroundResource(R.drawable.news);
				}
				placeHolder.msg_has_news.setVisibility(View.VISIBLE);
				placeHolder.msg_has_news.setText(""+news);
				
//				convertView.findViewById(R.id.msg_has_news).setVisibility(View.VISIBLE);
//				((TextView)convertView.findViewById(R.id.msg_has_news)).setText(room.getNews()+"");
			}
			placeHolder.msg_no_talker.setVisibility(View.GONE);
//			convertView.findViewById(R.id.msg_no_talker).setVisibility(View.GONE);
		}
		return convertView;

		// User user = mTalkers.get(position);
		// TalkerPlaceHolder placeHolder;
		//
		// if (convertView == null || /* empty view */convertView.getTag() ==
		// null) {
		// convertView = LayoutInflater.from(mContext).inflate(mResid, null);
		// placeHolder = new TalkerPlaceHolder();
		// placeHolder.talker_name = (TextView) convertView
		// .findViewById(R.id.talker_name);
		//
		// placeHolder.has_new_msg = (ImageView) convertView
		// .findViewById(R.id.has_new_msg);
		// placeHolder.has_destory_msg = (ImageView) convertView
		// .findViewById(R.id.has_destory_msg);
		//
		// convertView.setTag(placeHolder);
		//
		// } else {
		// placeHolder = (TalkerPlaceHolder) convertView.getTag();
		// }
		// placeHolder.talker_name.setText(user.getNickname());
		//
		//
		// EMConversation conversation = EMChatManager.getInstance()
		// .getConversation(user.getOpenid());
		//
		// if (conversation.getUnreadMsgCount() > 0) {
		// placeHolder.has_new_msg.setVisibility(View.VISIBLE);
		// } else {
		// placeHolder.has_new_msg.setVisibility(View.INVISIBLE);
		// }
		//
		// EMMessage msg = conversation.getLastMessage();
		// if (msg != null && msg.isAcked) {
		// placeHolder.has_destory_msg.setVisibility(View.VISIBLE);
		// } else {
		// placeHolder.has_destory_msg.setVisibility(View.INVISIBLE);
		// }
		// return convertView;
	}

	class TalkerPlaceHolder {
		TextView talker_name;
		TextView msg_no_talker;
		TextView msg_has_news;
//		ImageView has_destory_msg;
	}
	
	private int getUnReadMsg(String gid){
		int nums = 0;
		EMConversation conversation = EMChatManager.getInstance().getConversation(gid);;
		Iterator<EMMessage> Imsg = conversation.getAllMessages().iterator();
		while(Imsg.hasNext()){
			EMMessage msg = Imsg.next();
			if(msg.direct == EMMessage.Direct.RECEIVE){
				nums ++;
			}
		}
		return nums;
	}

}
