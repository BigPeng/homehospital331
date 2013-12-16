package com.health.myhealth;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.health.database.Tables;
import com.health.util.TimeHelper;
import com.health.web.WebService;

public class DataService {

	private static Random random = new Random(47);

	/**
	 * ��ȡѪѹ����
	 * 
	 * @param timeIndex
	 * @param cardNo
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static double[][] getBpData(Map<String, String> paras)
			throws IOException {
		try {

			JSONObject result = WebService.query(WebService.PATH_QUERY_BP,
					paras);
			int statusCode = result.getInt(WebService.STATUS_CODE);
			if (statusCode == WebService.OK) {
				JSONArray datas = result.getJSONArray(WebService.DATA);
				double[][] bpData = new double[3][datas.length()];
				for (int i = 0; i < datas.length(); i++) {
					JSONObject data = datas.getJSONObject(i);
					String checkTime = data.getString(WebService.CHECKTIME);
					long time;
					try {
						time = TimeHelper.parseTime(checkTime);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;// ���ʱ���������������������
					}
					bpData[0][i] = time;
					bpData[1][i] = data.getInt(Tables.SBP);
					bpData[2][i] = data.getInt(Tables.DBP);
					
				}
				//����
				sort(bpData);
				return bpData;
			} else
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

	}

	/***
	 * �����ݰ�ʱ������,����������ݼ��ػ����
	 * 
	 * @param bpData
	 */
	private static void sort(double[][] data) {
		int len = data[0].length;
		for (int i = 0; i < len; i++) {
			for (int j = i; j < len; j++) {
				if (data[0][i] > data[0][j]) {
					double temp = data[0][i];
					data[0][i] = data[0][j];
					data[0][j] = temp;
				}
			}
		}
	}

	/***
	 * ��ȡֻ��һ�����͵�����,��Ѫѹ���ʺ���Һ֮��,����������������
	 * 
	 * @param paras
	 * @param path
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public static double[][] getSingleData(Map<String, String> paras,
			String path, String token) throws IOException {
		try {
			JSONObject result = WebService.query(path, paras);
			int statusCode = result.getInt(WebService.STATUS_CODE);
			if (statusCode == WebService.OK) {
				JSONArray datas = result.getJSONArray(WebService.DATA);
				double[][] record = new double[2][datas.length()];
				for (int i = 0; i < datas.length(); i++) {
					JSONObject data = datas.getJSONObject(i);
					String checkTime = data.getString(WebService.CHECKTIME);
					long time;
					try {
						time = TimeHelper.parseTime(checkTime);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;// ���ʱ���������������������
					}
					record[0][i] = time;
					record[1][i] = data.getInt(token);
					
				}
				sort(record);
				return record;
			} else
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

}
