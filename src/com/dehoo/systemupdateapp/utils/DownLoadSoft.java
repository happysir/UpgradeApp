/**
 * 
 */
package com.dehoo.systemupdateapp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.dehoo.systemupdateapp.config.MessageModel;
import com.dehoo.systemupdateapp.utils.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

/**
 * 网络下载模组，通过传递进本类的fileName,url,hashNumber下载该文件
 * 
 * @author dehoo-ZhongHeliang 2013-2-28上午9:31:56
 * @version jdk 1.6; sdk 4.2.0
 */
public class DownLoadSoft {
	
	private final static boolean DEBUG = true;
	private final static String TAG = "DownLoadSoft";
	private Handler handler;
	private Context mContext;
	// 下载需要的信息
	private String mUrlString = null;
	private String mFileName = null;
	private String mhashdown = null;
	// 保存下载文件的路径
	private String mSavePath;
	/* 记录进度条数量 */
	private static int progress;
	private static int progressSign =0;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;
	private long sdsize = 0;
	private long length = 0;
	/* 哈希值计算 */
	private MessageDigest md5;
	private static String hashnumber = null;
	
	private Util mUtil;
	
	//======================== 下面是本类的方法 ========================//
	/**
	 * Function:指定上下文
	 * @author dehoo-ZhongHeliang 2013-2-28上午9:58:56
	 */
	public DownLoadSoft(Context context,Handler pHandler, Util util)
	{
		this.mContext = context;
		this.handler = pHandler;
		this.mUtil = util;
	}	
	
	/**
	 * Function: showDownloadDialog
	 * 显示下载的对话框
	 * @author dehoo-ZhongHeliang 2013-3-4下午2:02:23
	 * @param name 需要下载的文件名
	 * @param url 需要下载文件的url
	 * @param hash 需要下载文件的hash值
	 */
	public void showDownloadDialog(String name,String url,String hash)
	{
		this.mUrlString = url;
		this.mhashdown = hash;
		this.mFileName = name;
		if (DEBUG)
		{
			Log.d(TAG, "mFileName:"+mFileName);
			Log.d(TAG, "mUrlString:"+mUrlString);
			Log.d(TAG, "mhashdown:"+mhashdown);
		}
		
		// 下载文件
		downloadApk();
	}

	/**
	 * Function: downloadApk
	 * 启动下载文件线程
	 * @author dehoo-ZhongHeliang 2013-2-28上午9:58:56
	 */
	private void downloadApk()
	{
		// 启动下载线程
		new downloadApkThread().start();
	}
	
	/**
	 * 此类为线程，负责下载文件
	 * @author dehoo-ZhongHeliang 2013-3-4下午2:04:12
	 * @version jdk 1.6; sdk 4.2.0
	 */
	private class downloadApkThread extends Thread
	{
		public void run()
		{		
			try
			{		
				// 步骤一：判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					// 步骤二：获得存储卡的路径，并设置存储位置为/sdcard
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "";
					
					// 步骤三：判断文件目录是否存在
					File file = new File(mSavePath);
					if (!file.exists())
					{
						file.mkdir();
						Log.d(TAG, "#######  mkdir   #####");
					}
					// 断点测试路径是否正确
					if (DEBUG)
					{
						Log.d(TAG,"Path :"+file.getName());
					}			
					// 步骤四：计算存储区可以存储空间的大小
					sdsize = mUtil.getFreeSpaceInB(mSavePath);
					if (DEBUG)
					{
						Log.d(TAG,"计算sd卡可用空间大小："+""+sdsize+"KB");
					}
					// 创建连接
					URL mUrl = new URL(mUrlString);
					HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
					conn.connect();
					// 获取文件大小
					length = conn.getContentLength();					
					// 步骤五：比较文件和下载文件的大小
					if (length >= sdsize)
					{
						Log.d(TAG, "空间不够，请释放空间");
						return;
					}
					// 断点测试，是否能够连接到正确网站并计算文件大小
					if (DEBUG)
					{
						Log.d(TAG,"DOWN："+""+length);
					}
					// 创建输入流
					InputStream is = conn.getInputStream();
					// 步骤六：下载文件到指定目录
					if (DEBUG)
					{
						Log.d(TAG, "mSavePath: "+mSavePath);
						Log.d(TAG, "mFileName: "+mFileName);
					}
					FileOutputStream fos;
					File apkFile;
					apkFile = new File(mSavePath,mFileName);
					fos = new FileOutputStream(apkFile);		
					try
					{
						md5 = MessageDigest.getInstance("MD5");
					}
					catch (NoSuchAlgorithmException e1)
					{
						e1.printStackTrace();
					}
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中,并计算hash值
					 
					do
					{
						// 网络状态，1为网络通，0为不通
						int networkstate = 1;
						if (!mUtil.isNetworkAvailable(mContext))
						{
							networkstate = 0;
							cancelUpdate = true;
							return;
						}
						int numread = is.read(buf);
						// 计算hash值
						if (numread > 0)
						{
							try
							{
								md5.update(buf,0,numread);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 发送进度条信息到界面
						if(progressSign < progress){
						   Message message = new Message();
						   progressSign = progress;
						   message.what = MessageModel.PROGRESS_NUMBER;
						   message.arg1 = progress;
						   message.arg2 = networkstate;
						   message.obj = mFileName;
						handler.sendMessage(message);
						}
						if (numread <= 0)
						{
							// 下载完成，退出模块
							if(DEBUG)
								Log.d(TAG, "哈哈，下载完成了，开始验证hash值！");
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);
					fos.close();
					is.close();
					 
					// 步骤七：根据下载的文件计算其hash值
					try
					{
						hashnumber = mUtil.toHexString(md5.digest());
						// 测试hash值
						if (DEBUG)
						{
							Log.d(TAG,"hash:计算下载文件的hash值是："+hashnumber);
						}
						Message messageok = new Message();
						messageok.what =MessageModel.DOWNLOAD_OK;
						if (mhashdown.equals(hashnumber))
						{				
							if (DEBUG)
							{
								Log.d(TAG,"Handle:下载完成，hash值一致，进入安装模组");
							}
							// 发送文件名和hash值正确消息到界面	
							messageok.obj = mFileName;
							messageok.arg1 = 1;
							Log.d(TAG, "能否成功发送文件名："+messageok.obj.toString());
							handler.sendMessage(messageok);					

						}
						else 
						{
							Log.d(TAG, "Hash值不一致，不能进入安装模块");
							// 发送文件名和hash值错误消息到界面
							messageok.obj = mFileName;
							messageok.arg1 = 0;
							Log.d(TAG, "能否成功发送文件名："+messageok.obj.toString());
							handler.sendMessage(messageok);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}					
				}else {
					Log.v(TAG, "######请注意，没有SD卡！路径被写死在/mnt/sdcard/ ########");	 
				}
			} catch (MalformedURLException e)
			{				 
				e.printStackTrace();
			} catch (IOException e)
			{	 
				e.printStackTrace();
			}
		}//end with run(){}
		
	}
	
}
