package com.yidianhulian.secrettalk.task;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.util.ImageUtils;
import com.yidianhulian.secrettalk.activity.ChatActivity;
import com.yidianhulian.secrettalk.activity.MsgDetailsActivity;
import com.yidianhulian.secrettalk.activity.ShowBigImage;
import com.yidianhulian.secrettalk.adapter.MessageAdapter;
import com.yidianhulian.secrettalk.utils.CommonUtils;
import com.yidianhulian.secrettalk.video.util.ImageCache;

public class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {
    private ImageView iv = null;
    private TextView show = null;
    String localFullSizePath = null;
    String thumbnailPath = null;
    String remotePath = null;
    EMMessage message = null;
    ChatType chatType;
    Activity activity;
    int position;
    private EMConversation conversation;
    private String username;
    MessageAdapter adapter;

    @Override
    protected Bitmap doInBackground(Object... args) {
        thumbnailPath = (String) args[0];
        localFullSizePath = (String) args[1];
        remotePath = (String) args[2];
        chatType = (ChatType) args[3];
        iv = (ImageView) args[4];
        // if(args[2] != null) {
        activity = (Activity) args[5];
        // }
        message = (EMMessage) args[6];
        position = (Integer) args[7];
        adapter = (MessageAdapter) args[8];
        username = (String) args[9];
        show = (TextView) args[10];
        conversation = EMChatManager.getInstance().getConversation(username);
        File file = new File(thumbnailPath);
        if (file.exists()) {
            return ImageUtils.decodeScaleImage(thumbnailPath, 160, 160);
        } else {
            if (message.direct == EMMessage.Direct.SEND) {
                return ImageUtils.decodeScaleImage(localFullSizePath, 160, 160);
            } else {
                return null;
            }
        }
        

    }

    protected void onPostExecute(Bitmap image) {
        if (image != null) {
//            iv.setImageBitmap(image);
            ImageCache.getInstance().put(thumbnailPath, image);
            iv.setClickable(true);
            iv.setTag(thumbnailPath);
//            iv.setOnClickListener(new View.OnClickListener() {
            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	if(message.status == EMMessage.Status.SUCCESS && message.direct == EMMessage.Direct.SEND){
                		conversation.removeMessage(message.getMsgId());
                		adapter.refresh();
                	}else{
                		if (thumbnailPath != null) {
                        	Intent intent = new Intent(activity,MsgDetailsActivity.class);
                            File file = new File(localFullSizePath);
                            if (file.exists()) {
                                Uri uri = Uri.fromFile(file);
                                intent.putExtra("uri", uri);
                            } else {
                                intent.putExtra("remotepath", remotePath);
                            }
                            if (message.getChatType() != ChatType.Chat) {
                                // delete the image from server after download
                            }
                            if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked) {
                                message.isAcked = true;
                                try {
                                    // 看了大图后发个已读回执给对方
                                    EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            
    						Bundle bundle = new Bundle();
    						bundle.putInt("position", position);
    						bundle.putInt("type", MsgDetailsActivity.MESSAGE_TYPE_IMAGE);
    						intent.putExtras(bundle);
    						if(message.direct == EMMessage.Direct.SEND){
    							activity.startActivity(intent);
    						}else{
    							activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_CONTEXT_MENU);
    						}
                        }
                	}
                }
            });
        } else {
            if (message.status == EMMessage.Status.FAIL) {
                if (CommonUtils.isNetWorkConnected(activity)) {
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            EMChatManager.getInstance().asyncFetchMessage(message);
                        }
                    }).start();
                }
            }

        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
