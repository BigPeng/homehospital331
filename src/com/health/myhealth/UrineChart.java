package com.health.myhealth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.database.Cache;
import com.health.database.Tables;
import com.health.device.GmpUa;
import com.health.util.TimeHelper;
import com.health.web.WebService;

public class UrineChart extends BaseActivity {
	private static final String TAG = "urine_chart";
	public static final String SHOW_WHAT = "showWhat";
	private static final String PATH = WebService.PATH_QUERY_URINE;

	private static XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private static XYMultipleSeriesRenderer mRenderer;
	private static GraphicalView mChartView;
	private String[] titles = new String[] { Tables.LEU, Tables.NIT,
			Tables.UBG, Tables.PRO, Tables.BLD, Tables.KET, Tables.BIL,
			Tables.GLU, Tables.VC, Tables.PH, Tables.SG };
	private static final String CHART_TITLE = "";
	private static final String X_TITLE = "";
	private static final String Y_TITLE = "";
	private static final int Y_MIN = 57;
	private static final int Y_MAX = 67;
	private static final long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

	private Context context;
	private Button bpButton; // 血压
	private Button pulseButton; // 脉率
	private Button boButton; // 血氧
	private Button tempButton; // 体温
	private Button gluButton; // 血糖
	private Button uaButton; // 尿酸
	private Button cholButton; // 总胆固醇
	private Button urineButton;// 尿常规
	private Button tableShowButton; // 表格
	private Button curveShowButton; // 曲线
	private Button homeButton; // 回主界面
	private Button backButton; // 后退
	private LinearLayout mChartLayout;
	private String cPath = WebService.PATH_QUERY_BP;// 当前项目的后缀
	private String cToken = Tables.LEU;;// 当前项目
	private Bundle returnBundle = new Bundle();

	private List<Date> time = new ArrayList<Date>();
	private List<ArrayList<Integer>> urineData = new ArrayList<ArrayList<Integer>>();
	private final String[] itemValues = new String[] { "sg:1", "sg:1.005",
			"sg:1.01", "sg:1.015", "sg:1.02", "sg:1.025", "sg:1.03", "sg:sg",
			"ph:5.0", "ph:6.0", "ph:6.5", "ph:7.0", "ph:7.5", "ph:8.0",
			"ph:8.5", "ph:ph", "vc:-", "vc:+-", "vc:+1", "vc:+2", "vc:+3",
			"vc:vc", "glu:-", "glu:+-", "glu:+1", "glu:+2", "glu:+3", "glu:+4",
			"glu:glu", "bil:-", "bil:+1", "bil:+2", "bil:+3", "bil:bil",
			"ket:-", "ket:+-", "ket:+1", "ket:+2", "ket:+-", "ket:ket",
			"bld:-", "bld:+-", "bld:+1", "bld:+2", "bld:+3", "bld:bld",
			"pro:-", "pro:+-", "pro:+1", "pro:+2", "pro:+3", "pro:+4",
			"pro:pro", "ubg:-", "ubg:+1", "ubg:+2", "ubg:+3", "ubg:ubg",
			"nit:-", "nit:+", "nit:nit", "leu:-", "leu:+-", "leu:+1", "leu:+2",
			"leu:+3", "leu:leu" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.urine_chart);
		context = this;
		cache = new Cache(context);
		findView();
		initChartView();
		mChartLayout.addView(mChartView);
		setButtonListener();
		curveShowButton.setSelected(true);
		tableShowButton.setSelected(false);
		PaintTask paintTask = new PaintTask(context);
		paintTask.execute();
		// new Thread(new PaintingTask()).start();
		setChartViewListener();
	}

	private void setChartViewListener() {
		mChartView.setOnTouchListener(new OnTouchListener() {
			Toast toast = new Toast(getApplicationContext());

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				SeriesSelection seriesSelection = mChartView
						.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					Log.i(TAG, seriesSelection.getXValue() + "seriesSelection");
					LayoutInflater inflater = getLayoutInflater();
					View view = inflater.inflate(R.layout.showmsg,
							(ViewGroup) findViewById(R.id.showmsg));
					TextView valueTV = (TextView) view
							.findViewById(R.id.measure_value);
					TextView timeTV = (TextView) view
							.findViewById(R.id.measure_time);
					String time = TimeHelper
							.getStringTime((long) seriesSelection.getXValue());
					double value = seriesSelection.getValue();
					valueTV.setText("测量值:" + itemValues[(int) value]);
					timeTV.setText("测量时间:" + time);
					toast.setView(view);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.LEFT,
							(int) (event.getX()), (int) (event.getY()));
					toast.show();
				}
				return false;
			}

		});
	}

	private void findView() {
		bpButton = (Button) findViewById(R.id.show_bp);
		pulseButton = (Button) findViewById(R.id.show_pulse);
		boButton = (Button) findViewById(R.id.show_bo);
		tempButton = (Button) findViewById(R.id.show_temp);
		gluButton = (Button) findViewById(R.id.show_glu);
		uaButton = (Button) findViewById(R.id.show_ua);
		cholButton = (Button) findViewById(R.id.show_chol);
		urineButton = (Button) findViewById(R.id.show_urine);
		urineButton.setSelected(true);
		tableShowButton = (Button) findViewById(R.id.table_show);
		curveShowButton = (Button) findViewById(R.id.curve_show);
		homeButton = (Button) findViewById(R.id.to_home_button);
		backButton = (Button) findViewById(R.id.return_button);
		mChartLayout = (LinearLayout) findViewById(R.id.urine_chart);
	}

	private void initChartView() {
		int[] colors = new int[] { Color.BLUE, Color.GREEN, Color.CYAN,
				Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.RED,
				Color.BLUE, Color.GREEN, Color.CYAN };
		PointStyle[] pointStyles = new PointStyle[] { PointStyle.CIRCLE,
				PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE,
				PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE,
				PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE,
				PointStyle.CIRCLE };

		// create a set of initial data
		List<Date> initDate = new ArrayList<Date>();
		initDate.add(new Date());
		List<ArrayList<Integer>> initValue = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < titles.length; i++) {
			ArrayList<Integer> data = new ArrayList<Integer>();
			data.add(0);
			initValue.add(data);
		}

		// build dataset, set the renderer and draw
		// chart
		buildDataset(titles, initDate, initValue);
		List<String> yTextLabel = getYTextLabel();
		mRenderer = buildRenderer(colors, pointStyles, yTextLabel);
		Long curTime = new Date().getTime();
		long aWeekBefore = curTime - MILLISECONDS_PER_DAY * 7;
		setChartSettings(mRenderer, CHART_TITLE, X_TITLE, Y_TITLE, aWeekBefore,
				curTime, Y_MIN, Y_MAX, Color.LTGRAY, Color.LTGRAY);
		mChartView = ChartFactory.getTimeChartView(context, mDataset,
				mRenderer, "yy/MM/dd hh:mm");
	}

	private List<String> getYTextLabel() {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < itemValues.length; i++) {
			String[] strArray = itemValues[i].split(":");
			String s = strArray[1];
			if (s.equals(Tables.LEU)) {
				result.add("白细胞");
			} else if (s.equals(Tables.NIT)) {
				result.add("亚硝酸盐");
			} else if (s.equals(Tables.PH)) {
				result.add("PH值");
			} else if (s.equals(Tables.UBG)) {
				result.add("尿胆原");
			} else if (s.equals(Tables.PRO)) {
				result.add("蛋白质");
			} else if (s.equals(Tables.BLD)) {
				result.add("潜血");
			} else if (s.equals(Tables.KET)) {
				result.add("酮体");
			} else if (s.equals(Tables.BIL)) {
				result.add("胆红素");
			} else if (s.equals(Tables.GLU)) {
				result.add("葡萄糖");
			} else if (s.equals(Tables.VC)) {
				result.add("维生素C");
			} else if (s.equals(Tables.SG)) {
				result.add("比重");
			} else {
				result.add(s + "    ");
			}
		}
		return result;
	}

	/**
	 * Builds an XY multiple dataset using the provided
	 * values.
	 * 
	 * @param titles
	 *            the series titles
	 * @param xValues
	 *            xValues the values for the X axis
	 * @param yValues
	 *            yValues the values for the Y axis
	 * @return the XY multiple dataset
	 */
	private void buildDataset(String[] titles, List<Date> xValues,
			List<ArrayList<Integer>> yValues) {
		int cnt = mDataset.getSeriesCount();
		if (cnt == 0) {
			for (int i = 0; i < titles.length; i++) {
				TimeSeries ts = new TimeSeries(titles[i]);
				mDataset.addSeries(ts);
			}
		} else {
			XYSeries[] series = mDataset.getSeries();
			for (int i = 0; i < titles.length; i++) {
				series[i].clear();
			}

			for (int i = 0; i < titles.length; i++) {
				for (int j = 0; j < xValues.size(); j++) {
					series[i].add(Double.valueOf(xValues.get(j).getTime()),
							yValues.get(i).get(j));
				}

			}
		}
	}

	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors
	 *            the series rendering colors
	 * @param pointStyles
	 *            the series point styles
	 * @return the XY multiple series renderers
	 */
	private XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] pointStyles, List<String> yTextLabel) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(25);
		renderer.setChartTitleTextSize(30);
		renderer.setLabelsTextSize(12);
		renderer.setLegendTextSize(12);
		renderer.setPointSize(5f);
		renderer.setXLabels(7);
		renderer.setYLabels(20);
		renderer.setMarginsColor(Color.WHITE);// 背景设置为透明
		// renderer.setXLabelsAlign(Align.RIGHT);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setShowGrid(true);
		renderer.setMargins(new int[] { 10, 50, 10, 10 });
		for (int i = 0; i < colors.length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(pointStyles[i]);
			r.setFillPoints(true);
			r.setLineWidth(2);
			renderer.addSeriesRenderer(r);
		}
		renderer.setYLabels(0);
		int i = 0;
		for (String entry : yTextLabel) {
			renderer.addYTextLabel(i, entry);
			i++;
		}
		// renderer.setDisplayChartValues(true);
		return renderer;

	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer
	 *            the renderer to set the properties to
	 * @param title
	 *            the chart title
	 * @param xTitle
	 *            the title for the X axis
	 * @param yTitle
	 *            the title for the Y axis
	 * @param xMin
	 *            the minimum value on the X axis
	 * @param xMax
	 *            the maximum value on the X axis
	 * @param yMin
	 *            the minimum value on the Y axis
	 * @param yMax
	 *            the maximum value on the Y axis
	 * @param axesColor
	 *            the axes color
	 * @param labelsColor
	 *            the labels color
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	private void setButtonListener() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == curveShowButton) {

				} else if (v == tableShowButton) {
					cPath = WebService.PATH_QUERY_URINE;
					returnBundle.putString(TableRecord.SELECTED_TOKEN, cPath);
					Intent i = new Intent(UrineChart.this, TableRecord.class);
					i.putExtras(returnBundle);
					startActivity(i);
					finish();
				} else if (v == homeButton) {
					setResult(RESULT_OK);
					finish();
				} else if (v == backButton) {
					finish();
				} else {
					if (v == bpButton) {
						cPath = WebService.PATH_QUERY_BP;
						cToken = Tables.SBP;
					} else if (v == pulseButton) {
						cPath = WebService.PATH_QUERY_PULSE;
						cToken = Tables.PULSE;
					} else if (v == boButton) {
						cPath = WebService.PATH_QUERY_BO;
						cToken = Tables.BO;
					} else if (v == tempButton) {
						cPath = WebService.PATH_QUERY_TEMP;
						cToken = Tables.TEMP;
					} else if (v == gluButton) {
						cPath = WebService.PATH_QUERY_GLU;
						cToken = Tables.GLU;
					} else if (v == uaButton) {
						cPath = WebService.PATH_QUERY_UA;
						cToken = Tables.UA;
					} else if (v == cholButton) {
						cPath = WebService.PATH_QUERY_CHOL;
						cToken = Tables.CHOL;
					} else {
						return;
					}
					Intent i = new Intent(UrineChart.this, CurveRecord.class);
					returnBundle.putString(TableRecord.SELECTED_TOKEN, cToken);
					returnBundle.putString(TableRecord.SELECTED_PATH, cPath);
					i.putExtras(returnBundle);
					startActivity(i);
					finish();
				}
			}
		};
		bpButton.setOnClickListener(listener);
		pulseButton.setOnClickListener(listener);
		boButton.setOnClickListener(listener);
		tempButton.setOnClickListener(listener);
		gluButton.setOnClickListener(listener);
		uaButton.setOnClickListener(listener);
		cholButton.setOnClickListener(listener);
		tableShowButton.setOnClickListener(listener);
		curveShowButton.setOnClickListener(listener);
		homeButton.setOnClickListener(listener);
		backButton.setOnClickListener(listener);
	}

	class PaintTask extends AsyncTask<Void, Void, String> {
		ProgressDialog mProgressDialog;
		String userId = cache.getUserId();
		Context mContext;

		public PaintTask(Context c) {
			mContext = c;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				// Fetch data from network
				Map<String, String> parameters = getParameters(365, userId);
				Map<Long, String> urineRecords = DataService
						.getUrineRecordsForChart(context, parameters, PATH,
								Tables.LEU);

				if (urineRecords != null) {
					computeUrineData(urineRecords);

					// Rebuild the dataset and repaint
					// the
					// chart
					buildDataset(titles, time, urineData);
					mChartView.repaint();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(context, "Cannot get data", Toast.LENGTH_SHORT);
			}
			return "ok";
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(mContext);
			// mProgressDialog.setTitle("正在获取数据");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
		}
	}

	// private class PaintingTask implements Runnable {
	// private final String userId = cache.getUserId();
	//
	// @Override
	// public void run() {
	// try {
	// // Fetch data from network
	// Map<String, String> parameters =
	// getParameters(365, userId);
	// Map<Long, String> urineRecords =
	// DataService.getUrineRecordsForChart(
	// context, parameters, PATH, Tables.LEU);
	// computeUrineData(urineRecords);
	//
	// // Rebuild the dataset and repaint the chart
	// buildDataset(titles, time, urineData);
	// mChartView.repaint();
	// } catch (Exception e) {
	// e.printStackTrace();
	// Toast.makeText(context, "Cannot get data",
	// Toast.LENGTH_SHORT);
	// }
	// }
	//
	// }

	private Map<String, String> getParameters(int daysBeforeToday, String userId) {
		String endTime = TimeHelper.getCurrentTime();
		String startTime = TimeHelper.getBeforeTime(daysBeforeToday);
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(WebService.ENDTIME, endTime);
		parameters.put(WebService.STARTTIME, startTime);
		parameters.put(Tables.CARDNO, userId);
		return parameters;
	}

	private void computeUrineData(Map<Long, String> urineRecords) {
		// Create a hash map to store the mapping
		// relation between item values
		// and mark
		Map<String, Integer> itemValuesMap = new HashMap<String, Integer>();
		for (int i = 0; i < itemValues.length; i++) {
			itemValuesMap.put(itemValues[i], Integer.valueOf(i));
		}

		// Create 11 array list to store data
		Map<String, ArrayList<Integer>> urineDataMap = new HashMap<String, ArrayList<Integer>>();
		for (int i = 0; i < titles.length; i++) {
			urineDataMap.put(titles[i], new ArrayList<Integer>());
		}

		// Convert integrated urine record to 11
		// standalone items data
		Set<Map.Entry<Long, String>> entrySet = urineRecords.entrySet();
		for (Map.Entry<Long, String> entry : entrySet) {
			// get date of each record
			time.add(new Date(entry.getKey()));

			// get 11 item data in each record
			String[] strArray = entry.getValue().split(",");
			Map<String, String> record = new HashMap<String, String>();
			for (int i = 0; i < strArray.length; i++) {
				String[] kv = strArray[i].split(":");
				record.put(kv[0].trim(), kv[1].trim());
			}

			// Convert item value to mark, and store
			// them in corresponding list
			Set<Map.Entry<String, String>> recordEntrySet = record.entrySet();
			for (Map.Entry<String, String> recEntry : recordEntrySet) {
				String item = recEntry.getKey();
				String itemValue = item + ":" + recEntry.getValue();
				String itemItem = item + ":" + item;
				if (urineDataMap.containsKey(item)) {
					if (itemValuesMap.containsKey(itemValue)) {
						urineDataMap.get(item)
								.add(itemValuesMap.get(itemValue));
					} else {
						// add a maximum value if this
						// item value is not in the map
						if (itemValuesMap.containsKey(itemItem)) {
							urineDataMap.get(item).add(
									itemValuesMap.get(itemItem));
						}

					}
				} else {
					// ignore this item
				}
			}
		}
		// assign the data in map to array list
		for (int i = 0; i < titles.length; i++) {
			urineData.add(urineDataMap.get(titles[i]));
		}
	}

}
