package com.health.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WebService {

	// private static final String DOMAIN =
	// "http://pat.uuhealth.com.cn/pat/index/";
	private static final String DOMAIN = "http://www.itonehealth.com/pat/index/";

	public static final String STATUS = "status";// 上传状态
	public static final String UPLOADED = "已上传";// 已经上传
	public static final String UNUPLOAD = "未上传";// 未上传

	public static final String PATH = "path";
	public static final String PATH_BP = "8001";
	public static final String PATH_BO = "8002";
	public static final String PATH_TEMP = "8003";
	public static final String PATH_GLU = "8004";
	public static final String PATH_CHOL = "8005";
	public static final String PATH_UA = "8006";
	public static final String PATH_URINE = "8007";

	public static final String PATH_QUERY_BP = "8008";
	public static final String PATH_QUERY_PULSE = "8009";
	public static final String PATH_QUERY_BO = "8010";
	public static final String PATH_QUERY_TEMP = "8011";
	public static final String PATH_QUERY_GLU = "8012";
	public static final String PATH_QUERY_CHOL = "8013";
	public static final String PATH_QUERY_UA = "8014";
	public static final String PATH_QUERY_URINE = "8015";

	public static final String STATUS_CODE = "statusCode";
	public static final String STATUS_MSG = "statusMsg";
	public static final int OK = 0;// 成功
	public static final int ERROE = 1;

	public static final String PLAT_ID_KEY = "orgid";
	public static final String PLAT_ID_VALUE = "21";
	public static final String STARTTIME = "startTime";
	public static final String ENDTIME = "endTime";
	public static final String PAGECOUNT = "pageCount";
	public static final String PAGEINDEX = "pageIndex";
	public static final String CHECKTIME = "checkTime";
	public static final String DATA = "data";
	private static final String TAG = "WebService";

	private static JSONObject upload(JSONObject json, String url)
			throws JSONException, IOException {
		StringBuilder builder = new StringBuilder(url);
		builder.append("?");
		builder.append(json.toString());
		return httpConenction(builder.toString());

	}

	/***
	 * 上传数据
	 * 
	 * @param json
	 * @return
	 * @throws IOException
	 */
	public static JSONObject upload(JSONObject json) throws IOException {
		try {
			String url = DOMAIN + json.getString(PATH);
			json.remove(PATH);
			json = encodeUrl(json);
			return upload(json, url);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 登录
	 * 
	 * @param cardNo
	 * @param password
	 * @return
	 */
	public static JSONObject login(String cardNo, String password) {
		JSONObject json = new JSONObject();
		JSONObject result = null;
		try {
			json.put("cardNo", cardNo);
			json.put("password", password);
			json.put(PLAT_ID_KEY, PLAT_ID_VALUE);
			String url = DOMAIN + "8031?" + json.toString();
			result = httpConenction(url);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static JSONObject httpConenction(String pathUrl)
			throws JSONException, IOException {
		Log.i(TAG + " httpConenction", pathUrl);
		JSONObject json = new JSONObject();
		// String url =
		// URLEncoder.encode(pathUrl,"UTF-8");
		URL realUrl = new URL(pathUrl);
		HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);// 发送POST请求必须设置允许输出
		conn.setUseCaches(false);// 不使用Cache
		conn.setRequestMethod("POST");
		conn.setReadTimeout(15000);
		conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
		conn.setRequestProperty("Charset", "utf-8");
		conn.setRequestProperty("Content-Length", "0");
		// conn.setRequestProperty("Content-Language",
		// "zh-CN");
		// conn.setRequestProperty("Content-Type",
		// "text/html");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		int state = conn.getResponseCode();
		if (state == 200) {
			InputStream re = conn.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(re));
			StringBuffer buffer = new StringBuffer();
			String line = new String();
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			String responseString = buffer.toString();
			json = new JSONObject(responseString);
		}
		Log.i(TAG, json.toString());
		return json;
	}

	/***
	 * 上传数据
	 * 
	 * @param info
	 * @return
	 */
	public static JSONObject logup(JSONObject info) {
		JSONObject result = null;
		String url = DOMAIN + "8030?" + info.toString();
		try {
			result = httpConenction(url);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/***
	 * 查询数据
	 * 
	 * @param queryPath
	 * @param json
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	public static JSONObject query(String queryPath, Map<String, String> paras)
			throws JSONException, IOException {
		paras.put(PLAT_ID_KEY, PLAT_ID_VALUE);
		// 如果没有指定页面大小,设定一个默认值
		if (!paras.containsKey(PAGECOUNT))
			paras.put(PAGECOUNT, "20");
		// 默认为第一页
		if (!paras.containsKey(PAGEINDEX))
			paras.put(PAGEINDEX, "0");
		paras = encodeUrl(paras);
		JSONObject result = null;
		String url = DOMAIN + queryPath + "?"
				+ new JSONObject(paras).toString();
		result = httpConenction(url);
		return result;
	}

	/***
	 * 对空格和加号进行编码，否则url包含空格会报错
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject encodeUrl(JSONObject json) throws JSONException {
		JSONObject newJson = new JSONObject();
		final String space = " ";
		final String plus = "+";
		Iterator<String> iter = json.keys();
		while (iter.hasNext()) {
			String key = iter.next();
			String value = json.getString(key);
			if (key.contains(space) || key.contains(plus))
				key = URLEncoder.encode(key);
			if (value.contains(space) || value.contains(plus))
				value = URLEncoder.encode(value);
			newJson.put(key, value);
		}
		return newJson;
	}

	private static Map<String, String> encodeUrl(Map<String, String> dataMap) {
		final String space = " ";
		Map<String, String> newMap = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			String key = entry.getKey();
			if (key.contains(space))
				key = URLEncoder.encode(key);
			String value = entry.getValue();
			if (value.contains(space))
				value = URLEncoder.encode(value);
			newMap.put(key, value);
		}
		return newMap;
	}
}
