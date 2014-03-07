package com.health.web;

import org.json.JSONObject;

import android.content.Context;

import com.health.database.Cache;

/***
 * 二次上传工具
 * @author jiqunpeng
 *
 * 创建时间：2013-12-19 下午7:13:25
 */
public class ReUploader implements Runnable{
	private Context context;
	private JSONObject json;
	public ReUploader(Context context,JSONObject json){
		this.context = context;
		this.json = json;
	}

	@Override
	public void run() {
		JSONObject resultJson;		
		try {
			resultJson = WebService.upload(json);
			int statusCode = resultJson.getInt(WebService.STATUS_CODE);
			if (statusCode == WebService.OK) {
				json.put(WebService.STATUS, WebService.UPLOADED);// 上传成功,更新标志
				Cache cache = new Cache(context);
				cache.saveItem(json.getString(Cache.ITEM), json);
			} 
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
