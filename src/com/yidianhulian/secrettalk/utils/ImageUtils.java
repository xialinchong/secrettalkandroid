package com.yidianhulian.secrettalk.utils;

import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;

public class ImageUtils {
//  public static String getThumbnailImagePath(String imagePath) {
//      String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
//      path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
//      EMLog.d("msg", "original image path:" + imagePath);
//      EMLog.d("msg", "thum image path:" + path);
//      return path;
//  }
    
    public static String getImagePath(String remoteUrl)
    {
        String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
        String path =PathUtil.getInstance().getImagePath()+"/"+ imageName;
        EMLog.d("msg", "image path:" + path);
        return path;
        
    }
    
    
    public static String getThumbnailImagePath(String thumbRemoteUrl) {
        String thumbImageName= thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length());
        String path =PathUtil.getInstance().getImagePath()+"/"+ "th"+thumbImageName;
        EMLog.d("msg", "thum image path:" + path);
        return path;
    }
    
    
}
