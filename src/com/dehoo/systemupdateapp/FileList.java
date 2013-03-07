package com.dehoo.systemupdateapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dehoo.systemupdateapp.NetworkList.Item;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FileList extends ListActivity {
	
	private static final String TAG = "cyTest";

	private List<Map<String, String>> mTargetList;
	private Util mUtil;
	private LocalHandler mLocalHandler;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_list_main);
		
		Log.d(TAG, "come to FileList Activity");
		
		mLocalHandler = new LocalHandler();
		mUtil = new Util();
		mTargetList = mUtil.getFileListDataSortedAsync(Util.ROOT_PATH, "zip");
		if (mTargetList != null) {
			Log.d(TAG, "mTargetList is not null ");
			Log.d(TAG, "mTargetList's size is : " + mTargetList.size());
			for (int i = 0; i < mTargetList.size(); i++) {
				Log.d(TAG, "mTargetList.filename = " + mTargetList.get(i).get("filename"));
				Log.d(TAG, "mTargetList.filename = " + mTargetList.get(i).get("filepath"));
				
			}
		}

		SimpleAdapter adapter = new SimpleAdapter(this, mTargetList,
				R.layout.file_list, new String[] { "filename", "filepath" },
				new int[] { R.id.filename, R.id.filepath });
		this.setListAdapter(adapter);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// 获得被点击项所代表的File对象
		Log.v(TAG, "-------onListItemClick");
		Log.v(TAG, "onListItemClick"+position);
		Map<String, String> map = mTargetList.get(position);
		String filepath = map.get("filepath");
		Log.d(TAG, "local file's filepath = "+filepath);
		
		if(filepath != null){
			mLocalHandler.obtainMessage(MessageModel.LOCAL_FILE_UPDATE, filepath).sendToTarget();
		}
		// 传递filepath到固件或APP安装模组
	}
	
	@SuppressLint("HandlerLeak")
	class LocalHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
			case MessageModel.LOCAL_FILE_UPDATE :
				String path = msg.obj.toString();
				Log.d("cyTest", "download file path is ："+path);
				mUtil.ExcuteUpgradeAction(FileList.this, path);
				break;
			}
		}
	}

}
