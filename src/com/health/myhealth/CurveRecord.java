package com.health.myhealth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.health.database.Tables;
import com.health.util.MyProgressDialog;
import com.health.util.TimeHelper;
import com.health.web.WebService;

public class CurveRecord extends BaseActivity {
	private static final int OK = 0;
	private static final int NET_ERROR = 1;
	private static final int NO_DATA = 2;
	private static final Integer PAGE_COUNT = 10;
	private static final int GRAPH_WIDTH = 800;// 曲线图宽度
	private static final int DAYS = 7;// 一屏显示时间长度
	private static final String TAG = "CurveRecord";
	private double screenNum = 1.0;// 曲线占据的屏幕个数,滑动多少个屏才能看到所有显示的数据
	private Context context;
	private Button bpButton;// 血压
	private Button pulseButton;// 脉率
	private Button boButton;// 血氧
	private Button tempButton;// 体温
	private Button gluButton;// 血糖
	private Button uaButton;// 尿酸
	private Button cholButton;// 总胆固醇
	private Button urineButton;// 尿常规
	private Button tableShowButton;// 表格
	private Button curveShowButton;// 曲线
	private Button homeButton;// 回主界面
	private Button backButton;// 后退
	private LinearLayout curveLayout;// 曲线图

	private Button lastButton = null;// 上次点击点按钮
	private int pageIndex = 0;// 默认为第一页
	private boolean hasMore = false;// 还有更多数据显示
	private String cPath = WebService.PATH_QUERY_BP;// 当前项目的后缀
	private String cToken = Tables.SBP;;// 当前项目
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(2);
	private GraphicalView lineView;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private DataDenerator[] generators = new DataDenerator[8];// 各个项取数据的工具
	private MyProgressDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.curve_record);
		parseBundle();
		context = this;
		initView();
		setButtonListener();
		// 当前选中曲线展示
		tableShowButton.setSelected(false);
		curveShowButton.setSelected(true);
		setSelected(cToken);
		mDialog = new MyProgressDialog(context);
		curveLayout.addView(lineView());
		DataDenerator generator = getGenerator(cToken);
		if (null != generator)
			exec.execute(generator);
		setLineViewListener();
	}

	/***
	 * 设置初始时的选中button
	 * 
	 * @param token
	 */
	private void setSelected(String token) {
		Button cButton;
		if (Tables.SBP.equals(token))
			cButton = bpButton;
		else if (Tables.PULSE.equals(token))
			cButton = pulseButton;
		else if (Tables.SBP.equals(token))
			cButton = bpButton;
		else if (Tables.BO.equals(token))
			cButton = boButton;
		else if (Tables.TEMP.equals(token))
			cButton = tempButton;
		else if (Tables.GLU.equals(token))
			cButton = gluButton;
		else if (Tables.UA.equals(token))
			cButton = uaButton;
		else if (Tables.CHOL.equals(token))
			cButton = cholButton;
		else
			cButton = urineButton;
		setSelected(cButton);
	}

	private DataDenerator getGenerator(String token) {
		if (token.equals(Tables.SBP)) {
			if (generators[0] == null)
				generators[0] = new DataDenerator("血压", "血压折线图", "血压值（mmHg)",
						40, 200);
			return generators[0];
		} else if (token.equals(Tables.PULSE)) {
			if (generators[1] == null)
				generators[1] = new DataDenerator("脉率", "脉率折线图", "脉率（次/分钟）",
						30, 140);
			return generators[1];
		} else if (token.equals(Tables.BO)) {
			if (generators[2] == null)
				generators[2] = new DataDenerator("血氧", "血氧折线图", "血氧含量（%）", 20,
						101);
			return generators[2];
		} else if (token.equals(Tables.TEMP)) {
			if (generators[3] == null)
				generators[3] = new DataDenerator("体温", "体温折线图", "体温（摄氏度）", 28,
						43);
			return generators[3];
		} else if (token.equals(Tables.GLU)) {
			if (generators[4] == null)
				generators[4] = new DataDenerator("血糖", "血糖折线图", "血糖（mg/dL）",
						2, 600);
			return generators[4];
		} else if (token.equals(Tables.UA)) {
			if (generators[5] == null)
				generators[5] = new DataDenerator("尿酸", "尿酸折线图", "尿酸（mg/dL）",
						3, 150);
			return generators[5];
		} else if (token.equals(Tables.CHOL)) {
			if (generators[6] == null)
				generators[6] = new DataDenerator("总胆固醇", "总胆固醇折线图",
						"总胆固醇（mg/dL）", 100, 400);
			return generators[6];
		}
		return generators[0];

	}

	/***
	 * 解析bundle的数据
	 * 
	 * @param savedInstanceState
	 */
	private void parseBundle() {
		Bundle state = getIntent().getExtras();
		if (state != null) {
			String path = state.getString(TableRecord.SELECTED_PATH);
			if (null != path)
				cPath = path;
			String token = state.getString(TableRecord.SELECTED_TOKEN);
			if (null != token)
				cToken = token;
		}
	}

	/***
	 * 设置监听器
	 */
	private void setButtonListener() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == curveShowButton) {

				} else if (v == tableShowButton) {
					jump2table();
				} else if (v == homeButton) {
					CurveRecord.this.setResult(RESULT_OK);
					CurveRecord.this.finish();
				} else if (v == backButton) {
					CurveRecord.this.finish();
				} else {
					String path = WebService.PATH_QUERY_BP;
					String token = Tables.SBP;
					if (v == bpButton) {
						path = WebService.PATH_QUERY_BP;
						token = Tables.SBP;// 代表舒张压和收缩压
					} else if (v == pulseButton) {
						path = WebService.PATH_QUERY_PULSE;
						token = Tables.PULSE;
					} else if (v == boButton) {
						path = WebService.PATH_QUERY_BO;
						token = Tables.BO;
					} else if (v == tempButton) {
						path = WebService.PATH_QUERY_TEMP;
						token = Tables.TEMP;
					} else if (v == gluButton) {
						path = WebService.PATH_QUERY_GLU;
						token = Tables.GLU;
					} else if (v == uaButton) {
						path = WebService.PATH_QUERY_UA;
						token = Tables.UA;
					} else if (v == cholButton) {
						path = WebService.PATH_QUERY_CHOL;
						token = Tables.CHOL;
					} else if (v == urineButton) {
						path = WebService.PATH_QUERY_URINE;
						token = Tables.LEU;// 代表尿常规11项
						Intent intent = new Intent(CurveRecord.this,
								UrineChart.class);
						startActivity(intent);
						CurveRecord.this.finish();
					}
					// 按了新的按鈕
					if (!cToken.equals(token)) {
						cToken = token;
						cPath = path;
						pageIndex = 0;
						moved = 0;// 滑动屏数清零
						setSelected((Button) v);
						// 点击的按钮不同,将之前的数据清空
						clearDataset();
						setXAxis(mRenderer);
						DataDenerator generator = getGenerator(cToken);
						if (null != generator)
							exec.execute(generator);
					}

				}
			}

			/***
			 * 跳转到表格显示
			 */
			private void jump2table() {
				Intent intent = new Intent(CurveRecord.this, TableRecord.class);
				Bundle bundle = new Bundle();
				bundle.putString(TableRecord.SELECTED_TOKEN, cToken);
				bundle.putString(TableRecord.SELECTED_PATH, cPath);
				intent.putExtras(bundle);
				startActivity(intent);
				CurveRecord.this.finish();
			}
		};
		bpButton.setOnClickListener(listener);
		pulseButton.setOnClickListener(listener);
		boButton.setOnClickListener(listener);
		tempButton.setOnClickListener(listener);
		gluButton.setOnClickListener(listener);
		uaButton.setOnClickListener(listener);
		cholButton.setOnClickListener(listener);
		urineButton.setOnClickListener(listener);
		tableShowButton.setOnClickListener(listener);
		curveShowButton.setOnClickListener(listener);
		homeButton.setOnClickListener(listener);
		backButton.setOnClickListener(listener);
	}

	private class DataDenerator implements Runnable {

		private String seriesTitle;
		private String charTitle;
		private String yTitle;
		private int yMin;
		private int yMax;

		public DataDenerator(String seriesTitle, String charTitle,
				String yTitle, int yMin, int yMax) {
			this.seriesTitle = seriesTitle;
			this.charTitle = charTitle;
			this.yTitle = yTitle;
			this.yMin = yMin;
			this.yMax = yMax;
		}

		@Override
		public void run() {
			try {
				showCurveLine(seriesTitle, charTitle, yTitle, yMin, yMax);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace(); 
			}
		}

	}

	/***
	 * 将数据集清空
	 */
	private void clearDataset() {
		for (XYSeries series : mDataset.getSeries()) {
			series.clear();
		}
	}

	private double moved = 0;

	private void setLineViewListener() {
		lineView.setOnTouchListener(new OnTouchListener() {
			long begin = 0;
			long end = 0;
			String TAG = "setOnTouchListener";
			Toast toast = new Toast(getApplicationContext());

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				SeriesSelection seriesSelection = lineView
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
					String value = String.valueOf((float) seriesSelection
							.getValue());
					valueTV.setText("测量值:" + value);
					timeTV.setText("测量时间:" + time);
					toast.setView(view);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.TOP | Gravity.LEFT,
							(int) (event.getX()), (int) (event.getY()));
					toast.show();

				}

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					begin = (int) event.getX();
					break;
				case MotionEvent.ACTION_MOVE:
					end = (int) event.getX();
					break;
				case MotionEvent.ACTION_UP:
					moved += end - begin;
					Log.i(TAG, " moved: " + moved / GRAPH_WIDTH + " screenNum:"
							+ screenNum);
					// 移动的位置超出了显示的范围
					// 没有要显示的数据,不需要动态加载
					if (hasMore && moved / GRAPH_WIDTH >= screenNum - 1) {
						pageIndex++;
						DataDenerator generator = getGenerator(cToken);
						if (null != generator)
							exec.execute(generator);
					}
				}
				return false;
			}
		});
	}

	/***
	 * 显示折线图
	 * 
	 * @param seriesTitle
	 * @param charTitle
	 * @param yTitle
	 * @param yMin
	 * @param yMax
	 * @throws IOException
	 * @throws JSONException
	 */
	public void showCurveLine(String seriesTitle, String charTitle,
			String yTitle, int yMin, int yMax) throws IOException,
			JSONException {
		Map<String, String> paras = getQueueParas(pageIndex);
		TreeMap<Double, double[]> data = DataService.getSingleDataForCurve(
				paras, cPath, cToken);
		XYSeries[] xySeries = mDataset.getSeries();
		mRenderer.setChartTitle(charTitle);
		mRenderer.setYTitle(yTitle);
		mRenderer.setYAxisMin(yMin);// 设置y轴最小值
		mRenderer.setYAxisMax(yMax);
		setHasMore(data.size());
		if (cToken.equals(Tables.SBP))
			addBpData(xySeries, data);
		else
			addSinlgeData(xySeries, seriesTitle, data);
		if (data.size() > 0)
			setXAxis(mRenderer, data.lastKey());
		if (xySeries[0].getItemCount()==0)// 没有数据时，添加一个假数据，保证正常显示
			xySeries[0].add((double) System.currentTimeMillis(), -100);
		else
			updateCurveLength(xySeries[0]);
		lineView.repaint();// 重画
	}

	private void setXAxis(XYMultipleSeriesRenderer mRenderer, Double lastKey) {
		mRenderer.setXAxisMin(lastKey - TimeHelper.MILLSEC_DAY * (DAYS - 1));
		mRenderer.setXAxisMax(lastKey + DAYS);

	}

	/***
	 * 添加单个数据
	 * 
	 * @param xySeries
	 * @param seriesTitle
	 * @param data
	 */
	private void addSinlgeData(XYSeries[] xySeries, String seriesTitle,
			TreeMap<Double, double[]> data) {
		xySeries[0].setTitle(seriesTitle);
		xySeries[1].setTitle("");
		double[][] lastData = getPoints(xySeries[0]);

		clearDataset();

		for (int i = 0; i < lastData[0].length; i++) {
			data.put(lastData[0][i], new double[] { lastData[1][i] });
		}
		for (Map.Entry<Double, double[]> entry : data.entrySet()) {
			double key = entry.getKey();
			double[] value = entry.getValue();
			xySeries[0].add(key, value[0]);
		}
	}

	/***
	 * 添加血压数据
	 * 
	 * @param xySeries
	 * @param data
	 */
	private void addBpData(XYSeries[] xySeries, TreeMap<Double, double[]> data) {
		xySeries[0].setTitle("高压");
		xySeries[1].setTitle("低压");
		// 先暂存之前加载的数据
		double[][] lastHigh = getPoints(xySeries[0]);
		double[][] lastLow = getPoints(xySeries[1]);
		clearDataset();
		for (int i = 0; i < lastHigh[0].length; i++) {
			data.put(lastHigh[0][i], new double[] { lastHigh[1][i],
					lastLow[1][i] });
		}
		for (Map.Entry<Double, double[]> entry : data.entrySet()) {
			double key = entry.getKey();
			double[] value = entry.getValue();
			xySeries[0].add(key, value[0]);
			xySeries[1].add(key, value[1]);
		}
	}

	/***
	 * 获取显示的点数据
	 * 
	 * @param xySeries
	 * @return
	 */
	private double[][] getPoints(XYSeries xySeries) {
		int num = xySeries.getItemCount();
		double[][] data = new double[2][num];
		for (int i = 0; i < num; i++) {
			data[0][i] = xySeries.getX(i);
			data[1][i] = xySeries.getY(i);
		}
		return data;
	}

	/***
	 * 设置是否还有下一页
	 * 
	 * @param dataNum
	 */
	private void setHasMore(int dataNum) {
		// 还有下一页数据
		if (dataNum == PAGE_COUNT)
			hasMore = true;
		else
			// 收到的数据个数不等于页面大小，说明已经没有可加载的数据
			hasMore = false;
	}

	/***
	 * 更新曲线长度，计算为第一个点到最后一个点的所占的屏数
	 * 
	 * @param start
	 * @param end
	 */
	private void updateCurveLength(XYSeries xySeries) {
		int count = xySeries.getItemCount();
		if (count == 0)
			return;
		double start = xySeries.getX(0);
		double end = xySeries.getX(count - 1);
		String t1 = TimeHelper.getStringTime((long) start);
		String t2 = TimeHelper.getStringTime((long) end);
		Log.i(TAG, "count:" + count + "start:" + t1 + " end:" + t2);
		screenNum = ((end - start) / (TimeHelper.MILLSEC_DAY * DAYS));
	}

	/***
	 * 获取查询数据的参数，这里所有的查询参数都是相同的，只有查询路径不同
	 * 
	 * @param pageIndex
	 * @return
	 */
	private Map<String, String> getQueueParas(Integer pageIndex) {
		Map<String, String> paras = new HashMap<String, String>();
		String startTime = TimeHelper.getBeforeTime(365);
		String endTime = TimeHelper.getCurrentTime();
		paras.put(WebService.ENDTIME, endTime);
		paras.put(WebService.STARTTIME, startTime);
		paras.put(Tables.CARDNO, cache.getUserId());
		paras.put(WebService.PAGECOUNT, PAGE_COUNT.toString());
		paras.put(WebService.PAGEINDEX, pageIndex.toString());
		return paras;
	}

	/**
	 * 画血氧波形图
	 * 
	 * @return
	 */
	public View lineView() {
		XYSeriesRenderer xRenderer = new XYSeriesRenderer();// (类似于一条线对象)
		XYSeriesRenderer yRenderer = new XYSeriesRenderer();// (类似于一条线对象)
		TimeSeries ySeries = new TimeSeries("");
		TimeSeries xSeries = new TimeSeries("");
		mDataset.addSeries(xSeries);
		mDataset.addSeries(ySeries);
		// 设置图表的X轴的当前方向
		mRenderer.setOrientation(Orientation.HORIZONTAL);
		mRenderer.setAxisTitleTextSize(15);// 设置轴标题文本大小
		mRenderer.setChartTitleTextSize(15);// 设置图表标题文字的大小
		// mRenderer.setLabelsTextSize(18);// 设置标签的文字大小
		mRenderer.setYLabels(10);// y轴显示的点数
		mRenderer.setXLabels(10);// x轴显示的点数
		mRenderer.setYLabelsColor(1, Color.BLACK);
		mRenderer.setLegendTextSize(20);// 设置图例文本大小
		mRenderer.setPointSize(5f);// 设置点的大小
		mRenderer.setShowGrid(true);// 显示网格
		mRenderer.setShowLabels(true);
		mRenderer.setShowLegend(true);
		mRenderer.setMargins(new int[] { 1, 15, 1, 1 });// 设置视图位置
		mRenderer.setMarginsColor(Color.WHITE);// 背景设置为透明
		mRenderer.setPanEnabled(true, false);// 设置不能上下移动曲线
		mRenderer.setSelectableBuffer(10);
		setXAxis(mRenderer);
		xRenderer.setColor(Color.BLUE);// 设置颜色
		xRenderer.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		xRenderer.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		xRenderer.setLineWidth(2);// 设置线宽
		mRenderer.addSeriesRenderer(xRenderer);
		yRenderer.setColor(Color.GREEN);// 设置颜色
		yRenderer.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		yRenderer.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		yRenderer.setLineWidth(2);// 设置线宽
		mRenderer.addSeriesRenderer(yRenderer);
		lineView = ChartFactory.getTimeChartView(this, mDataset, mRenderer,
				"yy/MM/dd HH:mm");
		lineView.setBackgroundColor(Color.WHITE);
		return lineView;
	}

	/***
	 * 设置当前显示范围为最近10天
	 * 
	 * @param mRenderer
	 */
	private void setXAxis(XYMultipleSeriesRenderer mRenderer) {
		long current = TimeHelper.getCurrentDate().getTime();
		mRenderer.setXAxisMin(current - TimeHelper.MILLSEC_DAY * DAYS);
		mRenderer.setXAxisMax(current);
	}

	/**
	 * 设置选中按钮,取消上次按钮的选择
	 * 
	 * @param button
	 */
	private void setSelected(Button button) {
		if (lastButton != null)
			lastButton.setSelected(false);
		lastButton = button;
		lastButton.setSelected(true);
	}

	/***
	 * 初始化控件
	 */
	private void initView() {
		bpButton = (Button) this.findViewById(R.id.show_bp);
		pulseButton = (Button) this.findViewById(R.id.show_pulse);
		boButton = (Button) this.findViewById(R.id.show_bo);
		tempButton = (Button) this.findViewById(R.id.show_temp);
		gluButton = (Button) this.findViewById(R.id.show_glu);
		uaButton = (Button) this.findViewById(R.id.show_ua);
		cholButton = (Button) this.findViewById(R.id.show_chol);
		urineButton = (Button) this.findViewById(R.id.show_urine);
		tableShowButton = (Button) this.findViewById(R.id.table_show);
		curveShowButton = (Button) this.findViewById(R.id.curve_show);
		homeButton = (Button) this.findViewById(R.id.to_home_button);
		backButton = (Button) this.findViewById(R.id.return_button);
		curveLayout = (LinearLayout) this.findViewById(R.id.show_curve_layout);
	}
}
