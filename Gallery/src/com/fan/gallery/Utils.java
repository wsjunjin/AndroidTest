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
	public static final String LOG_FILE_NAME = "log1.txt";
	public static final String CPU_FILE_NAME = "log2.txt";
	public static final int DELAYTIME = 1;
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
     * 写日志
     * @param info 
     *        需要写入的日志信息
     * @param flag  
     *        false 重新建立文件并写日志
     *        true 添加日志
     */  
    public static void writeLog(String fileName,String info,Boolean flag){
    	if (isSdCardExist()) {
            File file = new File(Environment.getExternalStorageDirectory(), fileName);          
    		try { 
    			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file,flag),"gbk");
    			BufferedWriter bw = new BufferedWriter(writer);  		    			 
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
     
        }
	}
    
	public static double getPercent(float fraction) {
		fraction *= 100;
	    double d = Math.round(fraction);
	    return d;
	}
	
    /** 
     * 读日志
     */ 
    public static String readLog(String fileName) {  
	    String str = "";
    	if (isSdCardExist()) {
			try {
	    		File file = new File(Environment.getExternalStorageDirectory(), fileName);     
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
	
	public static double[] getCpuPercent(String fileName) {
		String info = readLog(fileName);
        String eL = "\\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}:[0-9]{3}\\]\\w*:";		
        Pattern p = Pattern.compile(eL);
        String[] arr = p.split(info);
        if (arr.length <= 3) {
			return new double[]{0};
		}
        String[] arrPidCpuTime = new String[(arr.length - 1)/2];
        String[] arrCpuTotalTime = new String[(arr.length - 1)/2];
        double[] arrPercent = new double[arrPidCpuTime.length - 1];
        long pidCpuTime,cpuTotalTime;
        int k = 1;
        for (int i = 0; i < arrPidCpuTime.length; i++) {
			arrPidCpuTime[i] = arr[k];
			k += 2;
		}
        k = 2;
        for (int i = 0; i < arrPidCpuTime.length; i++) {
			arrCpuTotalTime[i] = arr[k];
			k += 2;
		}
        for (int i = 0; i < arrPercent.length; i++) {
			pidCpuTime = Long.parseLong(arrPidCpuTime[i+1]) - Long.parseLong(arrPidCpuTime[i]);
			cpuTotalTime = Long.parseLong(arrCpuTotalTime[i+1]) - Long.parseLong(arrCpuTotalTime[i]);
			float f = (float)pidCpuTime/cpuTotalTime;
			arrPercent[i] = getPercent(f);
		}      
		return arrPercent;
	}
}
