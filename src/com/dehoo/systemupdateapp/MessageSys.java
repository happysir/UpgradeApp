/**
 * 
 */
package com.dehoo.systemupdateapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

/**
 *  系统提示信息模组，提示操作后发送广播
 * @author dehoo-ZhongHeliang 2013-3-1上午10:23:34
 * @version jdk 1.6; sdk 4.2.0
 */
public class MessageSys
{
	private boolean DEBUG = true;
	private final static String TAG = "MessageSys";
	private Context mContext = null;
	// 广播信息
	private final String INSTALLOK_MSG_BROADCAST = "installok_msg_broadcast";
	private final String INSTALLFAIL_MSG_BROADCAST = "installfail_msg_broadcast";
	private final String UPDATEMSG_BROADCAST = "updatamsg_broadcast";
	
	public MessageSys(Context context)
	{
		this.mContext = context;
	}
	
	/**
	 * Function: hasUpdate
	 * 有更新的系统提示
	 * @author dehoo-ZhongHeliang 2013-3-2上午10:11:21
	 */
	public void hasUpdate()
	{
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_has_update);
		builder.setMessage(R.string.new_update_down);
		// 更新
		builder.setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 显示下载对话框
				if (DEBUG)
					Log.d(TAG,"==========进入下载模组==========");
				// 发送下载广播
				Intent mIntent = new Intent(UPDATEMSG_BROADCAST);
				mIntent.putExtra("yaner", "发送广播");
				mContext.sendBroadcast(mIntent);
				if (DEBUG)
					Log.d(TAG,"==========发提示有更新广播进入下载==========");
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
		if (DEBUG)
			Log.d(TAG, "==========是否能够进入下载==========");
	}
		
	/**
	 * Function: installOK
	 * 安装成功的系统提示
	 * @author dehoo-ZhongHeliang 2013-3-2上午11:53:40
	 */
	public void installOK()
	{
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.update_ok);
		builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 显示下载对话框
				if (DEBUG)
					Log.d(TAG, "==========安装成功成功提示==========");
				// 发送下载广播
				Intent mIntent = new Intent(INSTALLOK_MSG_BROADCAST);
				mIntent.putExtra("yaner", "发送广播");
				mContext.sendBroadcast(mIntent);
				if (DEBUG)
					Log.d(TAG, "==========发安装成功广播进入下载==========");
			}
		});
		builder.setNegativeButton(R.string.btn_cancle, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 显示下载对话框
				if (DEBUG)
					Log.d(TAG, "==========取消更新提示==========");
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
		
	}
		
	/**
	 * Function: installOK
	 * 安装失败的系统提示
	 * @author dehoo-ZhongHeliang 2013-3-2上午11:53:40
	 */
	public void installFail()
	{
		// 构造对话框
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle(R.string.update_fail);
		builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				// 显示下载对话框
				if (DEBUG)
					Log.d(TAG, "==========安装成功失败提示==========");
				// 发送下载广播
				Intent mIntent = new Intent(INSTALLFAIL_MSG_BROADCAST);
				// mIntent.putExtra("yaner", "发送广播");
				mContext.sendBroadcast(mIntent);
				if (DEBUG)
					Log.d(TAG, "==========发安装失败广播进入下载==========");
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

}
