package com.dehoo.systemupdateapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 系统升级入口程序
 * 
 * @author work
 * 
 */
public class MainActivity extends Activity {

	private static final String TAG = "cyTest";
	private Util mUtil;
	private boolean isNetworkAvailable; // 网络是否可用
	private ConnectionReceive mConnectionReceive;
//	private List<Map<String, String>> mTargetList; // 存储以zip结尾的所有文件
	private Button localButton; // 本地安装
	private Button networkButton; // 网络安装
	private SystemUpdateHandler mSystemUpdateHandler;
	private boolean isNeedUpdate; // 是否有最新版本

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.mode_selection);

			// --------------------------hdong---------------------------------
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());

		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects() // 探测SQLite数据库操作
				.penaltyLog() // 打印logcat
				.penaltyDeath().build());
		// --------------------------hdong---------------------------------	
		
		mUtil = new Util();
		
		mSystemUpdateHandler = new SystemUpdateHandler();

		modeSelect();

		isNetworkAvailable = mUtil.isNetworkAvailable(MainActivity.this);
		Log.d(TAG, "onCreate.isNetworkAvailable = " + isNetworkAvailable);
		if (isNetworkAvailable) {
			// 设置界面网络升级可点击
			networkButton.setClickable(true);
			networkButton.setEnabled(true);
			// add by dehoo-jiangmq 17.03.04
			networkButton.setFocusable(true);
			//  add by jiangmq 17.03.04 end
			
		
		} else {
			// 设置界面网络升级不可点击
			networkButton.setClickable(false);
			networkButton.setEnabled(false);
			// add by dehoo-jiangmq 17.03.04
			networkButton.setFocusable(false);
			//  add by jiangmq 17.03.04 end
		
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mConnectionReceive = new ConnectionReceive();
		registerReceiver(mConnectionReceive, intentFilter);

	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addDataScheme("file");
		registerReceiver(mMountReceiver, intentFilter);
	}

	private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Uri uri = intent.getData();
			String path = uri.getPath();

			if (action == null || path == null)
				return;

			path = pathTransferForJB(path);

			// if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
			//
			// } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
			//
			// } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
			//
			// } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
			//
			// }
		}
	};

	private String pathTransferForJB(String path) {
		String pathout = path;

		if (path.startsWith("/storage/sd")) {
			if (path.contains("/storage/sdcard0")) {
				pathout = path.replaceFirst("/storage/sdcard0", "/mnt/sdcard");
			} else {
				pathout = path.replaceFirst("/storage/sd", "/mnt/sd");
			}
		}

		return pathout;
	}

	/**
	 * 接收网络状态改变广播
	 * 
	 * @author work
	 * 
	 */
	private class ConnectionReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {

			Log.d(TAG, "---ConnectionReceive.onReceive---");
			Log.d(TAG, "ConnectionReceive.onReceive's network status is " + mUtil.isNetworkAvailable(MainActivity.this));

			if (mUtil.isNetworkAvailable(MainActivity.this)) {
				// 设置界面网络升级可点击
				mSystemUpdateHandler.obtainMessage(MessageModel.NETWORK_UPDATE_BUTTON_USEFUL).sendToTarget();
			} else {
				// 设置界面网络升级不可点击
				mSystemUpdateHandler.obtainMessage(MessageModel.NETWORK_UPDATE_BUTTON_NOT_USEFUL).sendToTarget();
			}
		}

	}
	
	/**
	 * 消息处理类
	 * @author work
	 *
	 */
	class SystemUpdateHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what){
			case MessageModel.NETWORK_UPDATE_BUTTON_USEFUL : // 有网络
				networkButton.setClickable(true);
				networkButton.setEnabled(true);
				 
				// add by dehoo-jiangmq 17.03.04
				networkButton.setFocusable(true);
				//  add by jiangmq 17.03.04 end

				break;
			case MessageModel.NETWORK_UPDATE_BUTTON_NOT_USEFUL : // 无网络
				networkButton.setClickable(false);
				networkButton.setEnabled(false);
			 
				// add by dehoo-jiangmq 17.03.04
				networkButton.setFocusable(false);
				//  add by jiangmq 17.03.04 end
			
				break;
			case MessageModel.GET_UPDATE_RESULT: // 检查服务器是否有最新版本
//				String url = "http://202.102.55.140/NetworkType.aspx";
//				String url = "http://apple.lexun.com/test.html";
				if(mUtil.isNetworkAvailable(MainActivity.this)){
//					isNeedUpdate = mUtil.isNeedUpdate(url, MainActivity.this);
					if(true){
						// 显示所有更新内容
						Intent searchIntent = new Intent(MainActivity.this, NetworkList.class);
						startActivity(searchIntent);
						
					}
				}
				break;
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mConnectionReceive != null) {
			unregisterReceiver(mConnectionReceive);
		}
		if (mMountReceiver != null) {
			unregisterReceiver(mMountReceiver);
		}
	}

	/**
	 * initView 初始化View
	 */
	private void modeSelect() {


		localButton = (Button) MainActivity.this.findViewById(R.id.localBn);
		networkButton = (Button) MainActivity.this.findViewById(R.id.networkBn);
		localButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent searchIntent = new Intent(MainActivity.this, FileList.class);
				startActivity(searchIntent);
			}

		});

		networkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSystemUpdateHandler.obtainMessage(MessageModel.GET_UPDATE_RESULT).sendToTarget();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	


}
