package com.yidianhulian.secrettalk.utils;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.yidianhulian.framework.Api;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class Util {
	
	/**
	 * 提示对话框
	 */
	public static ProgressDialog loading;
	/**
	 * 数据加载中
	 */
	public static final int DATA_LOADING = 1;
	/**
	 * 数据处理中
	 */
	public static final int DATA_PROCESSING = 2;
	
	/**
	 * 加载提示对话框
	 * 
	 * @param context
	 * @param title
	 */
	public static void showLoading(Activity context, String desc) {
		if (loading != null) {
			loading.dismiss();
		}
		if (context == null) return;
		
		loading = new ProgressDialog(context);
	    loading.setCanceledOnTouchOutside(false);	// 返回按钮有响应
		loading.setMessage(desc);
		loading.show();
	}

	/**
	 * 加载提示对话框
	 * 
	 * @param context
	 * @param type
	 */
	public static void showLoading(Activity context, int type) {
		String desc = "";
		switch (type) {
		case DATA_LOADING:
			desc = "数据加载中,请稍等 …";
			break;
		case DATA_PROCESSING:
			desc = "数据处理中,请稍等 …";
			break;
		default:
			desc = "数据加载中,请稍等 …";
		}
		showLoading(context, desc);
	}

	/**
	 * 隐藏提示对话框
	 */
	public static void hideLoading() {
		if (loading != null && loading.isShowing()) {
			loading.dismiss();
		}
	}
	
	/**
	 * 隐藏软键盘
	 * @param view getWindow().peekDecorView();
	 * @param application
	 */
	public static void closeKeyboard(View view,Application application){
        if (view != null) {
        	InputMethodManager inputmanger = (InputMethodManager) application.getSystemService(application.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
	}
	
	public static boolean checkResult(Context context, JSONObject result) {
//		if (result == null) {
//			return false;
//		}
//		String status = (String) Api.getJSONValue(result, "status");
//		if ("ok".equals(status)) {
//			return true;
//		} else if ("error".equals(status)) {
//			Toast.makeText(context, Api.getStringValue(result, "error").toString(), Toast.LENGTH_SHORT);
//			return false;
//		} else {
//			return false;
//		}
		return false;
	}
	
    /**
     * 获取当前的网络状态  -1：没有网络  1：WIFI网络  2：wap网络  3：net网络
     * @param context
     * @return
     */ 
    public static int getAPNType(Context context){ 
        int netType = -1;  
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); 
        if(networkInfo==null){ 
            return netType; 
        } 
        int nType = networkInfo.getType(); 
        if(nType==ConnectivityManager.TYPE_MOBILE){ 
//            Log.e("networkInfo.getExtraInfo()", "networkInfo.getExtraInfo() is "+networkInfo.getExtraInfo()); 
            if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){ 
//                netType = CMNET; 
                netType = 3; 
            } 
            else{ 
//                netType = CMWAP; 
                netType = 2;
            } 
        } 
        else if(nType==ConnectivityManager.TYPE_WIFI){ 
//            netType = WIFI;
            netType = 1; 
        } 
        return netType; 
    }
    
    private static Boolean isExit = false;
	public static void exitBy2Click(Activity activity) {  
	    Timer tExit = null;  
	    if (isExit == false) {  
	        isExit = true; // 准备退出  
	        Toast.makeText(activity, "再按一次退出程序", Toast.LENGTH_SHORT).show();  
	        tExit = new Timer();  
	        tExit.schedule(new TimerTask() {  
	            @Override  
	            public void run() {  
	                isExit = false; // 取消退出  
	            }  
	        }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务  
	  
	    } else {  
	    	activity.finish();
	        System.exit(0);  
	    }  
	}
}
