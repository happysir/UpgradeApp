package com.dehoo.systemupdateapp;

//钟何亮 
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
// 钟何亮 
//钟何亮 

public class NetworkList extends Activity implements OnClickListener {
	TextView systemFirmware = null;
	ListView lv = null;

	private List<Item> list;
	private List<Item> data;
	private MyAdapter adapter;
	private InputStream mInStream;
	private ParseXml mParseXmlService;
	private List<HashMap<String, String>> mUpdateInfo;
	private NetworkListHandler mNetworkListHandler;
	private DownLoadSoft mDownLoadSoft;
	
	private Util mUtil;
	// 钟何亮 进度条数字
//	private int progress = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.network_list);
		
		mUtil = new Util();
		
		mNetworkListHandler = new NetworkListHandler();
		mDownLoadSoft = new DownLoadSoft(this ,mNetworkListHandler);
		
		systemFirmware = (TextView) findViewById(R.id.system_firmware_info);
		systemFirmware.setText(R.string.no_firware);
		lv = (ListView) this.findViewById(R.id.update_app_list);

		this.findViewById(R.id.selectall).setOnClickListener(this);
		this.findViewById(R.id.inverseselect).setOnClickListener(this);
		this.findViewById(R.id.cancel).setOnClickListener(this);
		this.findViewById(R.id.app_ok1).setOnClickListener(this);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Item item = list.get(arg2);
				// add by dehoo-jiangmq 09.13.04 03/05
				if(!item.v)
				{				 
					item.b = !item.b;			 
				}
				// add by dehoo-jiangmq 09.13.04 03/05
				initAdapter();
			}
		});

		new GetInputStreamThrea().start();

	}
	/**
	 *   线程中去读取服务器上XML信息的到可下载app列表
	 *  
	 */
	class GetInputStreamThrea extends Thread
	{

		@Override
		public void run()
		{
			mUpdateInfo = getDownList();
			if (mUpdateInfo.size() == 3)
			{
				Log.d("NetworkList", "==========正确得到list=====");
			}
			mNetworkListHandler.sendEmptyMessage(MessageModel.PARSE_XML_OK);
		}
		
	}
	
	/**
	 *  handler 接收网络xml解析完成消息
	 *			下载进度
	 *			下载完成
	 *			开始下载
	 *  
	 */
	@SuppressLint("HandlerLeak")
	class NetworkListHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int progress;
			String appname;
			switch (msg.what){
			case MessageModel.PARSE_XML_OK:
				init();
				Log.d("信息接收端", "解析正确xml文件后");
				break;
				
			case MessageModel.PROGRESS_NUMBER:
				  progress = msg.arg1;
				  appname = msg.obj.toString();
				  //add jiangmq 2013-03-07 12:00:00
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).realname.equals(appname)) {
						list.get(i).progress=progress;
						 
						if(100==progress)
							list.get(i).v=true;
					}
					}
				//add jiangmq 2013-03-07 12:00:00
				Log.d("信息接收端", "接收进度条的信息,progress = "+""+progress);
				System.out.println("-------------------");
				break;
			case MessageModel.DOWNLOAD_OK:
				// 下载完成了
				Log.d("信息接收成功", "======下载完成=====");
				Log.d("NeworkList", "下载的文件名是："+msg.obj.toString());
				
				break;
			case MessageModel.NETWORK_URL_DOWNLOAD :
				Item item = (Item)msg.obj;
				Log.d("++++++",item.realname);
				if(item != null){
//					mDownLoadSoft.downloadApk(item, mNetworkListHandler);
					mDownLoadSoft.showDownloadDialog(item.realname, item.url, item.hashnumber);
				}
				break;
			case MessageModel.CHECK_NETWORK_DOWNLOAD_FILE_TYPE :
				String path = msg.obj.toString();
				Log.d("cyTest", "download file path is ："+path);
//				mUtil.ExcuteUpgradeAction(NetworkList.this, path);
				break;
	 
 
			}
			initAdapter();
		}
	}

	/**
	 *  
	 * 界面点击事件接收
	 *  
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.selectall:
			int size1 = list.size();
			for (int i = 0; i < size1; i++) {
				// add by dehoo-jiangmq 09.13.04 03/05
				if(!list.get(i).v)
				{				 
					list.get(i).b = true;				 
				}
				// add by dehoo-jiangmq 09.13.04 03/05
			}
			break;
		case R.id.inverseselect:
			int size2 = list.size();
			for (int i = 0; i < size2; i++) {
				Item item = list.get(i);
				// add by dehoo-jiangmq 09.13.04 03/05
				if(!item.v)
				{				 
					item.b = !item.b;				 
				}
				// add by dehoo-jiangmq 09.13.04 03/05
			}
			break;
		case R.id.cancel:
			int size3 = list.size();
			for (int i = 0; i < size3; i++) {
				list.get(i).b = false;
			}
			break;
		case R.id.app_ok1:
			System.out.println("++OK++");
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).b) {
//					Log.v("APP NAME", list.get(i).realname);
					mNetworkListHandler.obtainMessage(MessageModel.NETWORK_URL_DOWNLOAD, list.get(i)).sendToTarget();
					// add by dehoo-jiangmq 09.13.04 03/05
				//	list.get(i).v=true;
					list.get(i).progress=0;
					// add by dehoo-jiangmq 09.13.04 03/05
				}
			}

			break;
		default:
			break;
		}
		initAdapter();
	}

	/**
	 * Function: getDownList
	 * 从网络上解析一个xml文件，得到它里面的data返回一个list
	 * @author dehoo-ZhongHeliang 2013-3-5下午6:19:55
	 * @return list
	 */
	public List<HashMap<String, String>> getDownList()
	{
		List<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
		// 解析网络上的xml文件
		URL url = null;
		try
		{
			url = new URL("http://192.168.1.110/version.xml");
		}
		catch (MalformedURLException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			mInStream = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//mInStream = ParseXml.class.getClassLoader().getResourceAsStream("version.xml");
		mParseXmlService = new ParseXml();
		try {
			list = mParseXmlService.parseXml(mInStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	/**
	 * 初始化，配置app listView
	 * 
	 * 
	 */
	private void init() {
		if (list == null)
			list = new ArrayList<Item>();
		else
			list.clear();
		if (data == null)
			data = new ArrayList<Item>();

		Item iItem;
		Random random = new Random(100);
		
		Log.d("cyTest", "mUpdateInfo.size = "+mUpdateInfo.size());
		
		for (int i = 0; i < mUpdateInfo.size(); i++) {

			iItem = new Item(mUpdateInfo.get(i).get("realname"), mUpdateInfo.get(i).get("version"), mUpdateInfo.get(i).get("url"), mUpdateInfo.get(i).get("hashnumber"));
			list.add(iItem);
		}
		initAdapter();
	}
		/**
	 * 刷新listview
	 * 
	 * 
	 */
	public void initAdapter() {
		if (adapter == null) {
			adapter = new MyAdapter();
			lv.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}

		int size = list.size();
		data.clear();
		for (int i = 0; i < size; i++) {
			if (list.get(i).b) {

				data.add(list.get(i));
				System.out.println("APP NAME" + "|" + list.get(i).realname);
			} else
				data.remove(list.get(i));
		}
		System.out.println(list.size() + "|" + data.size());

	}
	//自定义app列表listView BaseAdapter适配器
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
				holder.dt = (TextView) view.findViewById(R.id.app_complete);
				holder.vs = (TextView) view.findViewById(R.id.app_version);
				holder.as = (TextView) view.findViewById(R.id.app_schedule);
				holder.cb = (CheckBox) view.findViewById(R.id.item_cb);
				holder.pb = (ProgressBar) view.findViewById(R.id.app_down_probar);
				view.setTag(holder);
			}
			Item item = getItem(position);
			holder.tv.setText(item.realname);
			holder.vs.setText(item.version);
			holder.cb.setChecked(item.b);
			// add by dehoo-jiangmq 09.03.04 03/05
			if(-1!=item.progress)
			{
				holder.cb.setVisibility(View.GONE);				
				item.b=false;
				holder.cb.setClickable(false);			 
				holder.cb.setEnabled(false);		 
				holder.cb.setFocusable(false);
				holder.pb.setVisibility(View.VISIBLE);
				holder.as.setVisibility(View.VISIBLE);
				holder.as.setText(item.progress+"%");
				holder.pb.setProgress(item.progress);
			}
			if(item.v)
			{	
			//	item.progress=-1;
				holder.pb.setVisibility(View.INVISIBLE);	
				holder.dt.setVisibility(View.VISIBLE);
				holder.as.setVisibility(View.INVISIBLE);
			}
			// add by dehoo-jiangmq 09.03.04 03/05
			return view;
		}
	}

	//app listView单条码信息
	class Item {
		public String realname;  //app名字
		public String version;	//app版本号
		public String url;		//下载地址
		public String hashnumber; //哈希值
		public int progress = -1; //下载进度
		public boolean b = false; //CheckBox标准位
		
		public boolean v = false; //下载标准位
		

		public Item(String name, boolean b) {
			this.realname = name;
			this.b = b;
		}
		// add by dehoo-jiangmq 09.03.04 03/05
		public Item(String name, String version, String url, String hashnumber) {
			this.realname = name;
			this.version = version;
			this.url = url;
			this.hashnumber = hashnumber;
			this.b =false;
			this.v =false;
			this.progress = -1;
			
		}
		// add by dehoo-jiangmq 09.03.04 03/05
		public Item() {

		}
	}
	
	class ViewHolder {
		public TextView tv = null;
		public TextView vs = null;
		public CheckBox cb = null;
		public TextView dt = null;
		public ProgressBar pb = null;
		public TextView as =null;
	}

}
