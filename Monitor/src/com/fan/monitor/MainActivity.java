package com.fan.monitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.android.internal.app.ProcessStats;
import com.android.internal.os.ProcessCpuTracker;

import android.R.integer;
import android.app.Activity;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	private Button startButton;
	private Button endButton;
	private Button analyzeButton;
	private EditText pkget;
	private TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
/*		String src = Utils.readLog();
		String str = Utils.getAnalyzeResult();*/
		
/*		String pkgName = "com.youku.phone";
		int uid = CpuInfo.getUid(getApplicationContext(), pkgName);
		int pid = CpuInfo.getPid(getApplicationContext(), uid, pkgName);
		long l = CpuInfo.getCpuTimeForPid(pid);
		Log.e("pidCpuTime", String.valueOf(l));
		Log.e("CpuTotalTime", String.valueOf(CpuInfo.getCpuTotalTime()));
		CpuInfo.getMemoryForPid(getApplicationContext(),pid);
*/
		setContentView(R.layout.activity_main);
		initView();
		startButton.setOnClickListener(new ClickListener());
        endButton.setOnClickListener(new ClickListener());
        analyzeButton.setOnClickListener(new ClickListener());
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	private void initView() {
		// TODO Auto-generated method stub
		startButton = (Button)findViewById(R.id.button1);
		endButton = (Button)findViewById(R.id.button2);
		analyzeButton = (Button)findViewById(R.id.button3);
		pkget = (EditText)findViewById(R.id.editText1);
		tv = (TextView)findViewById(R.id.textView2);
	}
	public class ClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub	
			if (Utils.isSdCardExist()) {
				String pkgName = pkget.getText().toString();
				int uid = CpuInfo.getUid(getApplicationContext(),pkgName);
				int pid = CpuInfo.getPid(getApplicationContext(), uid, pkgName);
				Log.e("pid", String.valueOf(pid));
				Traffic traffic = new Traffic();
				Battery battery = new Battery(getApplicationContext());
				battery.load();
				switch (v.getId()) {
				case R.id.button1:
					tv.setText("");
					Utils.writeLog("日志开始", false);
					Utils.writeLog("开始监测", true);
					traffic.getAppTrafficList(getApplicationContext(), uid);
					try {
						traffic.queryPacakgeSize(getApplicationContext(), pkgName, uid);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					battery.processAppUsage(getApplicationContext(), uid);
					Utils.writeLog("CpuTime:" + CpuInfo.getCpuTotalTime() + " CpuTimeForPid:" + CpuInfo.getCpuTimeForPid(pid), true);
					CpuInfo.getMemoryForPid(getApplicationContext(), pid);
					break;
				case R.id.button2:
					Utils.writeLog("结束监测", true);
					traffic.getAppTrafficList(getApplicationContext(), uid);
					try {
						traffic.queryPacakgeSize(getApplicationContext(), pkgName, uid);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					battery.processAppUsage(getApplicationContext(), uid);
					Utils.writeLog("CpuTime:" + CpuInfo.getCpuTotalTime() + " CpuTimeForPid:" + CpuInfo.getCpuTimeForPid(pid), true);
					CpuInfo.getMemoryForPid(getApplicationContext(), pid);
					break;
				case R.id.button3:				
					tv.setMovementMethod(new ScrollingMovementMethod());
					tv.setText(Utils.getAnalyzeResult());
					Utils.analyzeLog();
					
					break;
				default:
					break;
					}
			
			}
			else {
				tv.setText("请确认是否插入SD卡");
			}
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
