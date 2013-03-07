package com.dehoo.upgradedialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.UpgradeManager;
import android.util.Log;

/**
 * This is a broadcast booting from the start of system, to detect whether to
 * upgrade the system.
 * 
 * @author dehoo-HuangDong 2013-3-5上午10:39:40
 * @version jdk 1.6; sdk 4.1.2
 */
public class UpgradeBroadcast extends BroadcastReceiver
{
	private static final String TAG = "UpgradeBroadcast";
	UpgradeManager upgradeManager;
	
	/**a flag marking whether system service has detected system upgrade*/
	boolean systemFlag;
	boolean appFlag;

	int localVersionCode = 1;
	int serverVersionCode = 2;
	String localPackage = "com.dehoo.webtv";
	String serverPackage = "com.dehoo.webtv";

	/*
	 * (non-Javadoc)S
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "UpgradeBroadcast onReceive**********************");
		upgradeManager = (UpgradeManager) context
				.getSystemService(Context.UPGRADE_SERVICE);
		
		systemFlag = upgradeManager.checkUpgrade();
		createSystemDialog(context, intent);

		// appFlag = upgradeManager.checkAppVersion(localVersionCode,
		// serverVersionCode, localPackage, serverPackage);
		// createAppDialog(context);

	}

	/**
	 * 
	 * Function:createSystemDialog
	 * when detecting system upgrading, start TipDialog.java to pop up a dialog.
	 * 
	 * @author dehoo-HuangDong 2013-3-7下午1:47:47
	 * @param context
	 * @param intent
	 */
	private void createSystemDialog(Context context, Intent intent)
	{
		Log.d(TAG, "************flag = true**********************");
		if (systemFlag)
		{
			intent = new Intent(context, TipDialog.class);
			intent.putExtra("systemFlag", systemFlag);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	/**
	 * 
	 * Function:createAppDialog
	 *
	 * @author dehoo-HuangDong 2013-3-7下午1:49:16
	 * @param context
	 */
	private void createAppDialog(Context context)
	{
		if (appFlag)
		{
			Log.d(TAG, "..............................createAppDialog");
		}
	}
}
