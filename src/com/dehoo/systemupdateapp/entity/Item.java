package com.dehoo.systemupdateapp.entity;

public class Item {
	public String realname; // app名字
	public int version; // 版本号
	public String versionName;// 版本名称
	public String packageName;// 包名
	public String url; // 下载链接
	public String hashnumber; // app哈希值
	public int progress = -1; // 下载进度
	public boolean b = false; // 是否进入下载队列

	public boolean v = false;// 已经下载完成

	public Item(String name, int version, String url, String hashnumber,
			String versionName, String packageName) {
		this.realname = name;
		this.versionName = versionName;
		this.packageName = packageName;
		this.version = version;
		this.url = url;
		this.hashnumber = hashnumber;
		this.b = false;
		this.v = false;
		this.progress = -1;

	}

}
