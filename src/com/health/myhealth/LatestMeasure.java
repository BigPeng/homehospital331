package com.health.myhealth;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.database.Cache;
import com.health.database.Tables;
import com.health.util.LatestRecordAdapter;
import com.health.web.WebService;

/**
 * 最近的测量记录
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-11-27 上午10:59:34
 */
public class LatestMeasure extends BaseActivity {
	private static Context context;
	private LatestRecordAdapter myAdapter;
	private ListView listview;
	private List<String[]> datas = new ArrayList<String[]>();
	private List<JSONObject> jsonDatas = new ArrayList<JSONObject>();
	private final static String[] titles = { "测量时间", "项目", "测量值", "参考值", "状态" };
	Button refreshButton;
	Button homeButton;
	Button backButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.latest_measure);
		context = this;	
		setButtons();

		listview = (ListView) this.findViewById(R.id.latest_record);
		generateData();
		myAdapter = new LatestRecordAdapter(context, datas, jsonDatas);
		listview.setAdapter(myAdapter);
	}

	private void setButtons() {
		refreshButton = (Button) this.findViewById(R.id.refresh_button);
		homeButton = (Button) this.findViewById(R.id.to_home_button);
		backButton = (Button) this.findViewById(R.id.return_button);
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == refreshButton) {
					// 清除上次加载的数据
					clearDataset();
					// 刷新重新加载数据
					generateData();
					myAdapter.notifyDataSetChanged();
				} else if (v == homeButton) {
					LatestMeasure.this.setResult(RESULT_OK);
					LatestMeasure.this.finish();
				} else if (v == backButton) {
					LatestMeasure.this.finish();
				}
			}
		};
		refreshButton.setOnClickListener(listener);
		homeButton.setOnClickListener(listener);
		backButton.setOnClickListener(listener);
	}

	private void clearDataset() {
		datas.clear();
		jsonDatas.clear();
	}

	/***
	 * 构造listview显示的数据
	 */
	private void generateData() {
		datas.add(titles);

		String[] bp = getBp();
		if (bp != null) {
			datas.add(bp);

		}
		String[] pulse = getFromCache(Cache.BP, Tables.PULSE, "脉率(bpm)",
				"[60,100)", WebService.PATH_BP);
		if (pulse != null) {
			datas.add(pulse);

		}
		String[] temp = getFromCache(Cache.TEMP, Tables.TEMP, "体温(℃)",
				"36.2-37.5", WebService.PATH_TEMP);
		if (temp != null) {
			datas.add(temp);

		}
		String[] bo = getFromCache(Cache.BO, Tables.BO, "血氧(%)", "[95%,100%]",
				WebService.PATH_BO);
		if (bo != null) {
			datas.add(bo);

		}
		String[] glu = getFromCache(Cache.GLU, Tables.GLU, "血糖(mg/dL)",
				"餐前:[70,130),餐后：[100,180)", WebService.PATH_GLU);
		if (glu != null) {
			datas.add(glu);

		}
		String[] ua = getFromCache(Cache.UA, Tables.UA, "尿酸(mg/d)",
				"儿童[2.5-5.5],男性[3.4-7.0],女性[2.4-6.0]", WebService.PATH_UA);
		if (ua != null) {
			datas.add(ua);

		}
		String[] chol = getFromCache(Cache.CHOL, Tables.CHOL, "总胆固醇(mg/d)",
				"<200", WebService.PATH_CHOL);
		if (chol != null) {
			datas.add(chol);

		}
		String[] urine = getUrine();
		if (urine != null) {
			datas.add(urine);

		}
	}

	/**
	 * 获取尿液分析11项
	 * 
	 * @return
	 */

	private String[] getUrine() {
		String[] itemName = { getString(R.string.leu), getString(R.string.bld),
				getString(R.string.ph), getString(R.string.pro),
				getString(R.string.ubg), getString(R.string.nit),
				getString(R.string.sg), getString(R.string.ket),
				getString(R.string.bil), getString(R.string.glu),
				getString(R.string.vc) };
		String[] itemTag = { Tables.LEU, Tables.BLD, Tables.PH, Tables.PRO,
				Tables.UBG, Tables.NIT, Tables.SG, Tables.KET, Tables.BIL,
				Tables.UGLU, Tables.VC };
		try {
			JSONObject json = cache.getItem(Cache.URINE);
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < itemName.length; i++) {
				builder.append(itemName[i]);
				builder.append(":");
				builder.append(json.getString(itemTag[i]));
				builder.append(",");
			}
			builder.deleteCharAt(builder.length() - 1);
			json.put(Cache.ITEM, Cache.URINE);// 添加item,在二次上传中使用到
			json.put(WebService.PATH, WebService.PATH_URINE);// 添加上传路径
			jsonDatas.add(json);
			return new String[] { json.getString(Tables.TIME), "尿液分析",
					builder.toString(), "/", json.getString(WebService.STATUS) };
		} catch (Exception e) {
			return null;

		}
	}

	/***
	 * 获取血压两项
	 * 
	 * @return
	 * @throws JSONException
	 */
	private String[] getBp() {
		try {
			JSONObject json = cache.getItem(Cache.BP);
			json.put(Cache.ITEM, Cache.BP);// 添加item,在二次上传中使用到
			json.put(WebService.PATH, WebService.PATH_BP);// 添加上传路径
			jsonDatas.add(json);
			return new String[] {
					json.getString(Tables.TIME),
					"血压(mmHg)",
					"收缩压：" + json.getString(Tables.SBP) + "舒张压："
							+ json.getString(Tables.DBP),
					"收缩压:[90,140)/舒张压:[60,90)",
					json.getString(WebService.STATUS) };
		} catch (Exception e) {
			return null;
		}

	}

	/***
	 * 从缓存中获取最近的记录
	 * 
	 * @param item
	 *            项目标志
	 * @param attr
	 *            属性标志
	 * @param itemName项目名
	 * @param stand
	 *            参考标准
	 * @upUrl path 上传的url路径后缀
	 * @return
	 */
	private String[] getFromCache(String item, String attr, String itemName,
			String stand, String path) {
		try {
			JSONObject json = cache.getItem(item);
			json.put(Cache.ITEM, item);// 添加item,在二次上传中使用到
			json.put(WebService.PATH, path);// 添加上传路径
			jsonDatas.add(json);
			return new String[] { json.getString(Tables.TIME), itemName,
					json.getString(attr), stand,
					json.getString(WebService.STATUS) };
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 设置listview高度，注意listview子项必须为LinearLayout才能调用该方法
	 * 
	 * @param listview
	 *            listview
	 * 
	 */
	@SuppressWarnings("unused")
	private static void setListViewHeight(ListView listview) {
		int totalHeight = 0;
		ListAdapter adapter = listview.getAdapter();
		if (null != adapter) {
			for (int i = 0; i < adapter.getCount(); i++) {
				View listItem = adapter.getView(i, null, listview);
				if (null != listItem) {
					listItem.measure(0, 0);// 注意listview子项必须为LinearLayout才能调用该方法
					totalHeight += listItem.getMeasuredHeight();
				}
			}
			ViewGroup.LayoutParams params = listview.getLayoutParams();
			params.height = totalHeight
					+ (listview.getDividerHeight() * (listview.getCount() - 1));
			listview.setLayoutParams(params);
		}
	}

}
