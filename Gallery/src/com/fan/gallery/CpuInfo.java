package com.fan.gallery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.util.Log;

public class CpuInfo {
	public static final int PROC_SPACE_TERM = (int)' ';
	public static final int PROC_PARENS = 0x200;
    public static final int PROC_OUT_LONG = 0x2000;
    public static final int PROC_OUT_FLOAT = 0x4000;
    static final int PROCESS_STAT_UTIME = 2;
    static final int PROCESS_STAT_STIME = 3;
    public static final int PROC_COMBINE = 0x100;
    private static final int[] PROCESS_STATS_FORMAT = new int[] {
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_PARENS,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 10: minor faults
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 12: major faults
        PROC_SPACE_TERM,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 14: utime
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 15: stime
    };
    private static final int[] SYSTEM_CPU_FORMAT = new int[] {
        PROC_SPACE_TERM|PROC_COMBINE,
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 1: user time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 2: nice time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 3: sys time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 4: idle time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 5: iowait time
        PROC_SPACE_TERM|PROC_OUT_LONG,                  // 6: irq time
        PROC_SPACE_TERM|PROC_OUT_LONG                   // 7: softirq time
    };
    
    /** 
     * 获取uid
     * @param context
     * @param pkgName 包名
     * @return 返回包对应uid
     */ 
	public static int getUid(Context context,String pkgName) {
		// TODO Auto-generated method stub
		int uid = 0;
		PackageManager pm = context.getPackageManager();
		PackageInfo pInfo;
		String processName;
		try {
			pInfo = pm.getPackageInfo(pkgName, 0);
			uid = pInfo.applicationInfo.uid;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uid;
	}
	
    /** 
     * 获取uid和包名对应pid
     * @param context
     * @param uid 
     * @param pkgName 包名
     * @return 返回包对应pid
     */ 
	public static int getPid(Context context,int uid,String pkgName){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcessInfos = am.getRunningAppProcesses();
		int pid = 0;
		for (RunningAppProcessInfo pf:runningAppProcessInfos) {
			if (pf.uid == uid && pkgName.equals(pf.processName)) {
				pid = pf.pid;
			}
		}		
		return pid;
	}
	
    /** 
     * 获取uid对应所有pid
     * @param context
     * @param uid 
     * @return 返回uid对应pid
     */
	public static HashMap<Integer, String> getPidInfo(Context context,int uid){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningAppProcessInfos = am.getRunningAppProcesses();
		HashMap<Integer, String> hashMap = new HashMap<Integer,String>();
		for (RunningAppProcessInfo pf:runningAppProcessInfos) {
			if (pf.uid == uid) {
				hashMap.put(pf.pid, pf.processName);
				//Log.e("info", "pid: " + pf.pid + "name: " + pf.processName + "uid:" + pf.uid);
			}
		}		
		return hashMap;
	}
	
    /** 
     * 获取pid信息
     */
	public static int[] getPids(HashMap<Integer, String> hashMap){
		int[] arrPid = new int[hashMap.size()];
		Iterator iterator = hashMap.entrySet().iterator();
		for (int i = 0; i < hashMap.size(); i++) {
			Entry< Integer, String> entry = (Entry<Integer, String>) iterator.next();
			arrPid[i] = entry.getKey();
		}
		return arrPid;
	}
	
    /** 
     * 获取pid对应CpuTime
     */
	public static long getCpuTimeForPid(int pid) {
        final String statFile = "/proc/" + pid + "/stat";
        final long[] statsData = new long[4];
        try {
        	Class process = Class.forName("android.os.Process");
        	Method read = process.getMethod("readProcFile", String.class,int[].class,String[].class,long[].class,float[].class);                  
        	if ((Boolean)read.invoke(process.newInstance(), statFile, PROCESS_STATS_FORMAT,null, statsData, null)) {
        		long time = statsData[PROCESS_STAT_UTIME] + statsData[PROCESS_STAT_STIME];
        		//Log.e("CpuTimeForPid", String.valueOf(time));
        		return time;
			  }
            } catch (ClassNotFoundException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
            	e.printStackTrace();
            } catch (IllegalAccessException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            } catch (IllegalArgumentException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            } catch (InvocationTargetException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            } catch (InstantiationException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            }      
        return 0;
    }
	
    /** 
     * 获取CpuTime
     */
	public static long getCpuTotalTime() {
		final long[] sysCpu = new long[7];
		try {
			Class process = Class.forName("android.os.Process");
	    	Method[] methods = process.getMethods();
	    	Method read = process.getMethod("readProcFile", String.class,int[].class,String[].class,long[].class,float[].class);                  
			if ((Boolean)read.invoke(process.newInstance(), "/proc/stat", SYSTEM_CPU_FORMAT,null, sysCpu, null)) {
	            // Total user time is user + nice time.
	            final long usertime = sysCpu[0]+sysCpu[1];
	            // Total system time is simply system time.
	            final long systemtime = sysCpu[2];
	            // Total idle time is simply idle time.
	            final long idletime = sysCpu[3];
	            // Total irq time is iowait + irq + softirq time.
	            final long iowaittime = sysCpu[4];
	            final long irqtime = sysCpu[5];
	            final long softirqtime = sysCpu[6];
	            long totalTime = usertime + systemtime + idletime + irqtime + softirqtime;
	            //Log.e("CpuTime", String.valueOf(totalTime));
	            
	            return totalTime;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;	
	}
	
}
