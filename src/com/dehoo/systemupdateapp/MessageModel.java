package com.dehoo.systemupdateapp;

public class MessageModel {
	
	//1**handler 消息处理
	/** 获取网络最新版本 **/
	public static final int GET_UPDATE_RESULT = 101;
	/** 网络升级按钮可用 **/
	public static final int NETWORK_UPDATE_BUTTON_USEFUL = 102;
	/** 网络升级按钮不可用 **/
	public static final int NETWORK_UPDATE_BUTTON_NOT_USEFUL = 103;
	/** 根据指定url下载可更新文件 **/
	public static final int NETWORK_URL_DOWNLOAD = 104;
	/** 检查网络下载文件类型 **/
	public static final int CHECK_NETWORK_DOWNLOAD_FILE_TYPE = 105;
	/** 本地固件升级 **/
	public static final int LOCAL_FILE_UPDATE = 106;
	

	//2**系统网络信息
	/**
	 * 没有连接网络
	 */
	public final static String NETWORK_No_AVAILABLE = "201";
	/**
	 * 不能使用wifi
	 */
	public final static String NOT_USE_WIFI = "202";
	/**
	 * 网络请求异常
	 */
	public final static String RESPONSE_EXCEPTION = "204";
	/**
	 * 网络连接超时
	 */
	public final static String CONNECTION_TIMEOUT = "205";
	
	/*
	 *  解析xml文件线程发出的消息的名称
	 */
	public static final int PARSE_XML_OK = 301;
	/*
	 *  发送下载进度信息
	 */
	public static final int PROGRESS_NUMBER = 302;
	/*
	 * 发送下载完成的信息
	 */
	public static final int DOWNLOAD_OK = 303;
	
}
