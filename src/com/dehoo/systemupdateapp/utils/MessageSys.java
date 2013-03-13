/**
 * 
 */
package com.dehoo.systemupdateapp.utils;

import com.dehoo.systemupdateapp.R;
import com.dehoo.systemupdateapp.config.MessageModel;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
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
	private Handler mHandler = null;
	
	public MessageSys(Context context,Handler handler)
	{
		this.mContext = context;
		this.mHandler = handler;
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
					Log.d(TAG,"==========发提示有更新广播进入下载模组==========");
				// 发送有更新的提示消息
				Message message = new Message();
				message.what = MessageModel.SYS_MSG_UPDATEMSG;
				mHandler.sendMessage(message);
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
					Log.d(TAG, "==========安装成功成功提示，发安装成功消息进入下载==========");
				// 发送安装成功的提示消息
				Message message = new Message();
				message.what = MessageModel.SYS_MSG_INSTALLOK;
				mHandler.sendMessage(message);
			}
		});
		
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
		
	}
		
	/**
	 * Function: installFail
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
					Log.d(TAG, "==========安装成功失败提示,发安装失败消息进入下载========");
				// 发送安装失败的提示消息
				Message message = new Message();
				message.what = MessageModel.SYS_MSG_INSTALLFAIL;
				mHandler.sendMessage(message);
			}
		});
		Dialog noticeDialog = builder.create();
		noticeDialog.show();
	}

}
