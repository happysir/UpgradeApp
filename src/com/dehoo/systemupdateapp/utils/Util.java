package com.dehoo.systemupdateapp.utils;

import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.http.util.EncodingUtils;
import com.dehoo.systemupdateapp.config.MessageModel;
import com.dehoo.systemupdateapp.entity.AppInfo;
import com.dehoo.systemupdateapp.entity.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.util.Log;

/**
 * 工具类
 * @author work
 *
 */
public class Util {

	private static final String TAG = "cyTest";
	
	public static final String ROOT_PATH = "/mnt";

	private ConnectivityManager connectivity;
	private PackageInfo pInfo;
	private List<Map<String, String>> mTargetList = new ArrayList<Map<String, String>>(); // 存储固件刷机包文件
	private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * 检查是否有sdcard
	 * 
	 * @return
	 */
	public boolean checkSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}

	public boolean checkUSB() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_SHARED))
			return true;
		else
			return false;
	}

	/**
	 * 获取 ConnectivityManager
	 * 
	 * @param context
	 * @return
	 */
	public ConnectivityManager getConnectivityManager(Context context) {
		connectivity = (ConnectivityManager) context.getSystemService("connectivity");
		// connectivity = (ConnectivityManager)scontext.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivity;
	}

	/**
	 * 判断是否有可用网络
	 * 
	 * @param context
	 * @return
	 */
	public boolean isNetworkAvailable(Context context) {
		connectivity = getConnectivityManager(context);
		if (connectivity == null) {
			return false;
		}
		NetworkInfo[] info = connectivity.getAllNetworkInfo();
		if (info != null) {
			for (int i = 0; i < info.length; ++i) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//					Log.d(TAG, "it has network connetion !!");
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Function: toHexString
	 * 缓冲区数据
	 * @author dehoo-ZhongHeliang 2013-3-7上午11:08:04
	 * @param b 缓冲区数据
	 * @return 通过缓冲区数据得到的hash值
	 */
	public String toHexString(byte[] b) {  
		 StringBuilder sb = new StringBuilder(b.length * 2); 
		 
		 for(int i = 0; i < b.length; i++) {  
		     sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);  
		     sb.append(HEX_DIGITS[b[i] & 0x0f]);  
		 }  
		 return sb.toString();  
	}
	
	/**
	 * Function: getFreeSpaceInB
	 * @author dehoo-ZhongHeliang 2013-2-28下午6:27:50
	 * @param 所求存储区的绝对路径名
	 * 获取存储空间的可以空间大小
	 */
	public long getFreeSpaceInB(String path) {
    	long nSDFreeSize = 0;
    	if (path != null) {
        	StatFs statfs = new StatFs(path);
    		long nBlocSize = statfs.getBlockSize();
    		long nAvailaBlock = statfs.getAvailableBlocks();
    		nSDFreeSize = nAvailaBlock * nBlocSize;
    	}
    	Log.d(TAG,"获取空间大小:"+Long.toString(nSDFreeSize)+"KB");
    	
		return nSDFreeSize;
    }


	/**
	 * 检查一个字符串是否为异常消息
	 * 
	 * @param result
	 * @return 是异常消息返回True，否则为false。
	 */
	public static boolean isResultException(String result) {
		if (result == null)
			return true;
		Pattern pattern = Pattern.compile("^[0-9]{3}$");
		Matcher matcher = pattern.matcher(result);
		return matcher.find();
	}

	/**
	 * 获取PackageInfo(包信息)
	 * 
	 * @param context
	 * @return PackageInfo
	 */
	public PackageInfo getVersionCode(Context context) {
		try {
			Log.v(TAG, "context.getPackageName = " + context.getPackageName());
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return pInfo;
	}

	/**
	 * 获取android SDK版本
	 * 
	 * @return
	 */
	public int getSDKVersionCode() {
		int sdkLevel = 3;
		try {
			sdkLevel = Integer.valueOf(Build.VERSION.SDK_INT).intValue();
		} catch (NumberFormatException e) {
			sdkLevel = 3;
		}

		Log.d(TAG, "sdk Level is " + sdkLevel);

		return sdkLevel;
	}

	/**
	 * 查看App是否有更新
	 * 
	 * @param url
	 * @param context
	 * @return
	 */
	public boolean isNeedUpdate(Context context, Item item) {
		String appPackageName = null;
//		String appVersionName = null;
		int appVersionCode = 0;
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (int j = 0; j < packages.size(); j++) {
			PackageInfo packageInfo = packages.get(j);
			appPackageName = packageInfo.packageName;
	//		appVersionName = packageInfo.versionName;
			appVersionCode = packageInfo.versionCode;
 
			if (appPackageName.equals(item.packageName)) {
				Log.d("dehoo", item.realname + ":code" + appVersionCode);
				if (appVersionCode < item.version) {
					return true;
				} else
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Function: firmwareCheck  
	 * @author dehoo-jiangmq 2013-3-8下午6:19:55
	 *
	 * 检测系统固件升级
	 */
	public boolean firmwareCheck(List<Item> firmwareList) {
		String sysVersionName = android.os.Build.VERSION.RELEASE;
		String sysVersion = android.os.Build.VERSION.CODENAME;
		int sysVersionCode = android.os.Build.VERSION_CODES.BASE;
		Log.d(TAG, "sysVersionName:" + sysVersionName + "---sysVersion:" + sysVersion + "----sysVersionCode" + sysVersionCode);
		for (int j = 0; j < firmwareList.size(); j++) {
			if(firmwareList.get(j).versionName==null){
				 continue ;
			}
			if (firmwareList.get(j).version > sysVersionCode) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 得到应用签名信息
	 * @throws Exception
	 */
	public void getSignatures(Context context) throws Exception{
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = pm.getPackageInfo("com.dehoo.systemupdateapp", PackageManager.GET_SIGNATURES);
		Signature[] signs = pi.signatures;
		String yourSign=signs[0].toCharsString();
	}

	/**
	 * 查找文件
	 *  modify by zhanmin 13.03.07
	 * @param filepath
	 * @param targetWord
	 */
	public void searchFile(Context context, File filepath, String targetWord) {
		File[] files = filepath.listFiles();
		if (files.length > 0) {
			for (File file : files) {
				if (file.isDirectory()) {
					if (file.canRead()) { // 判断目录是否可读
						searchFile(context, file, targetWord); // 如果是目录，递归查找
					}
				} else {
					if (getFileExt(file.getName()) != null && getFileExt(file.getName()).equals(targetWord)) {
						getTargetFileList(context, file);
					}
				}
			}
		}
	}

	/**
	 * 通过文件名获取文件后缀名
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileExt(String fileName) {
		int numOfPoint = fileName.lastIndexOf(".");
		if (numOfPoint == -1) {
			return null;
		}
		String fileExt = fileName.substring(numOfPoint + 1, fileName.length());
		return fileExt;
	}

	/**
	 * 获取目标文件列表
	 * modify by zhanmin 13.03.06 添加数字证书验证
	 * 
	 * @param file
	 */
	public void getTargetFileList(Context context, File file) {

//		String publicKey = getDigitalSigned(file.getPath());
//		String assetPublicKey = getFromAssets(context, "publickey.txt");
//		if (publicKey != null) {
//			publicKey = formatString(publicKey);
//			if (assetPublicKey.equals(publicKey)) {
				Map<String, String> targetMap = new HashMap<String, String>();
				targetMap.put("filename", file.getName());
				targetMap.put("filepath", file.getPath());

				mTargetList.add(targetMap);
//			}
//		}
	}

	/**
	 * 获取以zip结尾的所有文件
	 * modify by zhanmin 13.03.07
	 * @param filepath
	 * @param targetWord
	 * @return
	 */
	public List<Map<String, String>> getFileListDataSortedAsync(Context context, String filepath, String targetWord) {
		// List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		File file = new File(filepath);
		searchFile(context, file, targetWord);

		return mTargetList;
	}

	/**
	 * 从assets 文件夹中获取文件并读取数据
	 * 
	 * @author dehoo-zhanmin 13.03.06
	 * @param context
	 * @param fileName
	 * @return
	 */
	public String getFromAssets(Context context, String fileName) {
		String result = "";
		try {
			InputStream in = context.getResources().getAssets().open(fileName);
			// 获取文件的字节数
			int lenght = in.available();
			// 创建byte数组
			byte[] buffer = new byte[lenght];
			// 将文件中的数据读到byte数组中
			in.read(buffer);
			result = EncodingUtils.getString(buffer, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 格式化字符串
	 * 
	 * @author dehoo-zhanmin 13.03.06
	 * @param str
	 * @return
	 */
	public String formatString(String str) {
		str = str.trim();
		Pattern p = Pattern.compile("\\s*|\t|\r|\n");
		Matcher m = p.matcher(str);
		str = m.replaceAll("");
		return str;
	}

	/**
	 * 存储获取的应用信息数据　　
	 * add by zhanmin 13.03.08
	 * @param context
	 * @return
	 */
	public List<AppInfo> getAllAppsInfo(Context context) {
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			AppInfo tmpInfo = new AppInfo();
			tmpInfo.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
			tmpInfo.packageName = packageInfo.packageName;
			tmpInfo.versionName = packageInfo.versionName;
			tmpInfo.versionCode = packageInfo.versionCode;
			
			Log.d(TAG, "packageInfo.packageName = " + packageInfo.packageName);
//			Log.d(TAG, "packageInfo.versionName = " + packageInfo.versionName);
//			Log.d(TAG, "packageInfo.versionCode = " + packageInfo.versionCode);
			appList.add(tmpInfo);
		}
		return appList;
	}

	/**
	 * Function: ExcuteUpgradeAction
	 * 
	 * @author dehoo-Houliqiang 2013-2-26 15:00
	 * @param�� context, PackageFile, ApkFilePath
	 * @throws Exception
	 *             ------------------------------------------------------- To do
	 *             Upgrade firmmware and install APK.
	 */

	public void ExcuteUpgradeAction(final Context context, String FilePath) {
		Log.d(TAG, "Start ExcuteUpgradeAction Path : " + FilePath);

		/******************************************/
		/** to confirm is the UpgradeFile exist ****/
		Log.d(TAG, "FilePath:" + FilePath);
		File UpgradeFile = new File(FilePath);
		if (!UpgradeFile.exists()) {
			return;
		}
		/*************** no exist, then return *****/
		/****************************************/

		if (FilePath.endsWith("zip")) {
			String FileName = new String(UpgradeFile.getPath());
			if (!FileName.startsWith("/mnt/sdcard") && !FileName.startsWith("/storage/sdcard")) {
				FileName = "/udisk/" + UpgradeFile.getName();
				File UpgradePackage = new File(FileName);
				Log.d(TAG, "The File is Upgrade Package , Start Reboot and Install package");
				Log.d(TAG, "CURRENT PATH :" + FileName);
				install_Package(context, UpgradePackage);
			} else {
				Log.d(TAG, "The File is Upgrade Package , Start Reboot and Install package");
				Log.d(TAG, "CURRENT PATH :" + UpgradeFile.getPath());
				install_Package(context, UpgradeFile);

			}
		}
		if (FilePath.endsWith("apk")) {
			Log.d(TAG, "The File is Apk package, Start Install APK:" + FilePath);
			install_apk(context, FilePath);
		}

	}

	/**
	 * Function: install_Package
	 * 
	 * @author dehoo-Houliqiang 2013-2-26 15:00
	 * @param�� context, PackageFilePath
	 * @throws Exception
	 *             ------------------------------------------------------- To do
	 *             Upgrade android system.
	 */
	public void install_Package(final Context context, final File packageFile) {

		Thread thr = new Thread("Reboot") {

			@Override
			public void run() {
				try {
					RecoverySystem.installPackage(context, packageFile);
				} catch (IOException e) {
					Log.e(TAG, "Can't perform rebootInstallPackage", e);
				}
			}
		};
		thr.start();

	}

	/**
	 * Function: install_apk
	 * 
	 * @author dehoo-Houliqiang 2013-2-26 15:00
	 * @param�� apk_filepath
	 * @throws Exception
	 *             ------------------------------------------------------- To
	 *             Install APK
	 */
	public void install_apk(Context context, String apk_filepath) {
		Log.d(TAG, "file Path" + apk_filepath);
		Intent installintent = new Intent();
		installintent.setComponent(new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity"));
		installintent.setAction(Intent.ACTION_VIEW);
		installintent.setData(Uri.fromFile(new File(apk_filepath)));
		context.startActivity(installintent);
	}

	// add by dehoo-zhanmin 13.03.06
	private static final Object mSync = new Object();
	private static WeakReference<byte[]> mReadBuffer;

	// add by dehoo-zhanmin 13.03.06 end

	/**
	 * 加载证书
	 * 
	 * @author dehoo-zhanmin 13.03.06
	 * @param jarFile
	 * @param je
	 * @param readBuffer
	 * @return
	 */
	private Certificate[] loadCertificates(JarFile jarFile, JarEntry je, byte[] readBuffer) {
		try {
			// We must read the stream for the JarEntry to retrieve
			// its certificates.
			InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
			while (is.read(readBuffer, 0, readBuffer.length) != -1) {
				// not using
			}
			is.close();
			return je != null ? je.getCertificates() : null;
		} catch (IOException e) {
			Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
		} catch (RuntimeException e) {
			Log.w(TAG, "Exception reading " + je.getName() + " in " + jarFile.getName(), e);
		}
		return null;
	}

	/**
	 * 获取数字签名
	 * 
	 * @author dehoo-zhanmin 13.03.06
	 * @param url
	 */
	public String getDigitalSigned(String url) {

		WeakReference<byte[]> readBufferRef;
		byte[] readBuffer = null;
		synchronized (mSync) {
			readBufferRef = mReadBuffer;
			if (readBufferRef != null) {
				mReadBuffer = null;
				readBuffer = readBufferRef.get();
			}
			if (readBuffer == null) {
				readBuffer = new byte[8192];
				readBufferRef = new WeakReference<byte[]>(readBuffer);
			}
		}
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Certificate[] certs = null;
		Enumeration<JarEntry> entries = jarFile.entries();
		try {
			final Manifest manifest = jarFile.getManifest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (entries.hasMoreElements()) {
			final JarEntry je = entries.nextElement();
			if (je.isDirectory())
				continue;

			final String name = je.getName();
			if (name.startsWith("META-INF/")) {
				continue;
			}

			final Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);
			if (localCerts == null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (certs == null) {
				certs = localCerts;
			} else {
				// Ensure all certificates match.
				for (int i = 0; i < certs.length; i++) {
					boolean found = false;
					for (int j = 0; j < localCerts.length; j++) {
						if (certs[i] != null && certs[i].equals(localCerts[j])) {
							found = true;
							break;
						}
					}
					if (!found || certs.length != localCerts.length) {
						try {
							jarFile.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		try {
			jarFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized (mSync) {
			mReadBuffer = readBufferRef;
		}
		if (certs != null && certs.length > 0) {
			final int N = certs.length;
			for (int i = 0; i < N; i++) {
				Log.i(TAG, "  Public key: " + certs[i].getPublicKey() + " ------------ ");
				return certs[i].getPublicKey().toString();
			}
		}
		return null;
	}

}
