package com.dehoo.systemupdateapp;

import java.util.List;
import java.util.Map;

import com.dehoo.systemupdateapp.config.MessageModel;
import com.dehoo.systemupdateapp.utils.Util;

import android.annotation.SuppressLint;
import android.app.ListActivity;
// modify by zhanmin 13.03.07
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
// modify by zhanmin 13.03.07 end
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FileList extends ListActivity {
	
	private static final String TAG = "cyTest";

	private List<Map<String, String>> mTargetList; // 固件包存储类
	private Util mUtil; // 工具类
	private LocalHandler mLocalHandler;
	private SimpleAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_list_main);
		
		mUtil = new Util();
		mLocalHandler = new LocalHandler();
		mLocalHandler.obtainMessage(MessageModel.GET_LOCAL_STORAGE_FIRMWARE).sendToTarget();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		/** 注册外接设备状态改变监听广播  add by zhanmin 13.03.07 **/
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addDataScheme("file");
		registerReceiver(mMountReceiver, intentFilter);
	}

	/**
	 * 注销外接设备状态改变监听广播
	 * add by zhanmin 13.03.07
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		/** 注销外接设备状态改变监听广播  add by zhanmin 13.03.07 **/
		if (mMountReceiver != null) {
			unregisterReceiver(mMountReceiver);
		}
	}

	/**
	 * 监听外接设备的状态改变
	 * add by zhanmin 13.03.07
	 */
	private BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Uri uri = intent.getData();
			String path = uri.getPath();
			if (action == null || path == null)
				return;
			if (action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_SCREEN_OFF)) {
				mLocalHandler.obtainMessage(MessageModel.RELOAD_DATA_FROM_EXTERNAL).sendToTarget();
			}
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// 获得被点击项所代表的File对象
		Log.v(TAG, "FileList.onListItemClick's position is " + position);
		Map<String, String> map = mTargetList.get(position);
		String filepath = map.get("filepath");
		Log.d(TAG, "local file's filepath = "+filepath);
		if(filepath != null){
			mLocalHandler.obtainMessage(MessageModel.LOCAL_FILE_UPDATE, filepath).sendToTarget();
		}
		// 传递filepath到固件或APP安装模组
	}
	
	/**
	 * 消息处理类
	 * @author zhanmin
	 *
	 */
	@SuppressLint("HandlerLeak")
	class LocalHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
			case MessageModel.LOCAL_FILE_UPDATE : // 本地固件升级
				String path = msg.obj.toString();
				Log.d(TAG, "local file path is ："+path);
				mUtil.ExcuteUpgradeAction(FileList.this, path);
				break;
			case MessageModel.RELOAD_DATA_FROM_EXTERNAL: // 当外接设备状态改变时重新加载数据
				mTargetList.clear();
				mTargetList = mUtil.getFileListDataSortedAsync(FileList.this, Util.ROOT_PATH, "zip");
				if (mTargetList != null) {
					Log.d(TAG, "mTargetList's size is : " + mTargetList.size());
				}
				mAdapter = new SimpleAdapter(FileList.this, mTargetList, R.layout.file_list, new String[] { "filename", "filepath" }, new int[] { R.id.filename, R.id.filepath });
				FileList.this.setListAdapter(mAdapter);
				break;
			case MessageModel.GET_LOCAL_STORAGE_FIRMWARE: // 获取接入设备的本地存储里的固件包 
				mTargetList = mUtil.getFileListDataSortedAsync(FileList.this, Util.ROOT_PATH, "zip");
				if (mTargetList != null) {
					Log.d(TAG, "mTargetList's size is : " + mTargetList.size());
				}
				mAdapter = new SimpleAdapter(FileList.this, mTargetList, R.layout.file_list, new String[] { "filename", "filepath" }, new int[] { R.id.filename, R.id.filepath });
				FileList.this.setListAdapter(mAdapter);
				break;
			}
		}
	}

}
