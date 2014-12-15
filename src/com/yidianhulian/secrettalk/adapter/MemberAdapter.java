package com.yidianhulian.secrettalk.adapter;

import java.util.List;

import cn.sharesdk.framework.authorize.a;

import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.model.User;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberAdapter extends BaseAdapter {
	private List<User> mMembers;
	private Activity mActivity;
	private int mResid;

	public MemberAdapter(List<User> members, Activity activity, int resid) {
		this.mMembers = members;
		this.mActivity = activity;
		this.mResid = resid;
	}

	@Override
	public int getCount() {
		return mMembers.size();
	}

	@Override
	public Object getItem(int position) {
		return mMembers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mMembers.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		User user = mMembers.get(position);
		ViewHolder holder = null;
		if (convertView == null || convertView.getTag() == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mActivity).inflate(mResid, null);
			holder.memberName = (TextView) convertView
					.findViewById(R.id.member_name);
			holder.memberStatus = (TextView) convertView
					.findViewById(R.id.member_status);
			holder.memberFrom = (ImageView) convertView
					.findViewById(R.id.member_from);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.memberName.setText(user.getNick_name());
		if("qq".equalsIgnoreCase(user.getLogin_type())){
			holder.memberFrom.setImageResource(R.drawable.login_qq);
		}else{
			holder.memberFrom.setImageResource(R.drawable.login_sina);
		}
		holder.memberStatus.setText("离线");
		if (user.getStatus() == 1) {
			holder.memberStatus.setText("在线");
		}
		return convertView;
	}

	public static class ViewHolder {
		TextView memberName;
		TextView memberStatus;
		ImageView memberFrom;
	}

}
