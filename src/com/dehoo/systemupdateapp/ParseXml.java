package com.dehoo.systemupdateapp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.util.Log;

/**
 * class:解析xml文件的内部类
 * 
 * @author dehoo-ZhongHeliang 2013-2-28上午9:58:56
 */
class ParseXml {

	private final static boolean DEBUG = true;
	private final static String TAG = "ParseXml";
	
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
			Element versionEle = (Element)fileEle.getElementsByTagName("version").item(0);
			Element urlEle = (Element)fileEle.getElementsByTagName("url").item(0);
			Element hashnumberEle = (Element)fileEle.getElementsByTagName("hashnumber").item(0);
			
			String aname = nameEle.getFirstChild().getNodeValue();
			Log.d(TAG, "aname is :"+aname);
			map.put("realname",aname);
			
			String aversoin = versionEle.getFirstChild().getNodeValue();
			Log.d(TAG, "aversoin is :"+aversoin);
			map.put("version",aversoin);
			
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
	
}