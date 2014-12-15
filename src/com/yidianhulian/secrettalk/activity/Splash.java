package com.yidianhulian.secrettalk.activity;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.YDSecretTalkApplication;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.Util;

public class Splash extends Activity {

    private YDSecretTalkApplication mApp;
    private MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.splash);
        
        mApp = (YDSecretTalkApplication) getApplication();
        Intent intent = getIntent();
        String openid = intent.getStringExtra("openid");
        if (openid != null) {
            mApp.fromUser.put(1, openid);  
        }
        
        mHandler = new MyHandler(this, mApp);

        new Thread() {

            @Override
            public void run() {
                try {
                    sleep(1000);
                    mHandler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    static class MyHandler extends Handler {
        // WeakReference<Splash> mActivity;
        Splash mActivity;
        Intent intent = new Intent();
        WeakReference<YDSecretTalkApplication> mApp;

        public MyHandler(Splash activity, YDSecretTalkApplication app) {
            // mActivity = new WeakReference<Splash>(activity);
            mActivity = new WeakReference<Splash>(activity).get();
            mApp = new WeakReference<YDSecretTalkApplication>(app);
        }

        @Override
        public void handleMessage(Message msg) {
            // Splash my = mActivity.get();
            YDSecretTalkApplication app = mApp.get();
            // Intent intent = new Intent();
            // 第一次打开app显示欢迎向导,
            // 不是第一次打开，如果没有登录则登录，
            // 登录了进入主界面
            if(Util.getAPNType(mActivity) == -1){
            	intent.setClass(mActivity, NoNetActivity.class);
            	mActivity.startActivity(intent);
            	mActivity.finish();
            	return;
            }
            

            if ("1".equals(app.getOption("hasLaunched"))) {// has launched
                User user = app.loginUser();
                if (user != null) {// has login
                    EMChatManager.getInstance().login(user.getOpenid(),
                            user.getHx_pwd(), new EMCallBack() {
                                @Override
                                public void onSuccess() {
                                    mActivity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            intent.setClass(mActivity,
                                                    TalkerListActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mActivity.startActivity(intent);
                                        }
                                    });
                                }

                                @Override
                                public void onProgress(int arg0, String arg1) {
                                }

                                @Override
                                public void onError(int arg0, String arg1) {
                                    Toast.makeText(mActivity,
                                            "登陆聊天服务器失败！请稍后再试！",
                                            Toast.LENGTH_LONG).show();
                                    intent.setClass(mActivity,
                                            LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mActivity.startActivity(intent);
                                }
                            });

                } else {// not login
                    intent.setClass(mActivity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                }
            } else {
                intent.setClass(mActivity, Welcome.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
                app.setOption("hasLaunched", "1");
            }
        }
    }
    

}
