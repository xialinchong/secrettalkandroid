package com.yidianhulian.secrettalk.utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.easemob.chat.EMChatManager;

public class VoiceCallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(!intent.getAction().equals(EMChatManager.getInstance().getIncomingVoiceCallBroadcastAction()))
			return;
		
		String from = intent.getStringExtra("from");
		context.startActivity(new Intent(context, null).
				putExtra("username", from).putExtra("isComingCall", true).
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

}
