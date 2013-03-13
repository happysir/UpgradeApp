package com.dehoo.systemupdateapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dehoo.systemupdateapp.config.MessageModel;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/**
 * 网络数据获取、解析
 * @author zhanmin
 */
public class NetworkService {

	private final static String TAG = "NetworkService";
	
	private InputStream mInStream;
	private final static boolean DEBUG = true;
	
	/**
	 * Function: getDownList 从网络上解析一个xml文件，得到它里面的data返回一个list
	 * 
	 * @author dehoo-ZhongHeliang 2013-3-5下午6:19:55
	 * @return list
	 */
	public List<HashMap<String, String>> getDownList() {
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		// 解析网络上的xml文件
		URL url = null;
		try {
			url = new URL("http://192.168.1.110/version.xml");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		try {
			mInStream = url.openStream();
		} catch (IOException e) {

			e.printStackTrace();
		}
		try {
			list = parseXml(mInStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// modify by zhanmin13.03.08
		// 验证在更新列表中的app是否可用
		//List<AppInfo> appInfoList = mUtil.getAllAppsInfo(this);
		//for(int i=0; i<appInfoList.size(); i++){
		//	for(int j=0; j<list.size(); j++){
		//		if(list.get(j).get("packagename").equals(appInfoList.get(i).packageName)){
		//			// 如果版本小于本地版本，不显示
		//			if(Integer.parseInt(list.get(j).get("version")) <= appInfoList.get(i).versionCode){
		//				list.remove(j); 
		//			}
		//			// 如果publicKey不相同，不显示
		//		}
		//	}
		//}
		// modify by zhanmin13.03.08
		return list;
	}
	
	/**
	 * Function:parseXml
	 * 解析一个xml，得到里面的数据 ，把数据存到一个list<Map<String,String>>中
	 * @author dehoo-ZhongHeliang 2013-3-4下午1:53:06
	 * @param inStream：由一个xml文件生成的InputStream
	 * @return 存储数据的list
	 * @throws Exception
	 */
	public List<HashMap<String, String>> parseXml(InputStream inStream) throws Exception
	{
		List<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
		// 实例化一个文档构建器工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// 通过文档构建器工厂获取一个文档构建器
		DocumentBuilder builder = factory.newDocumentBuilder();
		// 通过文档通过文档构建器构建一个文档实例
		Document document = builder.parse(inStream);
		// 获取XML文件根节点
		Element rootEle = document.getDocumentElement();
		// 二级父元素的list列表 fileNode
		NodeList fileNode = rootEle.getElementsByTagName("file");
		// 遍历update下所有的file
		if (DEBUG)
		{
			Log.d(TAG, "遍历update下所有的file");
		}
		for (int i = 0; i < fileNode.getLength(); i++)
		{
			HashMap<String, String> map = new HashMap<String, String>();
			
			Element fileEle = (Element)fileNode.item(i);
			//String fileName = fileEle.getAttribute("name");\
			if (DEBUG)
			{
				Log.d(TAG, "fileName = :"+fileEle.getAttribute("name"));
			}	
			// 获取所有子节点
			Element nameEle = (Element)fileEle.getElementsByTagName("realname").item(0);
			Element versionnameEle = (Element)fileEle.getElementsByTagName("versionname").item(0);
			Element versionEle = (Element)fileEle.getElementsByTagName("version").item(0);
			Element packagenameEle = (Element)fileEle.getElementsByTagName("packagename").item(0);
			Element urlEle = (Element)fileEle.getElementsByTagName("url").item(0);
			Element hashnumberEle = (Element)fileEle.getElementsByTagName("hashnumber").item(0);
			
			String aname = nameEle.getFirstChild().getNodeValue();
			Log.d(TAG, "aname is :"+aname);
			map.put("realname",aname);
			
			String aversoinname = versionnameEle.getFirstChild().getNodeValue();
			Log.d(TAG, "aversoinname is :"+aversoinname);
			map.put("versionname",aversoinname);
			
			String aversoin = versionEle.getFirstChild().getNodeValue();
			Log.d(TAG, "aversoin is :"+aversoin);
			map.put("version",aversoin);
			
			String apackagename = packagenameEle.getFirstChild().getNodeValue();
			Log.d(TAG, "apackagename is :"+apackagename);
			map.put("packagename",apackagename);
			
			String aurl = urlEle.getFirstChild().getNodeValue();
			Log.d(TAG, "aurl is :"+aurl);
			map.put("url",aurl);
			
			String ahashnumber = hashnumberEle.getFirstChild().getNodeValue();
			Log.d(TAG, "ahashnumber is :"+ahashnumber);
			map.put("hashnumber",ahashnumber);
			
			list.add(map);		
		}
				
		return list;
	}
	

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
