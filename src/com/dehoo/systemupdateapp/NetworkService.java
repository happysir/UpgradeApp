package com.dehoo.systemupdateapp;

import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class NetworkService {

	private final static String TAG = "NetworkService";

	/**
	 * HTTP网页内容抓取
	 * 
	 * @param url  String 网页地址
	 * @return
	 */
	public static String sendGet(Context mContext,String url) {
		String result = sendGet(mContext,url, 6, null, null,false);
		int  times =0;
		while(Util.isResultException(result) && times<2){
			result = sendGet(mContext,url, 6, null, null,false);
			++times;
		}
		return result;
	}
	
	public static String sendGet(Context mContext,String url,CookieStore cookieStore,boolean isSaveCookie){
		return sendGet(mContext,url, 10,null, cookieStore,isSaveCookie);
	}

	/**
	 * http网页内容抓取
	 * 
	 * @param url String 网页地址
	 * @param timeoutSeconds int 超时时间（单位秒）
	 * @param params Map<String, String> HTTPHead参数
	 * @param cookieStore CookieStore 网页cookie
	 * @return
	 */
	public static String sendGet(Context mContext,String url, int timeoutSeconds,
			Map<String, String> params, CookieStore cookieStore,boolean isSaveCookie) {
		String result = null;
		Log.v(TAG, "httpGet start to get url:" + url);
		HttpParams httpParams = new BasicHttpParams();
		HttpContext context = new BasicHttpContext();
		
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		if(!wifiManager.isWifiEnabled()){
			Uri uri = Uri.parse("content://telephony/carriers/preferapn"); //获取当前正在使用的APN接入点
			Cursor mCursor = mContext.getContentResolver().query(uri, null, null, null, null);
			if(mCursor != null&&mCursor.getCount()>0){
				mCursor.moveToNext();
				String proxyStr = mCursor.getString(mCursor.getColumnIndex("proxy"));
				if(proxyStr != null && proxyStr.trim().length() > 0){
					Log.v(TAG, "wap proxy:"+proxyStr);
					HttpHost proxy = new HttpHost(proxyStr, 80);
					httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
				}
			}
		}
		
		HttpConnectionParams.setConnectionTimeout(httpParams,timeoutSeconds * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, timeoutSeconds * 1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		
		HttpClientParams.setCookiePolicy(httpClient.getParams(),CookiePolicy.BROWSER_COMPATIBILITY);
		
		if (cookieStore != null) {
			
			httpClient.setCookieStore(cookieStore);
			context.setAttribute(ClientContext.COOKIE_STORE,cookieStore);
		}
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent","Mozilla/5.0 (Linux; U; Android "+Build.VERSION.RELEASE+"; Zh-cn; "+Build.MODEL+" )AppleWebKit/528.5+(KHTML,like Gecko)Version/3.1.2 Mobile Safari/525.20.1");
		if (params != null) {
			Iterator<String> ite = params.keySet().iterator();
			while (ite.hasNext()) {
				String key = (String) ite.next();
				String value = (String) params.get(key);
				httpGet.addHeader(key, value);
			}
		}
		try {
			HttpResponse response = httpClient.execute(httpGet, context);
			if(isSaveCookie){
				CookieStore cookieStores = httpClient.getCookieStore();
				if(cookieStores!=null){
					List<Cookie> listCookie = cookieStores.getCookies();
					int len = listCookie.size();
					for(int i = 0; i<len;i++){
						Cookie cookie = listCookie.get(i);
						StringBuffer sb = new StringBuffer();
						sb.append(cookie.getName()+"="+cookie.getValue()+";\n");
						sb.append("domain="+cookie.getDomain()+";\n");
						sb.append("path="+cookie.getPath()+";\n");
						sb.append("expiry="+cookie.getExpiryDate()+";\n");
						Log.i(TAG, sb.toString());
					}
				}
//				Config.getInstance().setCookieStroe(httpClient.getCookieStore());
			}
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				result =EntityUtils.toString(response.getEntity());
			} else {
				Log.v(TAG, "result:"+EntityUtils.toString(response.getEntity()));
				result = MessageModel.RESPONSE_EXCEPTION;
				Log.e(TAG, "Network Exception, statusCode:" + statusCode);
			}
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				result = MessageModel.CONNECTION_TIMEOUT;
				Log.e(TAG, "CONNECTION_TIMEOUT---- Network Exception:" + e.getMessage()+" url:"+url);
				e.printStackTrace();
			} else {
				result = MessageModel.RESPONSE_EXCEPTION;
				Log.e(TAG, "RESPONSE_EXCEPTION ---- Network Exception:" + e.getMessage()+" url:"+url);
				e.printStackTrace();
			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		Log.v(TAG, "httpGet get result:" + result);
		return result;
	}

}
