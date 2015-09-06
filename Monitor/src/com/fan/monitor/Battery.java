package com.fan.monitor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.hardware.SensorManager;
import android.net.TrafficStats;
import android.os.BatteryStats;
import android.os.BatteryStats.Uid;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;

public class Battery {
	
	public static final String TAG = "BatteryInfo";
	public static final boolean DEBUG = true;

	public static final int MSG_UPDATE_NAME_ICON = 1;
	public static final int MIN_POWER_THRESHOLD = 5;

	public IBatteryStats mBatteryInfo;
	public int mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
	public PowerProfile mPowerProfile;
	public static BatteryStatsImpl mStats;

	public double mMinPercentOfTotal = 0;
	public long mStatsPeriod = 0;
	public double mMaxPower = 1;
	public double mTotalPower;
	public double mWifiPower;
	public double mBluetoothPower;
	public long mAppWifiRunning;
	public Context mContext;
	public int testType;
	
	public Battery(Context context) {
		testType = 1;
		mContext = context;
		try {
			Class serviceManager = Class.forName("android.os.ServiceManager");
			Method getService = serviceManager.getMethod("getService", String.class);
			mBatteryInfo = IBatteryStats.Stub.asInterface((IBinder) getService.invoke(serviceManager.newInstance(), "batteryinfo"));
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
		
		//mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
		mPowerProfile = new PowerProfile(context);
	}
	
	/**
	 * 设置最小百分比，小于该值的程序将被过滤掉
	 * 
	 * @param minPercentOfTotal
	 */
	public void setMinPercentOfTotal(double minPercentOfTotal) {
		this.mMinPercentOfTotal = minPercentOfTotal;
	}

	/**
	 * 获取消耗的总量
	 * 
	 * @return
	 */
	public double getTotalPower() {
		return mTotalPower;
	}

	/**
	 * 获取电池的使用时间
	 * 
	 * @return
	 */
	public String getStatsPeriod() {
		return Utils.formatElapsedTime(mStatsPeriod);
	}
	
	public double getAverageDataCost() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from system 
        final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from system
        final double WIFI_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE)
                / 3600;
        final double MOBILE_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
                / 3600;
        final long mobileData = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
        final long wifiData = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - mobileData;
        final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
        final long mobileBps = radioDataUptimeMs != 0
                ? mobileData * 8 * 1000 / radioDataUptimeMs
                : MOBILE_BPS;

        double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
        double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);
        if (wifiData + mobileData != 0) {
            return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
                    / (mobileData + wifiData);
        } else {
            return 0;
        }
    }
    
	public void processMiscUsage() {
        final int which = mStatsType;
        long uSecTime = SystemClock.elapsedRealtime() * 1000;
        final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
        final long timeSinceUnplugged = uSecNow;
        if (DEBUG) {
            Log.i(TAG, "Uptime since last unplugged = " + (timeSinceUnplugged / 1000));
        }

        addPhoneUsage(uSecNow);
        addScreenUsage(uSecNow);
        addWiFiUsage(uSecNow);
        addBluetoothUsage(uSecNow);
        addIdleUsage(uSecNow); // Not including cellular idle power
        addRadioUsage(uSecNow);
    }
    
	public void addPhoneUsage(long uSecNow) {
        long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
        double phoneOnPower = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
                * phoneOnTimeMs / 1000;
        Utils.writeLog("PhoneUsage: " + String.valueOf(phoneOnPower), true);
    }
    
	public void addScreenUsage(long uSecNow) {
        double power = 0;
        long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
        power += screenOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
        final double screenFullPower =
                mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            double screenBinPower = screenFullPower * (i + 0.5f)
                    / BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
            long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow, mStatsType) / 1000;
            power += screenBinPower * brightnessTime;
            if (DEBUG) {
                Log.i(TAG, "Screen bin power = " + (int) screenBinPower + ", time = "
                        + brightnessTime);
            }
        }
        power /= 1000; // To seconds
        Utils.writeLog("ScreenUsage: " + String.valueOf(power), true);
    }
    
	public void addWiFiUsage(long uSecNow) {
        long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
        long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow, mStatsType) / 1000;
        if (DEBUG) Log.i(TAG, "WIFI runningTime=" + runningTimeMs
                + " app runningTime=" + mAppWifiRunning);
        runningTimeMs -= mAppWifiRunning;
        if (runningTimeMs < 0) runningTimeMs = 0;
        double wifiPower = (onTimeMs * 0 /* TODO */
                * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)
            + runningTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
        if (DEBUG) Log.i(TAG, "WIFI power=" + wifiPower + " from procs=" + mWifiPower);
        Utils.writeLog("WiFiUsage: " + String.valueOf(wifiPower), true);
    }
    
	public void addBluetoothUsage(long uSecNow) {
        long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
        double btPower = btOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
                / 1000;
        int btPingCount = mStats.getBluetoothPingCount();
        btPower += (btPingCount
                * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;
        Utils.writeLog("BluetoothUsage: " + String.valueOf(btPower), true);
    }
    
	public void addIdleUsage(long uSecNow) {
        long idleTimeMs = (uSecNow - mStats.getScreenOnTime(uSecNow, mStatsType)) / 1000;
        double idlePower = (idleTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))
                / 1000;
        Utils.writeLog("IdleUsage: " + String.valueOf(idlePower), true);
    }
    
	public void addRadioUsage(long uSecNow) {
        double power = 0;
        final int BINS = BatteryStats.NUM_SIGNAL_STRENGTH_BINS;
        long signalTimeMs = 0;
        for (int i = 0; i < BINS; i++) {
            long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow, mStatsType) / 1000;
            power += strengthTimeMs / 1000
                    * mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i);
            signalTimeMs += strengthTimeMs;
        }
        long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow, mStatsType) / 1000;
        power += scanningTimeMs / 1000 * mPowerProfile.getAveragePower(
                PowerProfile.POWER_RADIO_SCANNING);
        if (signalTimeMs != 0) {
            double noCoveragePercent = mStats.getPhoneSignalStrengthTime(0, uSecNow, mStatsType)
                    / 1000 * 100.0 / signalTimeMs;
        }
        Utils.writeLog("RadioUsage: " + String.valueOf(power), true);
    }
    
	public void processAppUsage(Context context,int iuid) {
        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        final int which = mStatsType;
        final int speedSteps = mPowerProfile.getNumSpeedSteps();
        final double[] powerCpuNormal = new double[speedSteps];
        final long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p = 0; p < speedSteps; p++) {
            powerCpuNormal[p] = mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);
        }
        final double averageCostPerByte = getAverageDataCost();
        long uSecTime = mStats.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, which);
        mStatsPeriod = uSecTime;
        updateStatsPeriod(uSecTime);
        SparseArray<? extends Uid> uidStats = mStats.getUidStats();
        final int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            if (iuid == u.getUid()) {
            	double power = 0;
                double highestDrain = 0;
                String packageWithHighestDrain = null;
                Map<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
                long cpuTime = 0;
                long cpuFgTime = 0;
                long wakelockTime = 0;
                long gpsTime = 0;
                if (processStats.size() > 0) {
                    // Process CPU time
                    for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent
                            : processStats.entrySet()) {
                        if (DEBUG) Log.i(TAG, "Process name = " + ent.getKey());
                        Uid.Proc ps = ent.getValue();
                        final long userTime = ps.getUserTime(which);
                        final long systemTime = ps.getSystemTime(which);
                        final long foregroundTime = ps.getForegroundTime(which);
                        cpuFgTime += foregroundTime * 10; // convert to millis
                        final long tmpCpuTime = (userTime + systemTime) * 10; // convert to millis
                        int totalTimeAtSpeeds = 0;
                        // Get the total first
                        for (int step = 0; step < speedSteps; step++) {
                            cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, which);
                            totalTimeAtSpeeds += cpuSpeedStepTimes[step];
                        }
                        if (totalTimeAtSpeeds == 0) totalTimeAtSpeeds = 1;
                        // Then compute the ratio of time spent at each speed
                        double processPower = 0;
                        for (int step = 0; step < speedSteps; step++) {
                            double ratio = (double) cpuSpeedStepTimes[step] / totalTimeAtSpeeds;
                            processPower += ratio * tmpCpuTime * powerCpuNormal[step];
                        }
                        cpuTime += tmpCpuTime;
                        power += processPower;
                        if (packageWithHighestDrain == null
                                || packageWithHighestDrain.startsWith("*")) {
                            highestDrain = processPower;
                            packageWithHighestDrain = ent.getKey();
                        } else if (highestDrain < processPower
                                && !ent.getKey().startsWith("*")) {
                            highestDrain = processPower;
                            packageWithHighestDrain = ent.getKey();
                        }
                    }
                    if (DEBUG) Log.i(TAG, "Max drain of " + highestDrain 
                            + " by " + packageWithHighestDrain);
                }
                if (cpuFgTime > cpuTime) {
                    if (DEBUG && cpuFgTime > cpuTime + 10000) {
                        Log.i(TAG, "WARNING! Cputime is more than 10 seconds behind Foreground time");
                    }
                    cpuTime = cpuFgTime; // Statistics may not have been gathered yet.
                }
                power /= 1000;

                // Process wake lock usage
                Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u.getWakelockStats();
                for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry
                        : wakelockStats.entrySet()) {
                    Uid.Wakelock wakelock = wakelockEntry.getValue();
                    // Only care about partial wake locks since full wake locks
                    // are canceled when the user turns the screen off.
                    BatteryStats.Timer timer = wakelock.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
                    if (timer != null) {
                        wakelockTime += timer.getTotalTimeLocked(uSecTime, which);
                    }
                }
                wakelockTime /= 1000; // convert to millis

                // Add cost of holding a wake lock
                power += (wakelockTime
                        * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;
                
                // Add cost of data traffic
                long tcpBytesReceived = u.getTcpBytesReceived(mStatsType);
                long tcpBytesSent = u.getTcpBytesSent(mStatsType);
                power += (tcpBytesReceived+tcpBytesSent) * averageCostPerByte;

                // Add cost of keeping WIFI running.
                long wifiRunningTimeMs = u.getWifiRunningTime(uSecTime, which) / 1000;
                mAppWifiRunning += wifiRunningTimeMs;
                power += (wifiRunningTimeMs
                        * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;

                // Process Sensor usage
                Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u.getSensorStats();
                for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry
                        : sensorStats.entrySet()) {
                    Uid.Sensor sensor = sensorEntry.getValue();
                    int sensorType = sensor.getHandle();
                    BatteryStats.Timer timer = sensor.getSensorTime();
                    long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
                    double multiplier = 0;
                    switch (sensorType) {
                        case Uid.Sensor.GPS:
                            multiplier = mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
                            gpsTime = sensorTime;
                            break;
                        default:
                            android.hardware.Sensor sensorData =
                                    sensorManager.getDefaultSensor(sensorType);
                            if (sensorData != null) {
                                multiplier = sensorData.getPower();
                                if (DEBUG) {
                                    Log.i(TAG, "Got sensor " + sensorData.getName() + " with power = "
                                            + multiplier);
                                }
                            }
                    }
                    power += (multiplier * sensorTime) / 1000;
                }

                if (DEBUG) Log.i(TAG, "UID " + u.getUid() + ": power=" + power);

                // Add the app to the list if it is consuming power
/*                if (power != 0) {
                	BatterySipper bs = new BatterySipper();
                    bs.cpuTime = cpuTime;
                    bs.gpsTime = gpsTime;
                    bs.wifiRunningTime = wifiRunningTimeMs;
                    bs.cpuFgTime = cpuFgTime;
                    bs.wakeLockTime = wakelockTime;
                    bs.tcpBytesReceived = tcpBytesReceived;
                    bs.tcpBytesSent = tcpBytesSent;

                }*/

    /*            if (u.getUid() == Process.WIFI_UID) {
                    mWifiPower += power;
                } else if (u.getUid() == Process.BLUETOOTH_GID) {
                    mBluetoothPower += power;
                } else {
                    if (power > mMaxPower) mMaxPower = power;
                    mTotalPower += power;
                }*/
                mTotalPower += power;
                if (DEBUG) Log.i(TAG, "Added power = " + power);
                //Utils.writeLog(u.getUid() + " processAppUsage:" + mTotalPower, true);
                Utils.writeLog("processAppUsage:" + mTotalPower, true);
			}
            
        }
    }
	
	public void updateStatsPeriod(long duration) {
        String durationString = Utils.formatElapsedTime(duration / 1000);
    }
    
	public void load() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);
            mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }
	public double getCapacity() {
		return mPowerProfile.getBatteryCapacity();
	}
	
}
