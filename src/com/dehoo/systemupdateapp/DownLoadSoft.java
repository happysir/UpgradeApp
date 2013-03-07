/**
 * 
 */
package com.dehoo.systemupdateapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

/**
 * 网络下载模组，通过解析一个xml文件下载文件
 * 
 * @author dehoo-ZhongHeliang 2013-2-28上午9:31:56
 * @version jdk 1.6; sdk 4.2.0
 */
public class DownLoadSoft
{
	private final static boolean DEBUG = true;
	private final static String TAG = "DownLoadSoft";
	private Handler handler;

	private static Context mContext;
	private static ProgressBar mProgressBar = null;
	private Dialog mDownloadDialog;
	// 发送的广播信息
	private final static String DOWNOK_MSG_BROADCAST = "downok_msg_broadcast";
	// 下载需要的信息
	private String mUrlString = null;
	private String mFileName = null;
	private static String mhashdown = null;
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	// 保存下载文件的路径
	private String mSavePath;
	/* 记录进度条数量 */
	private static int progress;
	/* 是否取消更新 */
	private boolean cancelUpdate = false;
	private long sdsize = 0;
	private long length = 0;
	/* 哈希值计算 */
	private MessageDigest md5;
	private static String hashnumber = null;
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Function:指定上下文
	 * 
	 * @author dehoo-ZhongHeliang 2013-2-28上午9:58:56
	 */
	public DownLoadSoft(Context context, Handler pHandler)
	{
		this.mContext = context;
		this.handler = pHandler;
	}

	/* 处理消息 */
	private static Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			// 下载中
			case DOWNLOAD:
				mProgressBar.setProgress(progress);
				break;
			// 下载完成
			case DOWNLOAD_FINISH:
				Log.d(TAG, "验证hash值是否正确");
				break;
			default:
				break;
			}
		}

	};

	/**
	 * Function: showDownloadDialog 显示下载的对话框
	 * 
	 * @author dehoo-ZhongHeliang 2013-3-4下午2:02:23
	 * @param name
	 *            需要下载的文件名
	 * @param url
	 *            需要下载文件的url
	 * @param hash
	 *            需要下载文件的hash值
	 */
	public void showDownloadDialog(String name, String url, String hash)
	{
		this.mUrlString = url;
		this.mhashdown = hash;
		this.mFileName = name;
		if (DEBUG)
		{
			Log.d(TAG, "mFileName:" + mFileName);
			Log.d(TAG, "mUrlString:" + mUrlString);
			Log.d(TAG, "mhashdown:" + mhashdown);
		}
		// 构造软件下载对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.download_progress, null);
		mProgressBar = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel,
				new OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
						// 设置取消状态
						cancelUpdate = true;
					}
				});
		mDownloadDialog = builder.create();
		// mDownloadDialog.show();
		// 下载文件
		downloadApk();
	}

	/**
	 * Function: downloadApk 启动下载文件线程
	 * 
	 * @author dehoo-ZhongHeliang 2013-2-28上午9:58:56
	 */
	private void downloadApk()
	{
		// 启动下载线程
		new downloadApkThread().start();

	}

	/**
	 * 次类为线程，负责下载文件
	 * 
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
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED))
				{
					// 步骤二：获得存储卡的路径，并设置存储位置为/download
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					mSavePath = sdpath + "";

					// 步骤三：判断文件目录是否存在
					File file = new File(mSavePath);
					if (!file.exists())
					{
						file.mkdir();
						Log.d(TAG, "mkdir");
					}
					// 断点测试路径是否正确
					if (DEBUG)
					{
						Log.d(TAG, "Path :" + file.getName());
					}

					// 步骤四：计算存储区可以存储空间的大小
					sdsize = getFreeSpaceInB(mSavePath);
					if (DEBUG)
					{
						Log.d(TAG, "download:sd卡可用空间大小：" + "" + sdsize);
					}
					// 创建连接
					URL mUrl = new URL(mUrlString);
					HttpURLConnection conn = (HttpURLConnection) mUrl
							.openConnection();
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
						Log.d(TAG, "DOWN：" + "" + length);
					}
					// 创建输入流
					InputStream is = conn.getInputStream();

					// 步骤六：下载文件到指定目录
					if (DEBUG)
					{
						Log.d(TAG, "mSavePath: " + mSavePath);
						Log.d(TAG, "mFileName: " + mFileName);
					}
					FileOutputStream fos;
					File apkFile;
					apkFile = new File(mSavePath, mFileName);

					fos = new FileOutputStream(apkFile);

					try
					{
						md5 = MessageDigest.getInstance("MD5");
					}
					catch (NoSuchAlgorithmException e1)
					{
						e1.printStackTrace();
						Log.d(TAG, "MD%");
					}
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中,并计算hash值

					do
					{
						int numread = is.read(buf);
						// 计算hash值
						if (numread > 0)
						{
							try
							{
								md5.update(buf, 0, numread);
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
						Message message = new Message();
						message.what = MessageModel.PROGRESS_NUMBER;
						message.arg1 = progress;
						message.obj = mFileName;
						handler.sendMessage(message);

						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0)
						{
							// 下载完成，退出模块
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					}
					while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();

					// 步骤七：根据下载的文件计算其hash值
					try
					{
						hashnumber = toHexString(md5.digest());
						if (mhashdown.equals(hashnumber))
						{
							if (DEBUG)
							{
								Log.d(TAG, "Handle:下载完成，hash值一致，进入安装模组");
							}
							// 发送进度条信息到界面
							Message messageok = new Message();
							messageok.what = MessageModel.DOWNLOAD_OK;
							messageok.obj = mFileName;
							Log.d(TAG, "能否成功" + messageok.obj.toString());
							handler.sendMessage(messageok);
							// Log.d(TAG, "下载完成=============");
						}
						else
						{
							Log.d(TAG, "Hash值不一致，不能进入安装模块");
						}
					}
					catch (Exception e)
					{

						e.printStackTrace();
					}
					// 测试hash值
					if (DEBUG)
					{
						Log.d(TAG, "hash:计算下载文件的hash值是：" + hashnumber);
					}
				}
			}
			catch (MalformedURLException e)
			{

				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			// 取消下载对话框显示
			// mDownloadDialog.dismiss();
		}
	}

	/**
	 * Function: getFreeSpaceInB
	 * 
	 * @author dehoo-ZhongHeliang 2013-2-28下午6:27:50
	 * @param 所求存储区的绝对路径名
	 *            获取存储空间的可以空间大小
	 */
	public long getFreeSpaceInB(String path)
	{
		long nSDFreeSize = 0;
		if (path != null)
		{
			StatFs statfs = new StatFs(path);

			long nBlocSize = statfs.getBlockSize();
			long nAvailaBlock = statfs.getAvailableBlocks();
			nSDFreeSize = nAvailaBlock * nBlocSize;
		}
		if (DEBUG)
		{
			Log.d(TAG, "获取空间大小:" + Long.toString(nSDFreeSize) + "KB");
		}
		return nSDFreeSize;
	}

	/**
	 * Function:
	 * 
	 * @author dehoo-ZhongHeliang 2013-2-28下午6:30:24 缓冲区数据
	 */
	public String toHexString(byte[] b)
	{
		StringBuilder sb = new StringBuilder(b.length * 2);

		for (int i = 0; i < b.length; i++)
		{
			sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
			sb.append(HEX_DIGITS[b[i] & 0x0f]);
		}
		return sb.toString();
	}
}
