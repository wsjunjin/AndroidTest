package com.fan.monitor;

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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Utils {
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int SECONDS_PER_HOUR = 60 * 60;
	private static final int SECONDS_PER_DAY = 24 * 60 * 60;
	private static final long KB = 1024;
	private static final long MB = KB * 1024;
	private static final long GB = MB * 1024;
	private static final String TAG = "write info:";

	/**
	 * Returns elapsed time for the given millis, in the following format: 2d 5h 40m 29s
	 * 
	 * @param millis
	 *            the elapsed time in milli seconds
	 * @return the formatted elapsed time
	 */
	public static String formatElapsedTime(double millis) {
		StringBuilder sb = new StringBuilder();
		int seconds = (int) Math.floor(millis / 1000);

		int days = 0, hours = 0, minutes = 0;
		if (seconds > SECONDS_PER_DAY) {
			days = seconds / SECONDS_PER_DAY;
			seconds -= days * SECONDS_PER_DAY;
		}
		if (seconds > SECONDS_PER_HOUR) {
			hours = seconds / SECONDS_PER_HOUR;
			seconds -= hours * SECONDS_PER_HOUR;
		}
		if (seconds > SECONDS_PER_MINUTE) {
			minutes = seconds / SECONDS_PER_MINUTE;
			seconds -= minutes * SECONDS_PER_MINUTE;
		}
		if (days > 0) {
			sb.append(days);
			sb.append(hours);
			sb.append(minutes);
			sb.append(seconds);
		} else if (hours > 0) {
			sb.append(hours);
			sb.append(minutes);
			sb.append(seconds);
		} else if (minutes > 0) {
			sb.append(minutes);
			sb.append(seconds);
		} else {
			sb.append(seconds);
		}
		return sb.toString();
	}

	/**
	 * Formats data size in KB, MB, from the given bytes.
	 * 
	 * @param context
	 *            the application context
	 * @param bytes
	 *            data size in bytes
	 * @return the formatted size such as 4.52 MB or 245 KB or 332 bytes
	 */
	public static String formatBytes(Context context, double bytes) {
		// TODO: I18N
		if (bytes > 1000 * 1000) {
			return String.format("%.2f MB", ((int) (bytes / 1000)) / 1000f);
		} else if (bytes > 1024) {
			return String.format("%.2f KB", ((int) (bytes / 10)) / 100f);
		} else {
			return String.format("%d bytes", (int) bytes);
		}
	}
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
	public static String getBatteryPercentage(int level,int scale) {
		return String.valueOf(level * 100 / scale) + "%";
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
     * 写日志
     * @param info 
     *        需要写入的日志信息
     * @param flag  
     *        false 重新建立文件并写日志
     *        true 添加日志
     */  
    public static void writeLog(String info,Boolean flag){
    	if (isSdCardExist()) {
            File file = new File(Environment.getExternalStorageDirectory(), "MonitorInfo.txt");          
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
    /** 
     * 读日志
     */ 
    public static String readLog() {  
	    String str = "";
    	if (isSdCardExist()) {
			try {
	    		File file = new File(Environment.getExternalStorageDirectory(), "MonitorInfo.txt");     
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
     * 根据输入参数分析数据
     * @param src 输入字符串
     * @param startValue  起始字符
     * @param endValue  结束字符 
     * @return 返回监测结果
     */
    public static String analyzeInfo(String src,String startValue,String endValue)
    {
    	int start,end;
    	String fstart,fend;
    	String unit = "";
    	char fc = ' ';
    	char ec = ' ';
	    start = src.indexOf(startValue) + startValue.length();
	    end = src.indexOf(endValue, start);
	    if (!startValue.equals("processAppUsage:")) {
		    fc = src.charAt(end - 1);  
		    if (fc == 'G' || fc == 'M'|| fc == 'K') {
				end = end - 2;
			}
		    else {
		    	end = end - 1;	
			} 
		}
	    fstart = src.substring(start, end);
	    start = src.lastIndexOf(startValue) + startValue.length();
    	end = src.indexOf(endValue, start); 
	    if (!startValue.equals("processAppUsage:")) {
	    	ec = src.charAt(end - 1); 
		    if (fc == 'G' || fc == 'M'|| fc == 'K') {
				end = end - 2;
			}
		    else {
		    	end = end - 1;	
			} 
		}
	    fend = src.substring(start, end);
    	BigDecimal fbd,ebd;
    	String s = "";
		fbd = new BigDecimal(convertByte(fstart,fc));
		ebd = new BigDecimal(convertByte(fend,ec));
    	if (!startValue.equals("processAppUsage:")) {
    		if (fc == ec) {
				float f = new BigDecimal(Float.parseFloat(fend) - Float.parseFloat(fstart)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				if (f < 0) {
					s = "-" + convertFileSize(convertByte(String.valueOf(Math.abs(f)), fc));
				}
				else {
					s = convertFileSize(convertByte(String.valueOf(f), fc));
				}			    
    		}
    		else {	
        		long l = ebd.subtract(fbd).setScale(2, BigDecimal.ROUND_HALF_UP).longValue();  
            	if (l < 0) {
            		s = "-" + convertFileSize(Math.abs(l));
        		}
            	else {
            		s = convertFileSize(l);
        		}
			}
		}
    	else {
    		s = String.valueOf(ebd.subtract(fbd).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
		}
    	return s;
    }
    
    private static long convertByte(String value, Character unit) {
		// TODO Auto-generated method stub
    	float f = Float.parseFloat(value);
    	long result;
		if (unit.equals('G')) {
			result = (long) (f * GB);
		}
		else if (unit.equals('M')) {
			result = (long) (f * MB);
		}
		else if (unit.equals('K')) {
			result = (long) (f * KB);
		}
		else {
			result = (long) f;
		}
		return result;
	}

	/** 
	 * 分析日志 
	 */ 
    public static void analyzeLog()
    {
    	writeLog("监测结果" + "\n" + getAnalyzeResult(), true);   			  	
    }    
    
    public static String getAnalyzeResult()
    {
    	String str = readLog();
    	String result = "";
    	result = "流量消耗:" + analyzeInfo(str, "app traffic:", "B");
    	result += "\n电量消耗:" + analyzeInfo(str, "processAppUsage:", "[");
    	result += "\n存储消耗:" + "cacheSize:" + analyzeInfo(str, "cacheSize:", "B") + "\ndataSize:" + analyzeInfo(str, "dataSize:", "B") + "\ncodeSize:" + analyzeInfo(str, "codeSize:", "B")
    			+ "\nexternalCacheSize:" + analyzeInfo(str, "externalCacheSize:", "B") +  "\nexternalDataSize:" + analyzeInfo(str, "externalDataSize:", "B") + "\nexternalCodeSize:" + analyzeInfo(str, "externalCodeSize:", "B");  
    	result += "\nCPU%:" + analyzeCpuInfo(str);
    	result += "\n内存消耗:" + "TotalPss:" + analyzeInfo(str, "TotalPss:", "B") + "\nTotalPrivateDirty:" + analyzeInfo(str, "TotalPrivateDirty:", "B") + "\nTotalSharedDirty:" + analyzeInfo(str, "TotalSharedDirty:", "B");
    	return result;
    }
    
	private static String analyzeCpuInfo(String src) {
		// TODO Auto-generated method stub
    	int start,end;
    	String firValue,SecValue;
    	String startValue = "CpuTime:";
    	String endValue = " ";
	    start = src.indexOf(startValue) + startValue.length();
	    end = src.indexOf(endValue, start);	    
	    firValue = src.substring(start, end);
	    start = src.lastIndexOf(startValue) + startValue.length();
    	end = src.indexOf(endValue, start); 
	    SecValue = src.substring(start, end);
    	long cpuTime = Long.parseLong(SecValue) - Long.parseLong(firValue);
    	startValue = "CpuTimeForPid:";
    	endValue = "[";
	    start = src.indexOf(startValue) + startValue.length();
	    end = src.indexOf(endValue, start);	    
	    firValue = src.substring(start, end);
	    start = src.lastIndexOf(startValue) + startValue.length();
    	end = src.indexOf(endValue, start); 
	    SecValue = src.substring(start, end);
        long cpuTimeForPid = Long.parseLong(SecValue) - Long.parseLong(firValue);
    	float percent = (float)cpuTimeForPid / cpuTime;
    	return getPercent(percent);
		
	}

	public static String getPercent(float fraction) {
		fraction *= 100;
	    String str = String.format("%.0f", fraction) + "%";
	    return str;
	}
}

