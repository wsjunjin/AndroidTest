package com.fan.gallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.R.integer;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	private static final String TAG = "Gallery";
	private static final int WRITE_MEM_LOG = 0;
	private static final int WRITE_CPU_LOG = 1;
	private static final int ANALYZE_LOG = 2;
	private static final int ANALYZE_CPU_LOG = 3;
	private static final int ANALYZE_MEM_LOG = 4;	
	private Button logButton;
	private Button infoButton;
	private Button analyzeButton;
	private EditText pkgName;
	private TextView cputv;
	private TextView memtv;
	private Handler handler;
	private LogRunnable runnable;  //Memory
	private LogInfoRunnable infoRunnable;  //Cpu
	private AnalyzeRunnable analyzeRunnable;  //获取日志信息	
	private AnalyzeRunnable cpuinfoRunnable;  //获取cpu信息
	private AnalyzeRunnable meminfoRunnable;  //获取Memory信息
	
	private int pid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        initView();		
        handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Bundle bundle = null;
				double[] arrx = null;
				List<double[]> listValues = null;
				List<double[]> listx = null;
				int len = 0;
				CpuPercent att = null;
				Intent intent = null;
				switch (msg.what) {
				case WRITE_MEM_LOG:
					cputv.setText("写Memory日志");
					memtv.setText("");
					break;
				case WRITE_CPU_LOG:
					cputv.setText("写Cpu日志");
					break;	
				case ANALYZE_LOG:			
					bundle = msg.getData();
					if (bundle.getDoubleArray("cpu").length == 1) {
						cputv.setText("Cpu采样数据为1");	
					}
					else {
						cputv.setText("Cpu/%: " + Arrays.toString(bundle.getDoubleArray("cpu")));					
					}						
					memtv.setText("TotalPss: " + Arrays.toString(bundle.getDoubleArray("TotalPss")) + "\n"
							+ "TotalPrivateDirty: " + Arrays.toString(bundle.getDoubleArray("TotalPrivateDirty")) + "\n"
							+ "TotalSharedDirty: " + Arrays.toString(bundle.getDoubleArray("TotalSharedDirty")));
					break;
									
				case ANALYZE_CPU_LOG:
				    bundle = msg.getData();	
					arrx = bundle.getDoubleArray("x");
					listValues = new ArrayList<double[]>();
					listValues.add(bundle.getDoubleArray("cpu"));
					listx = new ArrayList<double[]>();
					len = listValues.size();
					for (int i = 0; i < len; i++) {
						listx.add(arrx);
					}				
				    att = new CpuPercent(listValues,listx,bundle.getStringArray("title"),"Cpu Usage");
			        intent = att.execute(getApplicationContext());
			        startActivity(intent);
			        break;
									
				case ANALYZE_MEM_LOG:				
					bundle = msg.getData();
					arrx = bundle.getDoubleArray("x");
					listValues = new ArrayList<double[]>();
					listValues.add(bundle.getDoubleArray("TotalPss"));
					listValues.add(bundle.getDoubleArray("TotalPrivateDirty"));
					listValues.add(bundle.getDoubleArray("TotalSharedDirty"));
					listx = new ArrayList<double[]>();
					len = listValues.size();
					for (int i = 0; i < len; i++) {
						listx.add(arrx);
					}				
				    att = new CpuPercent(listValues,listx,bundle.getStringArray("title"),"Memory Usage");
			        intent = att.execute(getApplicationContext());
			        startActivity(intent);
			        break;									
				default:
					break;
				}	
				super.handleMessage(msg);
			}
        	
        };
        runnable = new LogRunnable();
        infoRunnable = new LogInfoRunnable();
        analyzeRunnable = new AnalyzeRunnable(ANALYZE_LOG);
        cpuinfoRunnable = new AnalyzeRunnable(ANALYZE_CPU_LOG);
        meminfoRunnable = new AnalyzeRunnable(ANALYZE_MEM_LOG);
        logButton.setOnClickListener(new ClickListener());
        infoButton.setOnClickListener(new ClickListener());
        analyzeButton.setOnClickListener(new ClickListener());
        cputv.setOnClickListener(new ClickListener());
        memtv.setOnClickListener(new ClickListener());
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		logButton = (Button)findViewById(R.id.createBtn);
		infoButton = (Button)findViewById(R.id.writeBtn);
		analyzeButton = (Button)findViewById(R.id.analyzeBtn);		
		cputv = (TextView)findViewById(R.id.cpuInfo);
		memtv = (TextView)findViewById(R.id.memInfo);
		pkgName = (EditText)findViewById(R.id.pkgInfo);
		cputv.setMovementMethod(new ScrollingMovementMethod());
		cputv.setClickable(true);
		memtv.setMovementMethod(new ScrollingMovementMethod());
		memtv.setClickable(true);
	}

	private class ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub		
			if (Utils.isSdCardExist()) {
				String packageName = pkgName.getText().toString();
				int uid = CpuInfo.getUid(getApplicationContext(),packageName);
				switch (v.getId()) {
				case R.id.createBtn:
					Utils.writeLog(Utils.MEM_FILE_NAME,"开始日志", false);
					Utils.writeLog(Utils.CPU_FILE_NAME,"开始日志", false);
					cputv.setText("创建日志，请点击‘开始监测’进行测试");
					memtv.setText("");
					break;
				case R.id.writeBtn:
					startTestApp(getApplicationContext(), packageName);
					//pid = CpuInfo.getPid(getApplicationContext(), uid, packageName);
					//Log.e("pid", String.valueOf(pid));
					new Thread(runnable).start();
					new Thread(infoRunnable).start();
					break;
				case R.id.analyzeBtn:
					handler.removeCallbacks(runnable);
					handler.removeCallbacks(infoRunnable);
					handler.removeMessages(WRITE_MEM_LOG);
					handler.removeMessages(WRITE_CPU_LOG);
					new Thread(analyzeRunnable).start();
					break;
				case R.id.cpuInfo:
					new Thread(cpuinfoRunnable).start();
					break;
				case R.id.memInfo:
					new Thread(meminfoRunnable).start();
					break;
				default:
					break;
				}
			}
		}
		
	}
	
	private class LogRunnable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			MemInfo.getMemoryForPid(getApplicationContext(), pid, Utils.MEM_FILE_NAME);
			Message msg = handler.obtainMessage();
			msg.what = WRITE_MEM_LOG;
			handler.sendMessage(msg);
			handler.postDelayed(this, 60000 * Utils.DELAYTIME);			
			//Log.e(TAG, "写MemInfo");
		}
		
	}
	private class LogInfoRunnable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Utils.writeLog(Utils.CPU_FILE_NAME, "CpuTimeForPid:" + CpuInfo.getCpuTimeForPid(pid), true);
			Utils.writeLog(Utils.CPU_FILE_NAME, "CpuTotalTime:" + CpuInfo.getCpuTotalTime(), true);
			Message msg = handler.obtainMessage();
			msg.what = WRITE_CPU_LOG;
			handler.sendMessage(msg);
			handler.postDelayed(this, 60000 * Utils.DELAYTIME);
			//Log.e(TAG, "写CpuInfo");
		}
		
	}
	
	private class AnalyzeRunnable implements Runnable{
        int what;
		public AnalyzeRunnable(int what) {
			// TODO Auto-generated constructor stub
			this.what = what;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message msg = handler.obtainMessage();
			msg.what = what;
			double[] arrValues = Utils.getCpuPercent();
			double[] arrcpux = new double[arrValues.length];			
			arrcpux[0] = 0;
			int len = arrcpux.length;
			for (int i = 1; i < len; i++) {
				arrcpux[i] = arrcpux[i - 1] + Utils.DELAYTIME;
			}
			List<double[]> infoValues = Utils.getMemInfo();				
			double[] arrmemx = new double[infoValues.get(0).length];
			arrmemx[0] = 0;
			len = arrmemx.length;
			for (int i = 1; i < len; i++) {
				arrmemx[i] = arrmemx[i - 1] + Utils.DELAYTIME;
			}
			Bundle bundle = new Bundle();
			if (what == ANALYZE_LOG) {
				bundle.putDoubleArray("cpu", arrValues);
				bundle.putDoubleArray("TotalPss", infoValues.get(0));
				bundle.putDoubleArray("TotalPrivateDirty", infoValues.get(1));
				bundle.putDoubleArray("TotalSharedDirty", infoValues.get(2));
			}
			else if (what == ANALYZE_CPU_LOG) {
				bundle.putDoubleArray("cpu", arrValues);
				bundle.putDoubleArray("x", arrcpux);
				bundle.putStringArray("title", new String[]{"cpu"});
			}
			else {
				bundle.putDoubleArray("TotalPss", infoValues.get(0));
				bundle.putDoubleArray("TotalPrivateDirty", infoValues.get(1));
				bundle.putDoubleArray("TotalSharedDirty", infoValues.get(2));
				bundle.putDoubleArray("x", arrmemx);
				bundle.putStringArray("title", new String[]{"TotalPss","TotalPrivateDirty","TotalSharedDirty"});
			}
			msg.setData(bundle);
			handler.sendMessage(msg);
			//Log.e(TAG, "获取日志信息");
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(analyzeRunnable);
		handler.removeCallbacks(cpuinfoRunnable);
		handler.removeCallbacks(meminfoRunnable);
		handler.removeMessages(ANALYZE_LOG);
		handler.removeMessages(ANALYZE_CPU_LOG);
		handler.removeMessages(ANALYZE_MEM_LOG);
		Log.e("MainActivity", "onDestroy");
	}

	public void startTestApp(Context context,String pkgName){
		PackageManager pm = context.getPackageManager();
		PackageInfo pInfo;
		try {
			pInfo = pm.getPackageInfo(pkgName, 0);		                   
            Intent intent = new Intent();  

            //获取intent  		 
            intent = pm.getLaunchIntentForPackage(pkgName);  		 
            startActivity(intent);  
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.none, container, false);
			return rootView;
		}
	}

}
