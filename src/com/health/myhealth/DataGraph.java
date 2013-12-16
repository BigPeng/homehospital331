package com.health.myhealth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.database.Cache;
import com.health.database.Tables;
import com.health.measurement.MeasureUrine;
import com.health.util.MyProgressDialog;
import com.health.util.TimeHelper;
import com.health.web.WebService;

/**
 * ͼ��չʾ����
 * 
 * @author jiqunpeng
 * 
 *         ����ʱ�䣺2013-11-11 ����11:19:17
 */
public class DataGraph extends BaseActivity {
	private static final String TAG = "DataGraph";
	// ��ʾ���ݵ�ʱ���
	private static final String[] TIMES = { "���һ��", "���һ����", "�������", "�������" };
	// ��ʾ����Ŀ
	private static final String[] ITEMS = { "Ѫѹ", "Ѫ��", "����", "����", "Ѫ��", "����",
			"���̴�", "��Һ����" };
	private static final Integer[] INTERVAL = { 7, 30, 183, 356 };

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(2);
	private static GraphicalView lineView;// ����ͼ
	private static Context context;
	private static int timeIndex = 0;// ʱ���
	private static int lastItem = 0;// ѡ�е���Ŀ����
	private static Random random = new Random(23);
	private MyProgressDialog mDialog;
	private static ExecutorService exec = Executors
			.newSingleThreadScheduledExecutor();

	private static Button homeButton;
	private static Button returnButton;

	private static final String SERIESTITLE = "seriesTitle";
	private static final String CHARTITLE = "charTitle";
	private static final String YTITLE = "yTitle";
	private static final String YMIN = "yMin";
	private static final String YMAX = "yMax";
	private static final String PATH = "path";
	private static final String TOKEN = "token";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_graph);
		context = this;
		cache = new Cache(context);
		mDialog = new MyProgressDialog(context);
		initView();

	}

	/**
	 * ��ʼ������
	 */
	private void initView() {
		// ���������б���
		Spinner spinner = (Spinner) this.findViewById(R.id.time_spinner);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, TIMES);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int groupId, long arg3) {
				timeIndex = groupId;
				mDialog.show();// ��ʾ������
				exec.execute(new DataShowService(lastItem, timeIndex));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
		// ���ò鿴��Ŀ
		ListView itemList = (ListView) this.findViewById(R.id.show_items_list);
		ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item);
		for (String item : ITEMS)
			itemAdapter.add(item);
		itemList.setAdapter(itemAdapter);
		itemList.setOnItemClickListener(itemClickListener);
		// ��������ͼ
		LinearLayout lineViewLayout = (LinearLayout) this
				.findViewById(R.id.data_graph_layout);
		lineViewLayout.addView(lineView());
		homeButton = (Button) this.findViewById(R.id.to_home_button);
		returnButton = (Button) this.findViewById(R.id.return_button);
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == homeButton) {
					DataGraph.this.setResult(RESULT_OK);
					DataGraph.this.finish();
				} else if (v == returnButton) {
					DataGraph.this.finish();
				}

			}
		};
		homeButton.setOnClickListener(listener);
		returnButton.setOnClickListener(listener);

	}

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				switch (msg.what) {
				case 0:
					Toast.makeText(getApplicationContext(), "������",
							Toast.LENGTH_LONG).show();
					break;
				case 1:
					Toast.makeText(getApplicationContext(), "�����쳣",
							Toast.LENGTH_LONG).show();
					break;
				}

		}
	};

	// ��Ŀѡַ������
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view,
				final int position, long arg3) {
			// Toast.makeText(context, position + " ",
			// Toast.LENGTH_SHORT).show();
			lastItem = position;
			mDialog.show();// ��ʾ������
			exec.execute(new DataShowService(position, timeIndex));
		}
	};

	private class DataShowService implements Runnable {
		private int item;
		private int timeIndex;
		private final String cardNo = cache.getUserId();

		public DataShowService(int item, int timeIndex) {
			this.item = item;
			this.timeIndex = timeIndex;
		}

		@Override
		public void run() {
			try {
				JSONObject json = new JSONObject();
				switch (item) {
				case 0:
					showBp(timeIndex, cardNo);
					break;
				case 1:
					json.put(TOKEN, Tables.BO);
					json.put(PATH, WebService.PATH_QUERY_BO);
					json.put(SERIESTITLE, "Ѫ��");
					json.put(CHARTITLE, "Ѫ������ͼ");
					json.put(YTITLE, "Ѫ��������%��");
					json.put(YMIN, 80);
					json.put(YMAX, 105);
					showSingleLine(json, cardNo);
					break;
				case 2:
					json.put(TOKEN, Tables.PULSE);
					json.put(PATH, WebService.PATH_QUERY_PULSE);
					json.put(SERIESTITLE, "����");
					json.put(CHARTITLE, "��������ͼ");
					json.put(YTITLE, "���ʣ���/���ӣ�");
					json.put(YMIN, 40);
					json.put(YMAX, 150);
					showSingleLine(json, cardNo);
					break;
				case 3:
					json.put(TOKEN, Tables.TEMP);
					json.put(PATH, WebService.PATH_QUERY_TEMP);
					json.put(SERIESTITLE, "����");
					json.put(CHARTITLE, "��������ͼ");
					json.put(YTITLE, "���£����϶ȣ�");
					json.put(YMIN, 28);
					json.put(YMAX, 45);
					showSingleLine(json, cardNo);
					break;
				case 4:
					json.put(TOKEN, Tables.GLU);
					json.put(PATH, WebService.PATH_QUERY_GLU);
					json.put(SERIESTITLE, "Ѫ��");
					json.put(CHARTITLE, "Ѫ������ͼ");
					json.put(YTITLE, "Ѫ�ǣ�mml/L��");
					json.put(YMIN, 1);
					json.put(YMAX, 34);
					showSingleLine(json, cardNo);
					break;
				case 5:
					json.put(TOKEN, Tables.UA);
					json.put(PATH, WebService.PATH_QUERY_UA);
					json.put(SERIESTITLE, "����");
					json.put(CHARTITLE, "��������ͼ");
					json.put(YTITLE, "���ᣨmml/L��");
					json.put(YMIN, 0.15);
					json.put(YMAX, 1.22);
					showSingleLine(json, cardNo);
					break;
				case 6:
					json.put(TOKEN, Tables.CHOL);
					json.put(PATH, WebService.PATH_QUERY_CHOL);
					json.put(SERIESTITLE, "�ܵ��̴�");
					json.put(CHARTITLE, "�ܵ��̴�����ͼ");
					json.put(YTITLE, "�ܵ��̴���mml/L��");
					json.put(YMIN, 2.5);
					json.put(YMAX, 10.5);
					showSingleLine(json, cardNo);
					break;
				case 7:
					showNull();
					break;
				}
			} catch (IOException e) {// ���������쳣��Ӧ����
				e.printStackTrace();
				Message message = new Message();
				message.what = 1;
				mHandler.sendMessage(message);
				setXAxis(mRenderer);
			} catch (JSONException e) {
				e.printStackTrace();
				setXAxis(mRenderer);
			}
			mDialog.cancel();
		}
	}

	/**
	 * ��Ѫ������ͼ
	 * 
	 * @return
	 */
	public View lineView() {
		XYSeriesRenderer xRenderer = new XYSeriesRenderer();// (������һ���߶���)
		XYSeriesRenderer yRenderer = new XYSeriesRenderer();// (������һ���߶���)
		TimeSeries ySeries = new TimeSeries("");
		TimeSeries xSeries = new TimeSeries("");
		for (int i = 0; i < 0; i++) {
			xSeries.add(i, Math.cos(i));
			ySeries.add(i, 0);
		}
		mDataset.addSeries(xSeries);
		mDataset.addSeries(ySeries);
		// ����ͼ���X��ĵ�ǰ����
		mRenderer
				.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setAxisTitleTextSize(15);// ����������ı���С
		mRenderer.setChartTitleTextSize(15);// ����ͼ��������ֵĴ�С
		// mRenderer.setLabelsTextSize(18);// ���ñ�ǩ�����ִ�С
		mRenderer.setYLabels(10);// y����ʾ�ĵ���
		mRenderer.setXLabels(10);// x����ʾ�ĵ���
		mRenderer.setYLabelsColor(1, Color.BLACK);
		mRenderer.setLegendTextSize(20);// ����ͼ���ı���С
		mRenderer.setPointSize(5f);// ���õ�Ĵ�С
		setXAxis(mRenderer);
		mRenderer.setShowGrid(true);// ��ʾ����
		mRenderer.setShowLabels(true);
		mRenderer.setShowLegend(true);
		mRenderer.setMargins(new int[] { 1, 15, 1, 1 });// ������ͼλ��
		xRenderer.setColor(Color.BLUE);// ������ɫ
		xRenderer.setPointStyle(PointStyle.CIRCLE);// ���õ����ʽ
		xRenderer.setFillPoints(true);// ���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�
		xRenderer.setLineWidth(2);// �����߿�
		mRenderer.addSeriesRenderer(xRenderer);
		yRenderer.setColor(Color.GREEN);// ������ɫ
		yRenderer.setPointStyle(PointStyle.CIRCLE);// ���õ����ʽ
		yRenderer.setFillPoints(true);// ���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�
		yRenderer.setLineWidth(2);// �����߿�
		mRenderer.addSeriesRenderer(yRenderer);
		mRenderer.setMarginsColor(Color.WHITE);// ��������Ϊ͸��
		mRenderer.setPanEnabled(true, false);// ���ò��������ƶ�����
		mRenderer.setDisplayChartValues(true);
		mRenderer.setSelectableBuffer(10);
		// lineView =
		// ChartFactory.getCubeLineChartView(this,
		// mDataset, mRenderer,
		// 0.1f);
		lineView = ChartFactory.getTimeChartView(this, mDataset, mRenderer,
				"yyyy/MM/dd HH:mm");
		lineView.setBackgroundColor(Color.WHITE);
		return lineView;
	}

	/**
	 * ��ʾû������
	 */
	public void showNull() {

		XYSeries[] xySeries = mDataset.getSeries();
		for (XYSeries series : xySeries) {
			series.setTitle("");
			series.clear();
		}
		mRenderer.setChartTitle("");
		mRenderer.setYTitle("");
		lineView.repaint();// �ػ�
		try {
			TimeUnit.MILLISECONDS.sleep(random.nextInt(800) + 300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message message = new Message();
		message.what = 0;
		mHandler.sendMessage(message);
	}

	public void showSingleLine(JSONObject json, String cardNo)
			throws IOException, JSONException {
		XYSeries[] xySeries = mDataset.getSeries();
		XYSeries series = xySeries[0];
		series.setTitle(json.getString(SERIESTITLE));
		series.clear();// ���֮ǰ������
		xySeries[1].clear();
		xySeries[1].setTitle("");
		Map<String, String> paras = getParas(timeIndex, cardNo);
		double[][] boData = DataService.getSingleData(paras,
				json.getString(PATH), json.getString(TOKEN));
		int dataNum = boData[0].length;
		for (int i = 0; i < dataNum; i++) {
			series.add(boData[0][i], boData[1][i]);
		}
		mRenderer.setChartTitle(json.getString(CHARTITLE));
		mRenderer.setYTitle(json.getString(YTITLE));
		mRenderer.setYAxisMin(json.getInt(YMIN));// ����y����Сֵ
		mRenderer.setYAxisMax(json.getInt(YMAX));
		setXAxis(mRenderer);
		lineView.repaint();// �ػ�
	}

	/***
	 * ���õ�ǰ��ʾ��ΧΪ���10��
	 * 
	 * @param mRenderer
	 */
	private void setXAxis(XYMultipleSeriesRenderer mRenderer) {
		long current = TimeHelper.getCurrentDate().getTime();
		mRenderer.setXAxisMin(current - TimeHelper.MILLSEC_DAY * 7);
		mRenderer.setXAxisMax(current);
	}

	/**
	 * ��ʾѪѹ����
	 * 
	 * @param cardNo
	 * 
	 * @param timeIndex2
	 * @throws IOException
	 */
	protected void showBp(int timeIndex, String cardNo) throws IOException {
		XYSeries[] series = mDataset.getSeries();
		XYSeries lowBpSeries = series[0];
		XYSeries highBpSeries = series[1];
		lowBpSeries.setTitle("��ѹ");
		highBpSeries.setTitle("��ѹ");
		lowBpSeries.clear();
		highBpSeries.clear();
		Map<String, String> paras = getParas(timeIndex, cardNo);
		double[][] bpData = DataService.getBpData(paras);
		int dataNum = bpData[0].length;
		for (int i = 0; i < dataNum; i++) {
			lowBpSeries.add(bpData[0][i], bpData[1][i]);
			highBpSeries.add(bpData[0][i], bpData[2][i]);
		}
		mRenderer.setChartTitle("Ѫѹ����ͼ");
		mRenderer.setYTitle("Ѫѹֵ��mmHg)");
		mRenderer.setYAxisMin(50);// ����y����Сֵ��25
		mRenderer.setYAxisMax(200);
		setXAxis(mRenderer);
		lineView.repaint();// �ػ�
	}

	/***
	 * ���ò�ѯ����
	 * 
	 * @param timeIndex
	 * @param cardNo
	 * @return
	 */
	private static Map<String, String> getParas(int timeIndex, String cardNo) {
		String endTime = TimeHelper.getCurrentTime();
		String startTime = TimeHelper.getBeforeTime(INTERVAL[timeIndex]);
		Map<String, String> paras = new HashMap<String, String>();
		paras.put(WebService.ENDTIME, endTime);
		paras.put(WebService.STARTTIME, startTime);
		paras.put(Tables.CARDNO, cardNo);
		return paras;
	}
}
