package com.fan.gallery;

import java.util.Arrays;

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
import android.content.Intent;
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
	private static final int WRITE_LOG = 0;
	private static final int WRITE_ALOG = 1;
	private static final int ANALYZE_LOG = 2;
	private Button logButton;
	private Button infoButton;
	private Button analyzeButton;
	private EditText pkgName;
	private TextView tv;
	private Handler handler;
	private LogRunnable runnable;
	private LogInfoRunnable infoRunnable;
	private AnalyzeRunnable analyzeRunnable;
	private int pid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        initView();

		tv.setMovementMethod(new ScrollingMovementMethod());
        handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case WRITE_LOG:
					tv.setText("写test日志");
					break;
				case WRITE_ALOG:
					tv.setText("写Cpu日志");
					break;	
				case ANALYZE_LOG:
					Bundle bundle = msg.getData();
					double[] arrValues = bundle.getDoubleArray("values");
					double[] arrx = bundle.getDoubleArray("x");
					if (arrValues.length == 1) {
						tv.setText("Cpu采样数据为1");	
					}
					else {
						tv.setText("Cpu/%: " + Arrays.toString(arrValues));					
				        CpuPercent att = new CpuPercent(arrValues,arrx);
				        Intent intent = att.execute(getApplicationContext());
				        startActivity(intent);
					}

				default:
					break;
				}	
				super.handleMessage(msg);
			}
        	
        };
        runnable = new LogRunnable();
        infoRunnable = new LogInfoRunnable();
        analyzeRunnable = new AnalyzeRunnable();
        logButton.setOnClickListener(new ClickListener());
        infoButton.setOnClickListener(new ClickListener());
        analyzeButton.setOnClickListener(new ClickListener());
      
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
		tv = (TextView)findViewById(R.id.result);
		pkgName = (EditText)findViewById(R.id.pkgInfo);
	}

	private class ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub		
			if (Utils.isSdCardExist()) {
				String packageName = pkgName.getText().toString();
				int uid = CpuInfo.getUid(getApplicationContext(),packageName);
				pid = CpuInfo.getPid(getApplicationContext(), uid, packageName);
				switch (v.getId()) {
				case R.id.createBtn:
					Utils.writeLog(Utils.LOG_FILE_NAME,"开始日志", false);
					Utils.writeLog(Utils.CPU_FILE_NAME,"开始日志", false);
					tv.setText("创建日志，请打开需要测试APP后点击'写日志'按钮开始测试");
					break;
				case R.id.writeBtn:
					new Thread(runnable).start();
					new Thread(infoRunnable).start();
					break;
				case R.id.analyzeBtn:
					new Thread(analyzeRunnable).start();
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
			Utils.writeLog(Utils.LOG_FILE_NAME,"day day up", true);
			Message msg = handler.obtainMessage();
			msg.what = WRITE_LOG;
			handler.sendMessage(msg);
			Log.e("MainActivity", "button1");
		}
		
	}
	private class LogInfoRunnable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Utils.writeLog(Utils.CPU_FILE_NAME, "CpuTimeForPid:" + CpuInfo.getCpuTimeForPid(pid), true);
			Utils.writeLog(Utils.CPU_FILE_NAME, "CpuTotalTime:" + CpuInfo.getCpuTotalTime(), true);
			Message msg = handler.obtainMessage();
			msg.what = WRITE_ALOG;
			handler.sendMessage(msg);
			handler.postDelayed(this, 60000 * Utils.DELAYTIME);
			Log.e("MainActivity", "button2");
		}
		
	}
	
	private class AnalyzeRunnable implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message msg = handler.obtainMessage();
			msg.what = ANALYZE_LOG;
			double[] arrValues = Utils.getCpuPercent(Utils.CPU_FILE_NAME);
			double[] arrx = new double[arrValues.length];
			arrx[0] = 0;
			for (int i = 1; i < arrx.length; i++) {
				arrx[i] = arrx[i - 1] + Utils.DELAYTIME;
			}
			Bundle bundle = new Bundle();
			bundle.putDoubleArray("values", arrValues);
			bundle.putDoubleArray("x", arrx);
			msg.setData(bundle);
			handler.sendMessage(msg);
			Log.e("MainActivity", "button3");
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		handler.removeCallbacks(runnable);
		handler.removeCallbacks(infoRunnable);
		handler.removeCallbacks(analyzeRunnable);
		handler.removeMessages(WRITE_LOG);
		handler.removeMessages(WRITE_ALOG);
		handler.removeMessages(ANALYZE_LOG);
		Log.e("MainActivity", "onDestroy");
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
