package com.yidianhulian.secrettalk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.OnNotificationClickListener;
import com.yidianhulian.framework.db.KVHandler;
import com.yidianhulian.secrettalk.activity.ChatActivity;
import com.yidianhulian.secrettalk.activity.MainActivity;
import com.yidianhulian.secrettalk.activity.Splash;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.PreferenceUtils;
import com.yidianhulian.secrettalk.utils.VoiceCallReceiver;

public class YDSecretTalkApplication extends Application {

    private KVHandler mKvh;
    private User loginUser;
    public Map<Integer, String> fromUser = new HashMap<Integer, String>();
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mKvh = new KVHandler(getApplicationContext(), "secrettalk", null, 1);
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        // 如果使用到百度地图或者类似启动remote service的第三方库，这个if判断不能少
        if (processAppName == null || processAppName.equals("")) {
            return;
        }

        applicationContext = this;
        // 初始化环信SDK,一定要先调用init()
        EMChat.getInstance().init(applicationContext);

        // 获取到EMChatOptions对象
        EMChatOptions options = EMChatManager.getInstance().getChatOptions();

        // 设置收到消息是否有新消息通知，默认为true
        // PreferenceUtils.getInstance(applicationContext).getSettingMsgNotification()
        options.setNotifyBySoundAndVibrate(true);

        // 设置收到消息是否有声音提示，默认为true
        options.setNoticeBySound(true);
        // 设置收到消息是否震动 默认为true
        options.setNoticedByVibrate(true);

        // 设置语音消息播放是否设置为扬声器播放 默认为true
        options.setUseSpeaker(true);

        // 设置后台接收新消息时是否通过通知栏提示
        options.setShowNotificationInBackgroud(true);

        // 自定义消息提示
        options.setNotifyText(new OnMessageNotifyListener() {

            @Override
            public String onSetNotificationTitle(EMMessage message) {
                return "密聊";
            }

            @Override
            public String onNewMessageNotify(EMMessage message) {
                return null;
            }

            @Override
            public String onLatestMessageNotify(EMMessage message,
                    int fromUsersNum, int messageNum) {
            	ChatType chatType = message.getChatType();
                if (chatType == ChatType.Chat) { // 单聊信息
                } else { // 群聊信息
                	String gid = message.getTo();
                	if(!"".equals(gid) && gid != null){
                		EMGroup group = EMGroupManager.getInstance().getGroup(gid);
                		String gname = group.getGroupName();
                		return "“" + gname + "” 房间有 " + messageNum + " 条新消息";
                	}
                }
                return "你有" + messageNum + "条新消息";
            }

			@Override
			public int onSetSmallIcon(EMMessage arg0) {
				// TODO Auto-generated method stub
				return 0;
			}
        });

        // 设置notification消息点击时，跳转的intent为自定义的intent
        options.setOnNotificationClickListener(new OnNotificationClickListener() {

            @Override
            public Intent onNotificationClick(EMMessage message) {
                Intent intent = new Intent(applicationContext, ChatActivity.class);
                ChatType chatType = message.getChatType();
                if (chatType == ChatType.Chat) { // 单聊信息
                    intent.putExtra("openid", message.getFrom());
                    // intent.putExtra("chatType", 1);
                } else { // 群聊信息
                         // message.getTo()为群聊id
                    intent.putExtra("gid", message.getTo());
                    intent.putExtra("from", ChatActivity.FROM_NOTIFICATION);
//                    intent.putExtra("chatType", 2);
                }
                return intent;
                
            }
        });
        // 设置一个connectionlistener监听账户重复登陆
        EMChatManager.getInstance().addConnectionListener(
                new MyConnectionListener());

        // 注册一个语言电话的广播接收者
        IntentFilter callFilter = new IntentFilter(EMChatManager.getInstance()
                .getIncomingVoiceCallBroadcastAction());
        registerReceiver(new VoiceCallReceiver(), callFilter);
    }

    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) this
                .getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
                    .next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm
                            .getApplicationInfo(info.processName,
                                    PackageManager.GET_META_DATA));
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
            }
        }
        return processName;
    }

    class MyConnectionListener implements ConnectionListener {
        @Override
        public void onReConnecting() {
        }

        @Override
        public void onReConnected() {
        }

        @Override
        public void onDisConnected(String errorString) {
            if (errorString != null && errorString.contains("conflict")) {
                // Intent intent = new Intent(applicationContext,
                // MainActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // intent.putExtra("conflict", true);
                // startActivity(intent);
            }

        }

        @Override
        public void onConnecting(String progress) {

        }

        @Override
        public void onConnected() {
        }
    }

    public String getOption(String name) {
        return mKvh.getValue(name);
    }

    public void setOption(String name, String value) {
        mKvh.setValue(name, value);
    }

    public User loginUser() {
        if (loginUser != null)
            return loginUser;

        String user = mKvh.getValue("loginUser");
        if (user == null || "".equalsIgnoreCase(user))
            return null;
        try {
            JSONObject userInfo = new JSONObject(user);
            loginUser = new User(userInfo);
            return loginUser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveUser(User user) {
        loginUser = user;
        if (user == null) {
            mKvh.setValue("loginUser", "");
        } else {
            mKvh.setValue("loginUser", user.toString());
        }
    }
}
