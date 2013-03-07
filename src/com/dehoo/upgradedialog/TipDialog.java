package com.dehoo.upgradedialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import com.dehoo.systemupdateapp.NetworkList;
import com.dehoo.systemupdateapp.R;

/**
 * A dialog pops up when detecting system upgrading.This activity is started by
 * UpgradeBroadcast.java and starts NetworkList.java when flag = true.
 * 
 * @author dehoo-HuangDong 2013-3-7下午1:36:18
 * @version jdk 1.6; sdk 4.1.2
 */
public class TipDialog extends Activity
{
	/**systemFlag from UpgradeBroadcast.java*/
	private boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog);

		Intent intent = this.getIntent();
		flag = intent.getBooleanExtra("systemFlag", false);
		if (flag)
		{
			createDialog();
		}
	}

	public void createDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("Note");
		dialog.setMessage("Check to the new version, whether Update?");
		dialog.setPositiveButton("Ok", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Intent intent = new Intent(TipDialog.this, NetworkList.class);
				TipDialog.this.startActivity(intent);
				TipDialog.this.finish();
			}
		});
		dialog.setNegativeButton("Cancel", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				TipDialog.this.finish();
			}
		});
		dialog.show();
	}

}
