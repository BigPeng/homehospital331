package com.health.myhealth;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.database.Cache;
import com.health.database.Tables;
import com.health.util.BigListAdapter;
import com.health.util.MyListAdapter;
import com.health.web.WebService;

/**
 * ����Ĳ�����¼
 * 
 * @author jiqunpeng
 * 
 *         ����ʱ�䣺2013-11-27 ����10:59:34
 */
public class LatestMeasure extends BaseActivity {
	private static Context context;
	private MyListAdapter[] myAdapter = new MyListAdapter[6];
	private BigListAdapter urineAdapter;
	private ListView[] listview = new ListView[7];
	private List<List<String[]>> datas = new ArrayList<List<String[]>>();
	private List<List<Boolean>> isTitles = new ArrayList<List<Boolean>>();
	private static final String[] EMPTY = { "", "", "", "", WebService.UPLOADED };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.latest_measure);
		context = this;
		cache = new Cache(context);
		listview[0] = (ListView) this.findViewById(R.id.latest_bp_pulse);
		listview[1] = (ListView) this.findViewById(R.id.latest_temp);
		listview[2] = (ListView) this.findViewById(R.id.latest_bo);
		listview[3] = (ListView) this.findViewById(R.id.latest_glu);
		listview[4] = (ListView) this.findViewById(R.id.latest_ua);
		listview[5] = (ListView) this.findViewById(R.id.latest_chol);
		listview[6] = (ListView) this.findViewById(R.id.latest_urine);
		generateData();
		for (int i = 0; i < 6; i++) {

			myAdapter[i] = new MyListAdapter(context, datas.get(i),
					isTitles.get(i), 5);
			listview[i].setAdapter(myAdapter[i]);
			setListViewHeight(listview[i]);
		}
		urineAdapter = new BigListAdapter(context, datas.get(6),
				isTitles.get(6), 13);
		listview[6].setAdapter(urineAdapter);
		setListViewHeight(listview[6]);

	}

	private void generateData() {
		String[] title = new String[] { "����ʱ��", "����ѹ", "����ѹ", "����", "״̬" };
		String[] record = getBpPulse();
		add(title, record);
		title = new String[] { "����ʱ��", " ", "����", " ", "״̬" };
		record = getFromCache(Cache.TEMP, Tables.TEMP);
		add(title, record);
		title = new String[] { "����ʱ��", " ", "Ѫ��", " ", "״̬" };
		record = getFromCache(Cache.BO, Tables.BO);
		add(title, record);
		title = new String[] { "����ʱ��", " ", "������", " ", "״̬" };
		record = getFromCache(Cache.GLU, Tables.GLU);
		add(title, record);
		title = new String[] { "����ʱ��", " ", "����", " ", "״̬" };
		record = getFromCache(Cache.UA, Tables.UA);
		add(title, record);
		title = new String[] { "����ʱ��", " ", "�ܵ��̴�", " ", "״̬" };
		record = getFromCache(Cache.CHOL, Tables.CHOL);
		add(title, record);
		title = new String[] { "����ʱ��", "��ϸ��", "��������", "��ԭ", "������", "pHֵ",
				"ǱѪ", "����", "ͪ��", "������", "������", "ά����C", "״̬" };
		record = getUrine();
		add(title, record);
	}

	private String[] getUrine() {
		String[] empty = { "2013-11-28 11:12:58", "1", "2", "3", "4", "1", "2",
				"3", "4", "1", "2", "3", "δ�ϴ�" };
		try {
			JSONObject json = cache.getItem(Cache.URINE);
			if (json == null)
				return empty;
			return new String[] { json.getString(Tables.TIME),
					json.getString(Tables.LEU), json.getString(Tables.NIT),
					json.getString(Tables.UBG), json.getString(Tables.PRO),
					json.getString(Tables.PH), json.getString(Tables.BLD),
					json.getString(Tables.SG), json.getString(Tables.KET),
					json.getString(Tables.BIL), json.getString(Tables.UGLU),
					json.getString(Tables.VC),
					json.getString(WebService.STATUS),
					json.getString(Tables.DEVICENAME),
					json.getString(Tables.DEVICENAME) };
		} catch (JSONException e) {
			e.printStackTrace();
			return empty;
		}
	}

	private String[] getFromCache(String item, String attr) {
		try {
			JSONObject json = cache.getItem(item);
			if (json == null)
				return EMPTY;
			return new String[] { json.getString(Tables.TIME), "",
					json.getString(attr), "",
					json.getString(WebService.STATUS),
					json.getString(Tables.DEVICENAME),
					json.getString(Tables.DEVICENAME) };
		} catch (JSONException e) {
			e.printStackTrace();
			return EMPTY;
		}
	}

	private void add(String[] title, String[] content) {
		List<String[]> dataContent = new ArrayList<String[]>();
		List<Boolean> isTitle = new ArrayList<Boolean>();
		dataContent.add(title);
		isTitle.add(true);
		dataContent.add(content);
		isTitle.add(false);
		datas.add(dataContent);
		isTitles.add(isTitle);

	}

	/**
	 * �ӻ����л�ȡѪѹ������ֵ
	 * 
	 * @return
	 */
	private String[] getBpPulse() {
		try {
			JSONObject json = cache.getItem(Cache.BP);
			if (json == null)
				return EMPTY;
			return new String[] { json.getString(Tables.TIME),
					json.getString(Tables.SBP), json.getString(Tables.DBP),
					json.getString(Tables.DBP),
					json.getString(WebService.STATUS),
					json.getString(Tables.DEVICENAME),
					json.getString(Tables.DEVICENAME) };
		} catch (JSONException e) {
			e.printStackTrace();
			return EMPTY;
		}
	}

	/**
	 * ����listview�߶ȣ�ע��listview�������ΪLinearLayout���ܵ��ø÷���
	 * 
	 * @param listview
	 *            listview
	 * 
	 */
	public static void setListViewHeight(ListView listview) {
		int totalHeight = 0;
		ListAdapter adapter = listview.getAdapter();
		if (null != adapter) {
			for (int i = 0; i < adapter.getCount(); i++) {
				View listItem = adapter.getView(i, null, listview);
				if (null != listItem) {
					listItem.measure(0, 0);// ע��listview�������ΪLinearLayout���ܵ��ø÷���
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
