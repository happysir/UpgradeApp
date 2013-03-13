package com.dehoo.systemupdateapp;

//钟何亮 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dehoo.systemupdateapp.config.MessageModel;
import com.dehoo.systemupdateapp.entity.Item;
import com.dehoo.systemupdateapp.utils.DownLoadSoft;
import com.dehoo.systemupdateapp.utils.NetworkService;
import com.dehoo.systemupdateapp.utils.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NetworkList extends Activity implements OnClickListener {
	
	private static final String TAG = "NetworkList";
	
	private TextView systemFirmware;
//	private ProgressBar firmwareBar;
	private int firmDownPro = 0;
	private ListView lv = null;

	private List<Item> list; //可以app列表
	private Item mItem = null; //升级固件
	private List<Item> firmwareList; // 获取的固件信息
	private MyAdapter adapter;
	private List<HashMap<String, String>> mUpdateInfo;
	private NetworkListHandler mNetworkListHandler;
	private DownLoadThread iThread; //下载线程
	private Util mUtil;
	private NetworkService mNetworkService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network_list);

		mUtil = new Util();
		mNetworkService = new NetworkService();

		// 固件信息，文本
		systemFirmware = (TextView) findViewById(R.id.system_firmware_info);
	//	firmwareBar = (ProgressBar)findViewById(R.id.firmware_progressbar);
		systemFirmware.setFocusable(true);
		systemFirmware.setClickable(true);
		systemFirmware.requestFocus();
		
	//	firmwareBar.getBackground().setAlpha(100);  
		systemFirmware.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
				Log.d("cyTest", "hasFocus = "+hasFocus);
				
				if(hasFocus){
					systemFirmware.setBackgroundColor(NetworkList.this.getResources().getColor(R.color.button_bg));
				}else
					systemFirmware.setBackgroundColor(NetworkList.this.getResources().getColor(R.color.color_black));
					
				
			}
		});
		
		systemFirmware.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 
				Log.e("dehoo","firmware OnClick");
				if(mItem !=null){
					DownLoadSoft mDownLoadSoft = new DownLoadSoft(NetworkList.this, mNetworkListHandler, mUtil);
					mDownLoadSoft.showDownloadDialog(
							mItem.realname, mItem.url,
							mItem.hashnumber);	
					
				}
			}
		});
		
		
		// app list
		lv = (ListView) this.findViewById(R.id.update_app_list);

		/**
		 * APP的list列表监听
		 */
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Item item = list.get(arg2);

				if (!item.v) {
					if (item.progress == -1) {
						item.b = !item.b; // 加入/退出-->下载队列
						Log.d("dehoo", "Click:" + item.realname);
					}
				}
				initAdapter();
			}
		});

		mNetworkListHandler = new NetworkListHandler();
		new GetInputStreamThrea().start();
		iThread = new DownLoadThread();
		iThread.start();

	}

	// 得到服务器上，固件及APP信息
	class GetInputStreamThrea extends Thread {
		@Override
		public void run() {
			mUpdateInfo = mNetworkService.getDownList();
			if (mUpdateInfo.size() == 0) {
				Log.d(TAG, "服务器没有可用更新");
				return;
			}
			mNetworkListHandler.sendEmptyMessage(MessageModel.PARSE_XML_OK);
		}
	}

	/**
	 *  
	 * @author dehoo-jiangmq 2013-3-8下午6:19:55
	 * 循环线程，用于扫描applist，下载已选app
	 *
	 */
	class DownLoadThread extends Thread {
		DownLoadSoft mDownLoadSoft = new DownLoadSoft(NetworkList.this, mNetworkListHandler, mUtil);
		@Override
		public void run() {
			do {
				if (list != null)
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).b) {
							// downloadList.get(i).b = false;
							mDownLoadSoft.showDownloadDialog(
									list.get(i).realname, list.get(i).url,
									list.get(i).hashnumber);
							while (!list.get(i).v) {
							}
						 if(i==list.size())
						 {
							 
						 }
							// list.get(i).b = false;
						}
					}
			} while (true);
		}
	}
	 
	@SuppressLint("HandlerLeak")
	class NetworkListHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int progress;
			String appname;
			switch (msg.what) {
			case MessageModel.PARSE_XML_OK:
				init();
				Log.d(TAG, "解析正确xml文件后");
				break;
			case MessageModel.PROGRESS_NUMBER:
				progress = msg.arg1;
				appname = msg.obj.toString();
				
				 
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).realname.equals(appname)) {
						list.get(i).progress = progress;
						break;
					}
					if(i==list.size()-1){
						firmDownPro = progress;
						 
					}
				}
				break;
			case MessageModel.DOWNLOAD_OK:
				// 下载完成，安装程序
				Log.d("cyTest", "安装：" + msg.obj.toString());
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).realname.equals(msg.obj.toString())) {
						Log.d("cyTest", "安装：" + msg.obj.toString());
						if (msg.arg1 == 1)
							mUtil.ExcuteUpgradeAction(NetworkList.this, "/mnt/sdcard/" + msg.obj.toString());
						list.get(i).progress = -1;
						list.get(i).b = false;
						list.get(i).v = true;
					}
				}
				break;
			case MessageModel.CHECK_NETWORK_DOWNLOAD_FILE_TYPE:
				String path = msg.obj.toString();
				Log.d("cyTest", "download file path is ：" + path);
//				 mUtil.ExcuteUpgradeAction(NetworkList.this, path);
				break;
			}
			initAdapter();
		}
	}

	/**
	 * Function: onClick  
	 * @author dehoo-jiangmq 2013-3-8下午6:19:55
	 *
	 * 点击事件监听
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.system_firmware_info:
			Log.v(TAG, "system_firmware_info");
			break;
		default:
			break;
		}
		initAdapter();
	}

	/**
	 * Function: init  
	 * @author dehoo-jiangmq 2013-3-8下午6:19:55
	 * 
	 *
	 * 从服务器解析的升级信息配置app列表list
	 */
	private void init() {
		if (list == null)
			list = new ArrayList<Item>();
		else
			list.clear();

		if (firmwareList == null)
			firmwareList = new ArrayList<Item>();

		Item iItem = null;

		Log.d(TAG, "mUpdateInfo.size = " + mUpdateInfo.size());

		for (int i = 0; i < mUpdateInfo.size(); i++) {
			iItem = new Item(mUpdateInfo.get(i).get("realname"), Integer
					.valueOf(mUpdateInfo.get(i).get("version")).intValue(),
					mUpdateInfo.get(i).get("url"), mUpdateInfo.get(i).get( "hashnumber"), mUpdateInfo.get(i) .get("versionname"), mUpdateInfo.get(i).get( "packagename"));

			int numOfPoint = iItem.realname.lastIndexOf(".");
			if (numOfPoint == -1) {
				continue;
			}

			String fileExt = iItem.realname.substring(numOfPoint + 1, iItem.realname.length());
			if (fileExt.equals("zip")) {
				firmwareList.add(iItem);
			} else {
				Log.d(TAG, iItem.realname);
				if (appCheck(iItem))
					list.add(iItem);
				
			}
		}

		iItem = firmwareCheck();
		if (iItem.realname.equals("system")) {
//		if (true) {
			Log.v("dehoo", "No available firmware");
			systemFirmware.setText(R.string.no_firware);
			systemFirmware.setTextColor(NetworkList.this.getResources().getColor(R.color.button_bg2));
			systemFirmware.setEnabled(false);
			systemFirmware.setFocusable(false);
		} 
		else {
			mItem = iItem;
			systemFirmware.setText(iItem.realname);
			systemFirmware.setFocusable(true);
			systemFirmware.setBackgroundColor(NetworkList.this.getResources().getColor(R.color.button_bg));
		}

		initAdapter();
	}
	
	/**
	 * Function: firmwareCheck  
	 * @author dehoo-jiangmq 2013-3-8下午6:19:55
	 *
	 * 检测系统固件版本
	 */
	public Item firmwareCheck() {
		String sysVersionName = android.os.Build.VERSION.RELEASE;
		String sysVersion = android.os.Build.VERSION.CODENAME;
		Log.d(TAG, "sysVersionName:" + sysVersionName + "---sysVersion:" + sysVersion);
		Item fItem = new Item("system", 0, null, null, sysVersionName, null);
		for (int j = 0; j < firmwareList.size(); j++) {
			if(firmwareList.get(j).versionName==null){
				 continue ;
			}
			if (0 > fItem.versionName.compareTo(firmwareList.get(j).versionName)) {
				fItem = firmwareList.get(j);
			}
		}
		return fItem;
	}
	
	/**
	 * Function: appCheck  
	 * @author dehoo-jiangmq 2013-3-8下午6:19:55
	 *
	 * 扫描系统中所有APP，检测App是否需要更新
	 */
	public boolean appCheck(Item iItem) {
		String appPackageName = null;
//		String appVersionName = null;
		int appVersionCode = 0;
		List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		for (int j = 0; j < packages.size(); j++) {
			PackageInfo packageInfo = packages.get(j);
			appPackageName = packageInfo.packageName;
	//		appVersionName = packageInfo.versionName;
			appVersionCode = packageInfo.versionCode;
 
			if (appPackageName.equals(iItem.packageName)) {
				Log.d("dehoo", iItem.realname + ":code" + appVersionCode);
				if (appVersionCode < iItem.version) {
					return true;
				} else
					return false;
			}
		}
		return true;
	}

	// 初始化、刷新list
	public void initAdapter() {
		if (adapter == null) {
			adapter = new MyAdapter();
			lv.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	// app list 适配器
	class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Item getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			ViewHolder holder;
			if (view == null || (holder = (ViewHolder) view.getTag()) == null) {
				view = View.inflate(NetworkList.this, R.layout.app_list, null);
				holder = new ViewHolder();
				holder.tv = (TextView) view.findViewById(R.id.app_name);

				holder.vs = (TextView) view.findViewById(R.id.app_version);
				holder.as = (TextView) view.findViewById(R.id.app_schedule);

				holder.pb = (ProgressBar) view
						.findViewById(R.id.app_down_probar);
				view.setTag(holder);
			}
			Item item = getItem(position);
			// 配置app名字、版本号
			holder.tv.setText(item.realname);
			holder.vs.setText("version:" + item.version);
			// 显示可下载
			holder.as.setText(R.string.download);
			// 正在下载阶段
			if (-1 < item.progress) {
				item.b = false;
				holder.pb.setVisibility(View.VISIBLE);
				holder.as.setText(item.progress + "%");
				holder.pb.setProgress(item.progress);
				return view;
			}
			// 等待下载
			else if (item.b) {
				holder.as.setText(R.string.cancel_download);
				holder.pb.setVisibility(View.INVISIBLE);
			}
			// 下载完成，隐藏
			else if (item.v) {
				holder.pb.setVisibility(View.INVISIBLE);
				holder.as.setText(R.string.downloaded);
			}
			return view;
		}
	}

	class ViewHolder {
		public TextView tv = null; // app名字
		public TextView vs = null; // app版本号

		public ProgressBar pb = null; // 下载进度条
		public TextView as = null; // 显示下载进度
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

}
