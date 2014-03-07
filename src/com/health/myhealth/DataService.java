package com.health.myhealth;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import cn.younext.R;

import com.health.database.Tables;
import com.health.util.TimeHelper;
import com.health.web.WebService;

public class DataService {

	private static final TreeMap<Double, double[]> EMPTY = new TreeMap<Double, double[]>() {

		private static final long serialVersionUID = 4717608862679765422L;

		{

			put((double) System.currentTimeMillis(),
					new double[] { -100, -100 });
		}
	};

	/**
	 * 获取血压数据用于显示曲线
	 * 
	 * @param timeIndex
	 * @param cardNo
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	// public static double[][]
	// getBpDataForCurve(Map<String, String> paras)
	// throws IOException {
	// try {
	//
	// JSONObject result =
	// WebService.query(WebService.PATH_QUERY_BP,
	// paras);
	// int statusCode =
	// result.getInt(WebService.STATUS_CODE);
	// if (statusCode == WebService.OK) {
	// JSONArray datas =
	// result.getJSONArray(WebService.DATA);
	// double[][] bpData = new
	// double[3][datas.length()];
	// for (int i = 0; i < datas.length(); i++) {
	// JSONObject data = datas.getJSONObject(i);
	// String checkTime =
	// data.getString(WebService.CHECKTIME);
	// long time;
	// try {
	// time = TimeHelper.parseTime(checkTime);
	// } catch (ParseException e) {
	// e.printStackTrace();
	// continue;// 如果时间解析错误就跳过这个数据
	// }
	// bpData[0][i] = time;
	// bpData[1][i] = data.getInt(Tables.SBP);
	// bpData[2][i] = data.getInt(Tables.DBP);
	// }
	// // 排序
	// bpData = sort(bpData);
	// return bpData;
	// } else
	// return EMPTY;
	// } catch (JSONException e) {
	// e.printStackTrace();
	// return EMPTY;
	// }
	//
	// }

	/***
	 * 將数据按时间排序,不排序的数据加载会错误
	 * 
	 * @param bpData
	 * @return
	 */
	public static double[][] sort(double[][] data) {
		int len = data[0].length;
		for (int i = 0; i < len; i++) {
			for (int j = i; j < len; j++) {
				if (data[0][i] > data[0][j]) {
					swap(data, i, j);
				}
			}
		}

		return data;
	}

	private static void swap(double[][] data, int i, int j) {
		for (int k = 0; k < data.length; k++) {
			double temp = data[k][i];
			data[k][i] = data[k][j];
			data[k][j] = temp;
		}
	}

	/***
	 * 获取只有一个类型的数据,出血压脉率和尿液之外,其他都可以走这里 用于显示曲线
	 * 
	 * @param paras
	 * @param path
	 * @param token要获取的数据的标志
	 * @return
	 * @throws IOException
	 */
	public static TreeMap<Double, double[]> getSingleDataForCurve(
			Map<String, String> paras, String path, String token)
			throws IOException {
		try {
			JSONObject result = WebService.query(path, paras);
			int statusCode = result.getInt(WebService.STATUS_CODE);
			if (statusCode == WebService.OK) {
				JSONArray datas = result.getJSONArray(WebService.DATA);
				TreeMap<Double, double[]> sortMap = new TreeMap<Double, double[]>();
				
				for (int i = 0; i < datas.length(); i++) {
					JSONObject data = datas.getJSONObject(i);
					String checkTime = data.getString(WebService.CHECKTIME);
					long time;
					try {
						time = TimeHelper.parseTime(checkTime);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;// 如果时间解析错误就跳过这个数据
					}
					double[] values;
					if (token.equals(Tables.SBP))
						values = new double[2];
					else
						values = new double[1];
					values[0] = data.getDouble(token);
					if (token.equals(Tables.SBP))
						values[1] = data.getDouble(Tables.DBP);
					sortMap.put((double) time, values);
				}
				return sortMap;
			} else
				return EMPTY;
		} catch (JSONException e) {
			e.printStackTrace();
			return EMPTY;
		}
	}

	public static List<String[]> getDataForTable(Context context,
			Map<String, String> paras, String path, String token)
			throws JSONException, IOException {
		JSONObject result = WebService.query(path, paras);
		int statusCode = result.getInt(WebService.STATUS_CODE);
		List<String[]> records = new ArrayList<String[]>();
		if (statusCode == WebService.OK) {
			JSONArray datas = result.getJSONArray(WebService.DATA);
			for (int i = 0; i < datas.length(); i++) {
				JSONObject data = datas.getJSONObject(i);
				String[] record = new String[3];
				String checkTime = data.getString(WebService.CHECKTIME);
				record[0] = checkTime;
				if (token.equals(Tables.SBP)) {
					record[1] = "收缩压:" + data.getString(Tables.SBP) + " / 舒张压:"
							+ data.getString(Tables.DBP);
				} else if (token.equals(Tables.LEU)) {
					String urine = getUrineData(context, data);
					if (null == urine)
						continue;
					
					else
						record[1] = urine;
				} else
					record[1] = data.getString(token);
				record[2] = "/";// 分析结果暂时为空
				records.add(record);
			}
		}
		return records;
	}

	/***
	 * 解析出尿常规11项
	 * 
	 * @param context
	 * @param data
	 * @return
	 */
	private static String getUrineData(Context context, JSONObject data) {
		String[] itemName = { context.getString(R.string.leu),
				context.getString(R.string.bld),
				context.getString(R.string.ph),
				context.getString(R.string.pro),
				context.getString(R.string.ubg),
				context.getString(R.string.nit),
				context.getString(R.string.sg),
				context.getString(R.string.ket),
				context.getString(R.string.bil),
				context.getString(R.string.glu), context.getString(R.string.vc) };
		String[] itemTag = { Tables.LEU, Tables.BLD, Tables.PH, Tables.PRO,
				Tables.UBG, Tables.NIT, Tables.SG, Tables.KET, Tables.BIL,
				Tables.UGLU, Tables.VC };
		try {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < itemName.length; i++) {
				builder.append(itemName[i]);
				builder.append(":");
				builder.append(data.getString(itemTag[i]));
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			return builder.toString();
		} catch (Exception e) {
			return null;
		}

	}

	public static Map<Long, String> getUrineRecordsForChart(Context context,
			Map<String, String> parameters, String path, String token) {
		try {
			Map<Long, String> urineRecordsMap = new TreeMap<Long, String>();
			JSONObject result = WebService.query(path, parameters);
			int statusCode = result.getInt(WebService.STATUS_CODE);
			if (statusCode == WebService.OK) {
				JSONArray datas = result.getJSONArray(WebService.DATA);
				for (int i = 0; i < datas.length(); i++) {
					JSONObject data = datas.getJSONObject(i);
					String checkTime = data.getString(WebService.CHECKTIME);
					long time;
					try {
						time = TimeHelper.parseTime(checkTime);
					} catch (ParseException e) {
						e.printStackTrace();
						continue;// 如果时间解析错误就跳过这个数据
					}
					String record = getUrineRecord(data);
					if (!urineRecordsMap.containsKey(time)) {
						urineRecordsMap.put(time, record);
					}
				}
				return urineRecordsMap;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String getUrineRecord(JSONObject data) {
		String[] itemTag = { Tables.LEU, Tables.BLD, Tables.PH, Tables.PRO,
				Tables.UBG, Tables.NIT, Tables.SG, Tables.KET, Tables.BIL,
				Tables.UGLU, Tables.VC };
		try {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < itemTag.length; i++) {
				builder.append(itemTag[i]);
				builder.append(":");
				builder.append(data.getString(itemTag[i]).trim());
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			return builder.toString();
		} catch (Exception e) {
			return null;
		}
	}

}
