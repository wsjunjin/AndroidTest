package com.fan.gallery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.util.Log;

public class MemInfo {
	
    /** 
     * 获取HeapMemInfo 获取正在执行app的HeapMemInfo，目前貌似不太适用
     */
	public static void getHeapMemInfo() {
		long nativeHeapSize = Debug.getNativeHeapSize();//KB
		long nativeHeapFreeSize = Debug.getNativeHeapFreeSize();
		long nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize();
		Runtime runtime = Runtime.getRuntime();
		long dalvikHeapSize = runtime.totalMemory();
		long dalvikHeapFreeSize = runtime.freeMemory();
		long dalvikHeapAllocatedSize = dalvikHeapSize - dalvikHeapFreeSize;
	}
	
    /** 
     * 获取MemInfo
     * @param context
     * @param hashMap
     * @param fileName 文件名称
     * @return 
     */
	public static void getMemoryInfo(Context context,HashMap<Integer, String> hashMap,String fileName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int[] pids = new int[hashMap.size()];
		String[] names = new String[hashMap.size()];
		Iterator iterator = hashMap.entrySet().iterator();
		int len = hashMap.size();
		for (int i = 0; i < len; i++) {
			Entry< Integer, String> entry = (Entry<Integer, String>) iterator.next();
			pids[i] = entry.getKey();
			names[i] = entry.getValue();
			Log.e("pid info:", "pid:" + pids[i] + " name:" + names[i]);
		}
		MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
		len = memoryInfos.length;
		for (int i = 0; i < len; i++) {
			Utils.writeLog(fileName,names[i] + "-TotalPss:" + Utils.convertFileSize(memoryInfos[i].getTotalPss()*1024) , true);
			Utils.writeLog(fileName,names[i] + "-TotalPrivateDirty:" + Utils.convertFileSize(memoryInfos[i].getTotalPrivateDirty()*1024) , true);
			Utils.writeLog(fileName,names[i] + "-TotalSharedDirty:" + Utils.convertFileSize(memoryInfos[i].getTotalSharedDirty()*1024) , true);
		}
			
	}
	
    /** 
     * 获取pid对应Memory
     * @param context
     * @param pid
     * @param fileName 文件名称
     * @return 
     */
	public static void getMemoryForPid(Context context,int pid,String fileName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		int[] pids = {pid,};
		MemoryInfo[] memoryInfos = am.getProcessMemoryInfo(pids);
		ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(outInfo);
		long availMem = outInfo.availMem;
		long  totalMem = outInfo.totalMem;
		Utils.writeLog(fileName,"TotalPss:" + Utils.convertFileSize(memoryInfos[0].getTotalPss()*1024) , true);
		Utils.writeLog(fileName,"TotalPrivateDirty:" + Utils.convertFileSize(memoryInfos[0].getTotalPrivateDirty()*1024), true);
		Utils.writeLog(fileName,"TotalSharedDirty:" + Utils.convertFileSize(memoryInfos[0].getTotalSharedDirty()*1024), true);
/*		Log.e("cpuInfo", "length:" + String.valueOf(memoryInfos.length));
		Log.e("dalvikPrivateDirty", String.valueOf(memoryInfos[0].dalvikPrivateDirty));
		Log.e("dalvikPss", String.valueOf(memoryInfos[0].dalvikPss));
		Log.e("dalvikSharedDirty", String.valueOf(memoryInfos[0].dalvikSharedDirty));
		Log.e("nativePrivateDirty", String.valueOf(memoryInfos[0].nativePrivateDirty));
		Log.e("nativePss", String.valueOf(memoryInfos[0].nativePss));
		Log.e("nativeSharedDirty",String.valueOf(memoryInfos[0].nativeSharedDirty));
		Log.e("TotalPss", String.valueOf(memoryInfos[0].getTotalPss()));	
		Log.e("TotalPrivateDirty", String.valueOf(memoryInfos[0].getTotalPrivateDirty()));
		Log.e("TotalSharedDirty", String.valueOf(memoryInfos[0].getTotalSharedDirty()));
		Log.e("RSS", String.valueOf(memoryInfos[0].getTotalSharedClean()));
		Log.e("RSS", String.valueOf(memoryInfos[0].getTotalPrivateClean()));*/
	
	}
}
