package com.dtu.dtushimmer;

import com.dtu.dtushimmer.ShimmerService.LocalBinder;
import com.shimmerresearch.driver.Shimmer;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static String TAG = "MainActivity";
	static ShimmerService mService;
	boolean mServiceBind = false;
	protected boolean mBound = true;
	private int mShimmerConnected = 0;
	public Context mContext;
	private BluetoothAdapter mBluetoothAdapter = null;
	static String deviceName1 = "Shimmer475A";
	static String bluetoothAddress1 = "00:06:66:A0:47:5A";
	private double[] samplingRates = new double[] { 10, 51.2, 102.4, 128,
			170.7, 204.8, 256, 512 };

	private TextView textView1;
	private TextView textView2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mService.sendMessageToUI("Hey! You changed my TextView!");
			}
		});

		// Start ShimmerService if not already running
		if (!isMyServiceRunning()) {
			Intent intent = new Intent(this, ShimmerService.class);
			startService(intent);
			if (mBound == true) {
				bindService(intent, mServiceConnection,
						Context.BIND_AUTO_CREATE);
				mBound = false;
			}
		}

		// Check if device supports Bluetooth
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this,
					"Device does not support Bluetooth\nExiting...",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mBound == false) {
			unbindService(mServiceConnection);
			mBound = true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuItem connectItem = menu.findItem(R.id.action_connect);
		if (mShimmerConnected == 2) {
			connectItem.setEnabled(true);
			connectItem.setTitle(R.string.action_disconnect);
		} else if (mShimmerConnected == 1) {
			connectItem.setEnabled(false);
			connectItem.setTitle(R.string.action_connect);
		} else {
			connectItem.setEnabled(true);
			connectItem.setTitle(R.string.action_connect);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_settings:
			break;
		case R.id.action_connect:
			if (mShimmerConnected == 2) {
				mService.disconnectDevice(bluetoothAddress1);
			} else {
				if (mBluetoothAdapter.isEnabled()) {
					mService.connectShimmer(bluetoothAddress1, deviceName1, samplingRates[7]);
				} else {
					Toast.makeText(mContext, "Bluetooth not enabled.",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected ServiceConnection mServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// TODO Auto-generated method stub
			Log.d("ShimmerService", "Service connected");
			LocalBinder binder = (ShimmerService.LocalBinder) service;
			mService = binder.getService();
			mServiceBind = true;
			mService.setHandlerMain(mHandler);
			// update the view
		}

		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			mServiceBind = false;
		}
	};

	protected boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.shimmerresearch.service.ShimmerServiceCBBC"
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	// Define the Handler that receives messages from the thread and update the
	// progress
	private final Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case ShimmerService.CHANGE_TEXTVIEW:
				Log.i(TAG, "Handler: CHANGE_TEXTVIEW");
				textView1.setText(msg.getData().getString(
						ShimmerService.MESSAGE_KEY));
				break;
			case Shimmer.MESSAGE_READ:
				break;
			case Shimmer.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case Shimmer.STATE_CONNECTED: // Device connected
					mShimmerConnected = 2;
					textView1.setText(R.string.text_connected);
					textView2.setText("Sampling Rate:" + Double.toString(mService
									.getSamplingRate()) + " Hz");
					Log.i(TAG, "Handler: MESSAGE_STATE_CONNECTED");
					break;
				case Shimmer.MSG_STATE_FULLY_INITIALIZED:
					Log.i(TAG, "Handler: MESSAGE_STATE_CONNECTING");
					break;
				case Shimmer.STATE_CONNECTING: // Device connecting
					mShimmerConnected = 1;
					textView1.setText(R.string.text_connecting);
					Log.i(TAG, "Handler: MESSAGE_STATE_CONNECTING");
					break;
				case Shimmer.STATE_NONE: // Device not connected
					mShimmerConnected = 0;
					textView1.setText(R.string.text_not_connected);
					Log.i(TAG, "Handler: MESSAGE_STATE_NONE");
					break;
				}
			default:
				// Toast.makeText(mContext, "Message type not recognized",
				// Toast.LENGTH_SHORT).show();
				Log.i(TAG, "Message that there is no case for was received");
				break;
			}

		}
	};

}
