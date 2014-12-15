package cn.sharesdk.onekeyshare;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.secrettalk.Const;
import com.yidianhulian.secrettalk.R;
import com.yidianhulian.secrettalk.YDSecretTalkApplication;
import com.yidianhulian.secrettalk.activity.TalkerListActivity;
import com.yidianhulian.secrettalk.model.User;
import com.yidianhulian.secrettalk.utils.Util;

public class ThirdLogin implements CallApiListener, PlatformActionListener,
        Callback {
    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;

    private static final int API_LOGIN = 6;

    private Activity mContext = null;
    private String mFrom = "";
    private String mUserID = "";
    private String mNickname = "";
    private String mLoginType = "";

    public ThirdLogin(Activity context, String from) {
        this.mContext = context;
        this.mFrom = from;
    }

    @Override
    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
        ShareSDK.removeCookieOnAuthorize(true);
        platform.removeAccount();
    }

    @Override
    public void onComplete(Platform platform, int action,
            HashMap<String, Object> res) {
        if (action == Platform.ACTION_USER_INFOR) {
            Message msg = new Message();
            msg.what = MSG_AUTH_COMPLETE;
            res.put("platName", platform.getName());
            res.put("userid", platform.getDb().getUserId());
            msg.obj = res;
            UIHandler.sendMessage(msg, this);
        }
        ShareSDK.removeCookieOnAuthorize(true);
        platform.removeAccount();
        

    }

    @Override
    public void onError(Platform platform, int action, Throwable t) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        ShareSDK.removeCookieOnAuthorize(true);
        platform.removeAccount();
        t.printStackTrace();

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MSG_USERID_FOUND: {
            Util.showLoading(mContext, R.string.userid_found);
        }
            break;
        case MSG_LOGIN: {
            String text = mContext.getResources().getString(R.string.logining,
                    msg.obj);
            Util.showLoading(mContext, text);
            CallApiTask.doCallApi(API_LOGIN, ThirdLogin.this, mContext);
        }
            break;
        case MSG_AUTH_CANCEL: {
            Util.hideLoading();
            Toast.makeText(mContext, R.string.auth_cancel, Toast.LENGTH_SHORT)
                    .show();
        }
            break;
        case MSG_AUTH_ERROR: {
            Util.hideLoading();
            Toast.makeText(mContext, R.string.auth_error, Toast.LENGTH_SHORT)
                    .show();
        }
            break;
        case MSG_AUTH_COMPLETE: {
            Util.showLoading(mContext, R.string.auth_complete);
            HashMap<String, Object> res = (HashMap<String, Object>) msg.obj;
            login(res.get("platName").toString(), res.get("userid").toString(),
                    res);
        }
            break;
        }
        return false;
    }

    private void login(String plat, String userId,
            HashMap<String, Object> userInfo) {
        if ("qq".equals(mFrom.toLowerCase())) {
            mNickname = userInfo.get("nickname").toString();
        } else if ("sina".equals(mFrom.toLowerCase())) {
            mNickname = userInfo.get("screen_name").toString();
        }
        mUserID = userId;
        mLoginType = mFrom;
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }

    public void startThirdLogin() {
        ShareSDK.initSDK(mContext);
        if (mFrom.toLowerCase().equals("qq")) {
            authorize(new QQ(mContext));
        } else if (mFrom.toLowerCase().equals("sina")) {
            authorize(new SinaWeibo(mContext));
        }
    }

    private void authorize(Platform plat) {
        // if (plat.isValid()) {
        // String userId = plat.getDb().getUserId();
        // if (userId != null) {
        // UIHandler.sendEmptyMessage(MSG_USERID_FOUND, this);
        // login(plat.getName(), userId, null);
        // return;
        // }
        // }
        plat.setPlatformActionListener(this);
        plat.SSOSetting(true);
        plat.showUser(null);
    }

    @Override
    public Api getApi(int what, Object... params) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("user_id", mUserID);
        map.put("login_type", mLoginType);
        map.put("nickname", mNickname);
        return new Api("get", Const.HOST + "login.php", map);
    }

    @Override
    public boolean isCallApiSuccess(JSONObject result) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void apiNetworkException(Exception e) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCacheKey(int what, Object... params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleResult(int what, JSONObject result, boolean isDone,
            Object... params) {
        switch (what) {
        case API_LOGIN: {
//            Util.hideLoading();
            boolean sucess = (Boolean) Api.getJSONValue(result, "success");
            if (sucess) {
                User user = null;
                JSONObject userJson = (JSONObject) Api.getJSONValue(result,
                        "data");
                try {
                    // 获取用户信息
                    user = new User();
                    user.setId(Integer.valueOf(userJson.getString("id")));
                    user.setOpenid(userJson.getString("openid"));
                    user.setLogin_type(userJson.getString("login_type"));
                    user.setNick_name(userJson.getString("nick_name"));
                    user.setCreated_on(userJson.getString("created_on"));
                    user.setHx_pwd(userJson.getString("hx_pwd"));
                    user.makeJson();
                    // 缓存用户信息
                    YDSecretTalkApplication yApplication = (YDSecretTalkApplication) mContext
                            .getApplication();
                    yApplication.saveUser(user);

                    // 跳转页面
                    EMChatManager.getInstance().login(user.getOpenid(),
                            user.getHx_pwd(), new EMCallBack() {
                                @Override
                                public void onSuccess() {
                                    mContext.runOnUiThread(new Runnable() {
                                        public void run() {
                                        	Util.hideLoading();
                                            Intent intent = new Intent();
                                            intent.setClass(mContext,
                                                    TalkerListActivity.class);
                                            mContext.startActivity(intent);
                                            mContext.finish();
                                        }
                                    });
                                }

                                @Override
                                public void onProgress(int arg0, String arg1) {
                                }

                                @Override
                                public void onError(int arg0, String arg1) {
                                    Toast.makeText(mContext, "登陆聊天服务器失败！请稍后再试！",
                                            Toast.LENGTH_LONG).show();
                                }
                            });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "登录失败", Toast.LENGTH_SHORT).show();
                }

            } else {
                String apiMsg = (String) Api.getJSONValue(result, "msg");
                if (apiMsg != null)
                    Toast.makeText(mContext, apiMsg, Toast.LENGTH_SHORT).show();
            }
            break;
        }

        default:
            break;
        }

    }

    @Override
    public JSONObject appendResult(int what, JSONObject from, JSONObject to) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONObject prependResult(int what, JSONObject from, JSONObject to) {
        // TODO Auto-generated method stub
        return null;
    }

}
