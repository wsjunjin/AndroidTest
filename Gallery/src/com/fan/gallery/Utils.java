package com.fan.gallery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.R.integer;
import android.os.Environment;
import android.renderscript.Script.KernelID;
import android.util.Log;

public class Utils {
	private static final String TAG = "write info:";
	public static final String DIR_NAME = "/Gallery/";
	public static final String MEM_FILE_NAME = "MemInfo.txt";
	public static final String CPU_FILE_NAME = "CpuInfo.txt";
	public static final int DELAYTIME = 1;	
	private static final long KB = 1024;
	private static final long MB = KB * 1024;
	private static final long GB = MB * 1024;
	/**
	 * Formats data size in KB, MB, from the given bytes.
	 * @param size
	 *            data size in bytes
	 * @return the formatted size such as 4.52 MB or 245 KB or 332 B
	 */ 
	public static String convertFileSize(long size) { 
        if (size >= GB) {
            return String.format("%.2f GB", (float) size / GB);
        } else if (size >= MB) {
            float f = (float) size / MB;
            return String.format( "%.2f MB", f);
        } else if (size >= KB) {
            float f = (float) size / KB;
            return String.format("%.2f KB", f);
        } else
            return String.format("%d B", size);
    }
	
	
    /** 
     * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡] 
     *  
     * @return 
     */  
    public static boolean isSdCardExist() {  
        return Environment.getExternalStorageState().equals(  
                Environment.MEDIA_MOUNTED);  
    }
    
    /** 
     * 获取SD卡根目录路径 
     *  
     * @return 
     */  
    public static String getSdCardPath() {  
        boolean exist = isSdCardExist();  
        String sdpath = "";  
        if (exist) {  
            sdpath = Environment.getExternalStorageDirectory()  
                    .getAbsolutePath();  
        } else {  
            sdpath = "SD卡不存在";  
        }  
        return sdpath;  
      
    } 
    
    /** 
     * 判断目录是否存在
     * @param dir
     *        目录名 
     * @return 
     */   
    public static boolean dirExist(String dir){
    	File file = new File(Environment.getExternalStorageDirectory() + dir);
    	return file.exists();	
    }
    
    /** 
     * 判断文件是否存在
     * @param dir
     *        目录名 
     * @param fileName
     *        文件名
     * @return 
     */ 
    public static boolean fileExist(String dir,String fileName) {
		File file = new File(Environment.getExternalStorageDirectory() + dir,fileName);
		return file.exists();
	}
    
    /**
     * 写日志
     * @param fileName
     *        文件名称
     * @param info 
     *        需要写入的日志信息
     * @param flag  
     *        false 重新建立文件并写日志
     *        true 添加日志
     */  
    public static void writeLog(String fileName,String info,Boolean flag){
    	if (isSdCardExist()) {
    		OutputStreamWriter writer = null;
    		BufferedWriter bw = null; 
    		File file = null;
    		if (!dirExist(DIR_NAME)) {
    			file = new File(Environment.getExternalStorageDirectory() + DIR_NAME); 
    			file.mkdir();
			}
    		if (!fileExist(DIR_NAME, fileName)) {
				file = new File(Environment.getExternalStorageDirectory() + DIR_NAME, fileName);
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    		file = new File(Environment.getExternalStorageDirectory() + DIR_NAME, fileName);
    		try { 
    			writer = new OutputStreamWriter(new FileOutputStream(file,flag),"gbk");
    			bw = new BufferedWriter(writer);  		    			 
    		    SimpleDateFormat sDateFormat = new SimpleDateFormat("[yyyy-MM-dd hh:mm:ss:SSS]");       
    		    String date = sDateFormat.format(new java.util.Date());
    		    bw.write(date + info + "\r\n");  
    		    bw.flush();  
    	        Log.i(TAG, "写日志信息成功");
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
    		finally{
    			if (bw != null) {
					try {
						bw.close();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
    			
    		}
        }
	}
    
    /**
     * 获取百分比
     * @param fraction
     */  
	public static double getPercent(float fraction) {
		fraction *= 100;
	    double d = Math.round(fraction);
	    return d;
	}
	
    /** 
     * 读日志
     * @param fileName
     *        文件名称
     */ 
    public static String readLog(String fileName) {  
	    String str = "";
    	if (isSdCardExist()) {
			try {
	    		File file = new File(Environment.getExternalStorageDirectory() + DIR_NAME, fileName);     
	    		InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "gbk");
	    	    BufferedReader br = new BufferedReader(reader);  
	    	    String readline = "";    
	    	    StringBuffer sb = new StringBuffer();
	    	    while ((readline = br.readLine()) != null) {  
	    	        //Log.e("readline: ", readline);
	    	        sb.append(readline);  
	    	    }  
	    	    br.close(); 
	    	    str = sb.toString();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
    	return str;	     
    } 
	
    /** 
     * 获取Cpu信息
     */ 
	public static double[] getCpuPercent() {
		String info = readLog(CPU_FILE_NAME);
        String eL = "\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]\\w*:";		
        Pattern p = Pattern.compile(eL);
        String[] arr = p.split(info);
        int len = arr.length;
        if (len <= 3) {
			return new double[]{0};
		}
        String[] arrPidCpuTime = new String[(arr.length - 1)/2];
        String[] arrCpuTotalTime = new String[(arr.length - 1)/2];
        double[] arrPercent = new double[arrPidCpuTime.length - 1];
        long pidCpuTime,cpuTotalTime;
        int k = 1;
        len = arrPidCpuTime.length;
        for (int i = 0; i < len; i++) {
			arrPidCpuTime[i] = arr[k];
			arrCpuTotalTime[i] = arr[k + 1];
			k += 2;
		}
        len = arrPercent.length;
        for (int i = 0; i < len; i++) {
			pidCpuTime = Long.parseLong(arrPidCpuTime[i+1]) - Long.parseLong(arrPidCpuTime[i]);
			cpuTotalTime = Long.parseLong(arrCpuTotalTime[i+1]) - Long.parseLong(arrCpuTotalTime[i]);
			float f = (float)pidCpuTime/cpuTotalTime;
			arrPercent[i] = getPercent(f);
		}      
		return arrPercent;
	}
	
    /** 
     * 获取Memory信息
     * @return 返回信息为TotalPss数组，TotalPrivateDirty数组，TotalSharedDirty数组
     */ 
	public static List<double[]> getMemInfo() {
		String info = readLog(MEM_FILE_NAME);
        String eL = "\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]\\w*:";		
        Pattern p = Pattern.compile(eL);
        String[] arr = p.split(info);
        int len = arr.length;
        if (len <= 1) {
			return new ArrayList<double[]>();
		}
        double[] arrTotalPss = new double[(arr.length - 1)/3];
        double[] arrTotalPrivateDirty = new double[(arr.length - 1)/3];
        double[] arrTotalSharedDirty = new double[(arr.length - 1)/3];
        len = arrTotalPss.length;
        int k = 1;
        String str;
        double d;
        for (int i = 0; i < len; i++) {
			str = arr[k];
			int end = str.indexOf("B") - 1;
			str = str.substring(0, end);
			arrTotalPss[i] = Double.parseDouble(str);
			str = arr[k + 1];
			end = str.indexOf("B") - 1;
			str = str.substring(0, end);
			arrTotalPrivateDirty[i] = Double.parseDouble(str);
			str = arr[k + 2];
			end = str.indexOf("B") - 1;
			str = str.substring(0, end);
			arrTotalSharedDirty[i] = Double.parseDouble(str);
        	k += 3;
		}     
        List<double[]> arrList = new ArrayList<double[]>();
        arrList.add(arrTotalPss);
        arrList.add(arrTotalPrivateDirty);
        arrList.add(arrTotalSharedDirty);
		return arrList;
	}
}
