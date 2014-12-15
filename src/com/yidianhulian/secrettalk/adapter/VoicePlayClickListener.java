package com.yidianhulian.secrettalk.adapter;

import java.io.File;

import android.R.raw;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.chat.EMChatDB;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.VoiceMessageBody;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.activity.MsgDetailsActivity;

public class VoicePlayClickListener implements View.OnClickListener {

    EMMessage message;
    VoiceMessageBody voiceBody;
    ImageView voiceIconView;

    private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Activity activity;
    private ChatType chatType;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static VoicePlayClickListener currentPlayListener = null;


    /**
     * 
     * @param message
     * @param v
     * @param iv_read_status
     * @param context
     * @param activity
     * @param user
     * @param chatType
     */
    public VoicePlayClickListener(EMMessage message, ImageView v, ImageView iv_read_status, BaseAdapter adapter, Activity activity,
            String username) {
        this.message = message;
        voiceBody = (VoiceMessageBody) message.getBody();
        this.iv_read_status = iv_read_status;
        this.adapter=adapter;
        voiceIconView = v;
        this.activity = activity;
        this.chatType = message.getChatType();
    }
    
    public VoicePlayClickListener(EMMessage message, ImageView v, Activity activity) {
        this.message = message;
        voiceBody = (VoiceMessageBody) message.getBody();
        voiceIconView = v;
        this.activity = activity;
        this.chatType = message.getChatType();
    }

    public void stopPlayVoice() {
        voiceAnimation.stop();
        voiceIconView.setImageResource(R.drawable.sound_no);
//        if (message.direct == EMMessage.Direct.RECEIVE) {
//            voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
//        } else {
//            voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
//        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
//        ((ChatActivity)activity).playMsgId=null;
//        adapter.notifyDataSetChanged();
    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
//        ((ChatActivity)activity).playMsgId=message.getMsgId();
        AudioManager audioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        if (EMChatManager.getInstance().getChatOptions().getUseSpeaker()){
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        }
        else{
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
            //把声音设定成Earpiece（听筒）出来，设定为正在通话中
             audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();
            try {
                //如果是接收的消息
                if (!message.isAcked && message.direct == EMMessage.Direct.RECEIVE) {
                    message.isAcked = true;
//                    if (iv_read_status != null && iv_read_status.getVisibility() == View.VISIBLE) {
//                        //隐藏自己未播放这条语音消息的标志
//                        iv_read_status.setVisibility(View.INVISIBLE);
//                        EMChatDB.getInstance().updateMessageAck(message.getMsgId(), true);
//                    }
                    //告知对方已读这条消息
                    if(chatType != ChatType.GroupChat)
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                }
            } catch (Exception e) {
                message.isAcked = false;
            }
        } catch (Exception e) {
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
//        if (message.direct == EMMessage.Direct.RECEIVE) {
//                voiceIconView.setImageResource(R.anim.voice_from_icon);
//        } else {
//                voiceIconView.setImageResource(R.anim.voice_to_icon);
//        }
    	voiceIconView.setImageResource(R.anim.play_sound);
        voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        voiceAnimation.start();
    }
    @Override
    public void onClick(View v) {
        if (isPlaying) {
//            if(((ChatActivity)activity).playMsgId !=null&&((ChatActivity)activity).playMsgId .equals(message.getMsgId()))
//            {
//                currentPlayListener.stopPlayVoice();
//                return;
//            }
            currentPlayListener.stopPlayVoice();
            return;
        }

        if (message.direct == EMMessage.Direct.SEND) {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getLocalUrl());
        } else {
        	File file = new File(voiceBody.getLocalUrl());
        	if (file.exists() && file.isFile())
            {
        		playVoice(voiceBody.getLocalUrl());
            }else {
                Toast.makeText(activity, "正在下载语音，稍后点击", Toast.LENGTH_SHORT).show();
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        EMChatManager.getInstance().asyncFetchMessage(message);
                        return null;
                    }
                    
                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
//                        adapter.notifyDataSetChanged();
                    }
                    
                }.execute();
                
            
            }
 
        }
    }
    
    public void auto_play(){
    	if (message.direct == EMMessage.Direct.SEND) {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getLocalUrl());
        } else {
        	File file = new File(voiceBody.getLocalUrl());
        	if (file.exists() && file.isFile())
            {
        		playVoice(voiceBody.getLocalUrl());
            }else {
            	MsgDetailsActivity msgDetailsActivity = (MsgDetailsActivity) activity;
            	msgDetailsActivity.mIsDelete = false;
            	Toast.makeText(activity, "音频文件未找到，单击喇叭试试...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}