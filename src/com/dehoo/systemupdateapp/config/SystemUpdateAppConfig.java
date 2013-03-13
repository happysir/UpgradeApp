package com.dehoo.systemupdateapp.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetManager;
import android.util.Log;

/**
 * systemUpdateAppConfig.txt文件解析类
 * @author zhanmin
 */
public class SystemUpdateAppConfig {
	
	private static final String TAG = "SystemUpdateAppConfig";

	private static SystemUpdateAppConfig mSystemUpdateAppConfig;
	private Map<String,String> configInfo = new HashMap<String, String>();
	
	private SystemUpdateAppConfig(AssetManager assetManager){
		configInfo = load(assetManager,"systemUpdateAppConfig.txt");
	}
	
	private SystemUpdateAppConfig(AssetManager assetManager, String configFileName){
		configInfo = load(assetManager,configFileName);
	}
	
	public static SystemUpdateAppConfig getInstance(AssetManager assetManager){
		if(mSystemUpdateAppConfig==null){
			synchronized (SystemUpdateAppConfig.class) {
				if(mSystemUpdateAppConfig==null){
					mSystemUpdateAppConfig = new SystemUpdateAppConfig(assetManager);
				}
			}
		}
		return mSystemUpdateAppConfig;
	}
	
	/**
	 * 加载配置文件
	 * @param assetManager
	 * @param configFileName
	 * @return
	 */
	private Map<String,String> load(AssetManager assetManager, String configFileName){
		InputStream is;
		StringBuffer result= new StringBuffer();
		try {
			is = assetManager.open(configFileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line = "";
			while((line = br.readLine())!=null){
				result.append(line+"\n");
			}
		} catch (IOException e) {
			Log.e(TAG, "Config File Load Error:"+e.getMessage());
			e.printStackTrace();
		}
		return resolveConfig(result.toString());
	}
	
	/**
	 * 解析配置文件
	 * @param fileContent
	 * @return
	 */
	private Map<String,String> resolveConfig(String fileContent){
		Map<String,String> configInfo = new HashMap<String, String>();
		if(fileContent!=null){
			Log.v(TAG, "fileContent:"+fileContent);
			String [] contentLines = fileContent.split("\n");
			for (String line : contentLines) {
				line = line.replace(" ", "");
				line = line.replace("\t", "");
				if(line.indexOf("#")==0){
					continue;
				}
				int keyValueIndex = line.indexOf("=");
				String key = null;
				String value=null;
				if(keyValueIndex!=-1){
					key = line.substring(0,keyValueIndex);
					value = line.substring(keyValueIndex+1,line.length());
				}
				configInfo.put(key, value);
			}
			return configInfo;
		}else{
			return null;
		}
	}
	
	/**
	 * 是否打开DEBUG开关
	 * @return
	 */
	public boolean getUploadURL(){
		if(this.configInfo.get("DEBUG")==null||configInfo.get("DEBUG").equals("")){
			return false;
		}else{
			return Boolean.parseBoolean(this.configInfo.get("DEBUG"));
//			return Boolean.valueOf(this.configInfo.get("DEBUG")).booleanValue();
		}
	}
	
}
