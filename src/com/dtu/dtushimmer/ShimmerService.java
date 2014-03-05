package com.dtu.dtushimmer;

import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.driver.Shimmer;

public class ShimmerService extends Service {

	private static String TAG = "ShimmerService";
	public static String MESSAGE_KEY = "SERVICE_MESSAGE";
	public static final int CHANGE_TEXTVIEW = 0;
	private final IBinder mBinder = new LocalBinder();
	private Handler mHandlerMain = null;
	private static Shimmer mShimmerDevice=null;
	public HashMap<String, Object> mMultiShimmer = new HashMap<String, Object>(
			7);
	public Context mContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		mContext=this;
		Toast.makeText(mContext, "My Service Started", Toast.LENGTH_LONG).show();
		super.onCreate();

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public ShimmerService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return ShimmerService.this;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(mContext, "My Service Stopped", Toast.LENGTH_LONG).show();
		super.onDestroy();
	}

	public void setHandlerMain(Handler handler) {
		mHandlerMain = handler;
	}

	public void sendMessageToUI(String string) {
		if (mHandlerMain != null) {
			Bundle bundle = new Bundle();
			Message message = mHandlerMain.obtainMessage(CHANGE_TEXTVIEW);
			bundle.putString(ShimmerService.MESSAGE_KEY, string);
			message.setData(bundle);
			mHandlerMain.sendMessage(message);
		}
		Log.i(TAG, "Send message to UI");
	}

	public void connectShimmer(String bluetoothAddress, String selectedDevice,double samplingRate) {
		Log.d("Shimmer", "net Connection");
		mShimmerDevice = new Shimmer(mContext, mShimmerHandler, selectedDevice, samplingRate, 3, 0, Shimmer.SENSOR_ECG|Shimmer.SENSOR_ACCEL, false);
		mShimmerDevice.connect(bluetoothAddress, "default");
//		mMultiShimmer.remove(bluetoothAddress);
//		if (mMultiShimmer.get(bluetoothAddress) == null) {
//			mMultiShimmer.put(bluetoothAddress, shimmerDevice);
//			((Shimmer) mMultiShimmer.get(bluetoothAddress)).connect(
//					bluetoothAddress, "default");
//		}
	}
	
	public void disconnectDevice(String bluetoothAddress){
//		Collection<Object> colS = mMultiShimmer.values();
//		Iterator<Object> iterator = colS.iterator();
//		while (iterator.hasNext()) {
//			Shimmer stemp = (Shimmer) iterator.next();
//			if (stemp.getShimmerState() == Shimmer.STATE_CONNECTED
//					&& stemp.getBluetoothAddress().equals(bluetoothAddress)) {
//				stemp.stop();
//
//			}
//		}
		mShimmerDevice.stop();
	}
	
	
	public void setSamplingRate(String bluetoothAddress, double rate){
		mShimmerDevice.writeSamplingRate(rate);
	}
	
	public double getSamplingRate(){
		return mShimmerDevice.getSamplingRate();
	}

	public final Handler mShimmerHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Shimmer.MESSAGE_READ:
				Log.i(TAG, "Handler: MESSAGE_READ");
				break;
			case Shimmer.MESSAGE_STATE_CHANGE:
				Log.i(TAG, "Handler: MESSAGE_STATE_CHANGE");
				mHandlerMain.obtainMessage(Shimmer.MESSAGE_STATE_CHANGE,
						msg.arg1, -1, msg.obj).sendToTarget();
				break;
			case Shimmer.MESSAGE_TOAST:
				Toast.makeText(mContext, msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_LONG).show();
				break;
			case Shimmer.MESSAGE_STOP_STREAMING_COMPLETE:
				break;
			}
		}
	};

}
