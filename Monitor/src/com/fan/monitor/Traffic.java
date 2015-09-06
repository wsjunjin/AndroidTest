package com.fan.monitor;



import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.format.Formatter;
import android.util.Log;

public class Traffic {
	public long cachesize ; //缓存大小  
	public long datasize  ;  //数据大小   
	public long codesize  ;  //应用程序大小  
	public long totalsize ; //总大小 
	public Traffic()
	{		
	}
    /*** 获取app网络流量 
     * @param context 
     * @param uid  app uid 
     */ 
	public void getAppTrafficList(Context context,int uid){
		String str = "";
		long rx = TrafficStats.getUidRxBytes(uid);
		long tx = TrafficStats.getUidTxBytes(uid);
		str = Formatter.formatFileSize(context, rx + tx);
        Utils.writeLog("app traffic:" + str, true);
	}
	
    /*** 获取app所用内存 
     * @param context 
     * @param pkgName  app包名 
     * @param uid  app uid 
     */ 
    public void queryPacakgeSize(Context ctx,String pkgName,int uid) throws Exception {
        if (pkgName != null) {
            PackageManager pm = ctx.getPackageManager();
            try {              
            	Method getPackageSizeInfo = pm.getClass().getDeclaredMethod(
                        "getPackageSizeInfo", String.class,int.class,IPackageStatsObserver.class);
                getPackageSizeInfo.invoke(pm,pkgName,uid / 100000,new PkgSizeObserver());
            } catch (Exception ex) {
                Log.e("queryPacakgeSize", "NoSuchMethodException");
                ex.printStackTrace();
                throw ex;
            }
        }
    }
    
   //  aidl文件形成的Bindler机制服务类  
   public class PkgSizeObserver extends IPackageStatsObserver.Stub{  
    /*** 回调函数， 
    * @param pStatus ,返回数据封装在PackageStats对象中 
    * @param succeeded  代表回调成功 
    */   
	@Override
	public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
			throws RemoteException {
		// TODO Auto-generated method stub
        cachesize = pStats.cacheSize  ; //缓存大小  
        datasize = pStats.dataSize  ;  //数据大小   
        codesize = pStats.codeSize  ;  //应用程序大小  
        totalsize = cachesize + datasize + codesize ;  
        Log.i("Traffic", "cachesize--->" + cachesize + " datasize---->"+ datasize + " codeSize---->" + codesize);
        Log.i("Traffic", "externalCacheSize--->" + pStats.externalCacheSize + " externalDataSize---->" + pStats.externalDataSize + " externalCodeSize---->" + pStats.externalCodeSize);
/*      Utils.writeLog("cachesize--->" + cachesize + " datasize---->" + datasize + " codeSize---->" + codesize,true);
        Utils.writeLog("externalCacheSize--->" + pStats.externalCacheSize + " externalDataSize---->" + pStats.externalDataSize + " externalCodeSize---->" + pStats.externalCodeSize,true);
  */      
        Utils.writeLog("cacheSize:" + Utils.convertFileSize(cachesize) + " dataSize:" + Utils.convertFileSize(datasize) + " codeSize:" + Utils.convertFileSize(codesize) ,true);             
        Utils.writeLog("externalCacheSize:" + Utils.convertFileSize(pStats.externalCacheSize) + " externalDataSize:" + Utils.convertFileSize(pStats.externalDataSize) + " externalCodeSize:" + Utils.convertFileSize(pStats.externalCodeSize) ,true);
	}  
  }
}

