package com.dehoo.systemupdateapp.config;

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
	/**当外接设备状态改变时重新加载数据***/
	public static final int RELOAD_DATA_FROM_EXTERNAL = 107;
	/** 获取接入设备的本地存储里固件包 **/
	public static final int GET_LOCAL_STORAGE_FIRMWARE = 108;
	

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
	
	/** 解析xml文件线程发出的消息的名称 */
	public static final int PARSE_XML_OK = 301;
	/** 发送下载进度信息 */
	public static final int PROGRESS_NUMBER = 302;
	/** 发送下载完成的信息 */
	public static final int DOWNLOAD_OK = 303;
	/** 系统提示信息类型之有更新的软件或固件 */
	public static final int SYS_MSG_UPDATEMSG = 304;
	/** 系统提示信息类型之安装成功消息 */
	public static final int SYS_MSG_INSTALLOK = 305;
	/** 系统提示信息类型之安装失败消息 */
	public static final int SYS_MSG_INSTALLFAIL = 306;
	
	//4**代表应用程序信息
	/** 配置文件key不存在 */
	public final static String CONFIG_ERROR = "101";
	/** 创建文件失败 */
	public final static String CREATE_FILE_FAIL = "102";
	/** 异常 */
	public final static String APP_EXCEPTION = "103";
	/** SD卡不存在 */
	public final static String SDCARD_NOT_EXISTS = "104";
}
