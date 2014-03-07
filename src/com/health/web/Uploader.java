package com.health.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.health.database.Cache;
import com.health.database.DatabaseService;

public class Uploader implements Runnable {
	public static final int MESSAGE_UPLOADE_RESULT = 0x5300;
	public static final String STUTAS = "data";
	public static final int OK = 0;
	public static final int FAILURE = 1;// 失败
	public static final int NET_ERROR = 2;// 网络异常
	private Map<String, String> dataMap;
	private String path;
	private String item;
	private Cache cache;
	private DatabaseService dbService;
	private Handler handler;
	private Map<String, String> table;

	public Uploader(Map<String, String> dataMap, String item, String path,
			Cache cache, DatabaseService dbService, Handler handler,
			Map<String, String> table) {
		this.dataMap = dataMap;
		this.item = item;
		this.path = path;
		this.cache = cache;
		this.dbService = dbService;
		this.handler = handler;
		this.table = table;
	}

	@Override
	public void run() {
		Message msg = handler.obtainMessage(MESSAGE_UPLOADE_RESULT);
		Bundle bundle = new Bundle();
		bundle.putString(Cache.ITEM, item);
		cache.saveItem(item, dataMap);// 保存单项测量值
		dataMap.remove(WebService.STATUS);// 删除上传状态
		dataMap.put(WebService.PATH, path);		
		JSONObject json = new JSONObject(dataMap);
		JSONObject resultJson;
		int status;
		try {
			resultJson = WebService.upload(json);
			int statusCode = resultJson.getInt(WebService.STATUS_CODE);
			if (statusCode != WebService.OK) {// 上传不成功的数据，保存在数据库
				dbService.insert(table, dataMap);
				status = FAILURE;
			} else {
				dataMap.put(WebService.STATUS, WebService.UPLOADED);// 上传成功,更新标志
				cache.saveItem(item, dataMap);
				status = OK;
			}
		} catch (Exception e) {
			e.printStackTrace();
			status = NET_ERROR;
		}
		bundle.putInt(STUTAS, status);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

}
