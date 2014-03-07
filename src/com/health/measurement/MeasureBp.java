package com.health.measurement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.bluetooth.BluetoothListActivity;
import com.health.bluetooth.BluetoothService;
import com.health.database.Cache;
import com.health.database.DatabaseService;
import com.health.database.Tables;
import com.health.device.HealthDevice;
import com.health.device.PC300;
import com.health.util.TimeHelper;
import com.health.web.Uploader;
import com.health.web.WebService;

/**
 * 测量血压
 * 
 */
public class MeasureBp extends BaseActivity {

	private static EditText HighBpEditText = null;// 高压文本框
	private static EditText lowBpEditText = null;// 低压文本框
	private static EditText pulseEditText = null;// 脉率
	private static EditText boEditText = null;// 血氧
	private static EditText tempEditText = null;// 体温
	private static Button measureButton = null;// 测试测量血压按钮
	private static Button stableBoButton = null;// 测试测量血氧按钮
	private static Button stableTempButton = null;// 测试测量体温按钮
	private static Button homeButton;// 返回主界面按钮
	private static Button returnButton;// 返回上一步按钮
	private static Button uploadButton;// 上次数据按钮
	private static boolean boStable = false;// 血氧恒定标记
	private static boolean tempStable = false;// 体温恒定标记
	private static Button findButton = null;// 查找设备按钮
	private static TextView statusView = null;// 蓝牙连接状态
	private static ImageView hBpImageView = null;// 收缩压前面图标
	private static ImageView lBpImageView = null;// 舒张压前面图标
	private static ImageView pulseImageView = null;// 脉率前面图标
	private static ImageView boImageView = null;// 血氧前面图标
	private static ImageView tempImageView = null;// 体温前面图标

	private static final boolean DEBUG = true;
	private static final String TAG = "MeasureBp";
	private static BluetoothService bluetoothService = null;
	private static PC300Handler handler = null;

	private static String btName = "PC_300SNT";// 蓝牙名称
	private static String btMac = null;// 蓝牙名称
	private static Context context;
	private static boolean stop = false;
	private LinearLayout graphLayout;// 装血氧图的布局
	private static GraphicalView boWaveView;// 血氧图
	static XYSeries xSeries = new XYSeries("血氧");
	XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private static float boWaveIndex = 0;

	private static DatabaseService dbService;// 数据库服务

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.measure_bp);
		context = this;
		dbService = new DatabaseService(context);
		cache = new Cache(context);
		findId();
		setVisibility();
		if (handler == null)
			handler = new PC300Handler();
		bluetoothService = BluetoothService.getService(handler, true);// 异步方式

		graphLayout = (LinearLayout) this.findViewById(R.id.bo_image_view);
		graphLayout.addView(lineView());
		connectPC300();
		setOnClickListener();
		setConnectState();
	}

	/**
	 * 设置监听器
	 */
	private void setOnClickListener() {
		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == measureButton)
					takeMeasureBp();// 开始测量
				else if (v == findButton)
					startDeviceListActivity();// 开启查找蓝牙activity
				else if (v == stableBoButton) {
					boStable = !boStable;// 血氧恒定
					if (boStable)
						stableBoButton.setText("取消血氧锁定");
					else
						stableBoButton.setText("锁定血氧");
				} else if (v == stableTempButton) {
					tempStable = !tempStable;// 开启查找蓝牙activity
					if (tempStable)
						stableTempButton.setText("取消体温锁定");
					else
						stableTempButton.setText("锁定体温");
				} else if (v == homeButton) {
					MeasureBp.this.setResult(RESULT_OK);
					MeasureBp.this.finish();
				} else if (v == returnButton) {
					MeasureBp.this.finish();
				} else if (v == uploadButton) {
					uploadButton.setEnabled(false);
					uploadButton.setClickable(false);
					Toast.makeText(context, "后台开始上传", Toast.LENGTH_SHORT)
							.show();
					upload();// 上传数据
				}
			}

		};
		measureButton.setOnClickListener(onClickListener);
		stableBoButton.setOnClickListener(onClickListener);
		stableTempButton.setOnClickListener(onClickListener);
		homeButton.setOnClickListener(onClickListener);
		returnButton.setOnClickListener(onClickListener);
		uploadButton.setOnClickListener(onClickListener);
		findButton.setOnClickListener(onClickListener);
		uploadButton.setClickable(false);// 初始化时不可上传数据
	}

	private static void setImageView(ImageView imageview, int color) {
		if (color == Uploader.FAILURE || color == Uploader.NET_ERROR)
			imageview.setImageResource(R.drawable.light_red);
		else if (color == Uploader.OK)
			imageview.setImageResource(R.drawable.light_greed);
		else
			imageview.setImageResource(R.drawable.light_blue);
	}

	/***
	 * 获取几个测量项目都有的几个属性
	 * 
	 * @return
	 */
	private Map<String, String> getDefaltAttrs() {
		String time = TimeHelper.getCurrentTime();
		String idCard = cache.getUserId();
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put(Tables.TIME, time);
		dataMap.put(Tables.DEVICENAME, btName);
		dataMap.put(Tables.DEVICEMAC, btMac);
		dataMap.put(Tables.CARDNO, idCard);
		dataMap.put(WebService.STATUS, WebService.UNUPLOAD);// 状态为未上传
		return dataMap;
	}

	protected void upload() {
		Tables table = new Tables();
		ExecutorService exec = Executors.newSingleThreadExecutor();// 单线程池
		String dbp = lowBpEditText.getText().toString();// 舒张压
		if (dbp.length() > 0) {// 舒张压有数据，可以上传血压值
			String sbp = HighBpEditText.getText().toString();// 收缩压
			String pulse = pulseEditText.getText().toString();// 脉率
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.SBP, sbp);
			dataMap.put(Tables.DBP, dbp);
			dataMap.put(Tables.PULSE, pulse);
			Uploader uploader = new Uploader(dataMap, Cache.BP,
					WebService.PATH_BP, cache, dbService, handler,
					table.bpTable());
			exec.execute(uploader);
		}
		String temp = tempEditText.getText().toString();
		if (temp.length() > 0) {// 体温有数据
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.TEMP, temp);
			Uploader uploader = new Uploader(dataMap, Cache.TEMP,
					WebService.PATH_TEMP, cache, dbService, handler,
					table.tempTable());
			exec.execute(uploader);
		}
		String bo = boEditText.getText().toString();
		if (bo.length() > 0) {// 血氧有数据
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.BO, bo);
			Uploader uploader = new Uploader(dataMap, Cache.BO,
					WebService.PATH_BO, cache, dbService, handler,
					table.boTable());
			exec.execute(uploader);
		}
	}

	/**
	 * 设置几个button的显示与隐藏
	 * 
	 * @param status
	 */
	private static void setVisibility() {
		int status;
		if (bluetoothService != null
				&& bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
			status = View.VISIBLE;// 连接时设置可见
		} else {
			status = View.INVISIBLE;// 未连接时设置不可见
		}
		measureButton.setVisibility(status);
		stableBoButton.setVisibility(status);
		stableTempButton.setVisibility(status);
	}

	/**
	 * 初始化id
	 */
	private void findId() {
		HighBpEditText = (EditText) findViewById(R.id.hp);
		lowBpEditText = (EditText) findViewById(R.id.lp);
		pulseEditText = (EditText) findViewById(R.id.mb);
		boEditText = (EditText) findViewById(R.id.bo);
		tempEditText = (EditText) findViewById(R.id.temp);
		measureButton = (Button) findViewById(R.id.getdata);
		stableBoButton = (Button) findViewById(R.id.bo_stable_button);
		stableTempButton = (Button) findViewById(R.id.temp_stable_button);
		findButton = (Button) findViewById(R.id.find_device);
		homeButton = (Button) findViewById(R.id.to_home_button);
		returnButton = (Button) findViewById(R.id.return_button);
		uploadButton = (Button) findViewById(R.id.upload_button);
		statusView = (TextView) findViewById(R.id.connect_status);
		hBpImageView = (ImageView) findViewById(R.id.hp_image);
		lBpImageView = (ImageView) findViewById(R.id.lp_image);
		pulseImageView = (ImageView) findViewById(R.id.pulse_image);
		boImageView = (ImageView) findViewById(R.id.bo_image);
		tempImageView = (ImageView) findViewById(R.id.temp_image);
	}

	/**
	 * 设置连接状态的显示
	 */
	private void setConnectState() {
		if (bluetoothService == null) {
			statusView.setText(R.string.unconnect);
			return;
		}
		switch (bluetoothService.getState()) {
		case BluetoothService.STATE_CONNECTING:
			statusView.setText(R.string.connecting);
			break;
		case BluetoothService.STATE_CONNECTED:
			statusView.setText(btName);
			break;
		case BluetoothService.STATE_NONE:
			statusView.setText(R.string.unconnect);
			break;
		}
	}

	/**
	 * 进行测量、或者停止测量
	 */
	private void takeMeasureBp() {
		if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {			
			if (stop == false) {
				bluetoothService.write(PC300.COMMAND_BP_START);				
				initDataTextEdit();
				measureButton.setText("停止血压测量");
			} else {
				bluetoothService.write(PC300.COMMAND_BP_STOP);
				measureButton.setText("开始血压测量");
			}
			stop = !stop;
		}
	}

	/*
	 * 初始化测量显示数据
	 */
	protected void initDataTextEdit() {
		HighBpEditText.setText("");
		lowBpEditText.setText("");
		pulseEditText.setText("");
	}

	/**
	 * 连接到PC300设备
	 */
	private void connectPC300() {
		if (bluetoothService.getState() == BluetoothService.STATE_NONE) {// 空闲状态才连接

			String address = cache.getDeviceAddress(Cache.PC300);
			btMac = address;
			BluetoothDevice device = bluetoothService
					.getBondedDeviceByAddress(address);
			if (device != null) {
				bluetoothService.connect(device);
				HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
						bluetoothService, PC300.COMMAND_BETTERY, 3000);
				persistWriter.start();// 持续握手
			} else {
				Toast.makeText(context, "无已经绑定的PC300设备，试试查找设备...",
						Toast.LENGTH_LONG).show();// 没有配对过得设备，启动查找
			}
		}
	}

	/**
	 * 连接搜索到的PC300设备
	 * 
	 * @param address
	 */
	private void connectPC300(String address) {
		btMac = address;
		BluetoothDevice device = bluetoothService
				.getRemoteDeviceByAddress(address);
		bluetoothService.connect(device);
		HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
				bluetoothService, PC300.COMMAND_BETTERY, 3000);
		persistWriter.start();// 持续握手
	}

	/**
	 * 开启查找蓝牙设备的activity
	 */
	private void startDeviceListActivity() {
		Intent serverIntent = new Intent(this, BluetoothListActivity.class);
		startActivityForResult(serverIntent,
				BluetoothListActivity.REQUEST_CONNECT_DEVICE);
	}

	/**
	 * 查找蓝牙后，用户指定连接设备，返回进行连接
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case BluetoothListActivity.REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						BluetoothListActivity.EXTRA_DEVICE_ADDRESS);
				connectPC300(address);
			}
			break;
		}
	}

	private static class PC300Handler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					statusView.setText(btName);
					bluetoothService.write(PC300.COMMAND_TEMP_START);
					uploadButton.setEnabled(true);
					uploadButton.setClickable(true);// 设置可以点击
					Toast.makeText(context, "已连接到$$" + btName,
							Toast.LENGTH_LONG).show();
					setVisibility();// 设置测量按钮可见
					break;
				case BluetoothService.STATE_CONNECTING:
					statusView.setText(R.string.connecting);
					break;
				case BluetoothService.STATE_NONE:
					statusView.setText(R.string.unconnect);
					setVisibility();
					break;
				}
				break;

			case BluetoothService.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				new String(writeBuf);
				// Toast.makeText(context,
				// writeMessage,
				// Toast.LENGTH_LONG).show();
				break;
			case BluetoothService.MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;
				Log.i(TAG, Arrays.toString(readBuf));
				processReadData(readBuf);
				if (DEBUG) {
					// Log.d(TAG, readMessage);
					Log.d(TAG, Arrays.toString(readBuf));
				}
				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(context,
						msg.getData().getString(BluetoothService.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.MESSAGE_DEVICE:
				btName = msg.getData().getString(BluetoothService.DEVICE_NAME);
				String address = msg.getData().getString(
						BluetoothService.DEVICE_ADDRESS);
				cache.saveDeviceAddress(Cache.PC300, address);// 保存地址,以便下次自带连接
				break;
			case Uploader.MESSAGE_UPLOADE_RESULT:
				Bundle bundler = msg.getData();
				String item = bundler.getString(Cache.ITEM);
				int status = bundler.getInt(Uploader.STUTAS);
				if (Cache.BP.equals(item)) {
					setImageView(hBpImageView, status);
					setImageView(lBpImageView, status);
					setImageView(pulseImageView, status);
				}
				if (Cache.BO.equals(item))
					setImageView(boImageView, status);
				if (Cache.TEMP.equals(item))
					setImageView(tempImageView, status);
				break;
			}

		}
	};

	/**
	 * 处理读入的数据,读入的数据可能包含多条协议数据，根据协议的包头将读入的数据分割，
	 * 对于同一类型的数据只保存最新的且校验正确的一条,最后将每一类型的数据响应到界面
	 * 
	 * @param buffer
	 * 
	 */
	public static void processReadData(byte[] buffer) {
		PC300 pc300 = new PC300();
		SparseArray<List<byte[]>> map = pc300
				.getLegalPatternsFromBuffer(buffer);
		int dataSize = map.size();
		for (int i = 0; i < dataSize; i++) {
			int token = map.keyAt(i);// 获取token
			List<byte[]> datas = map.get(token);
			switch (token) {
			case PC300.TOKEN_BP_CURRENT:
				Integer currentBp = pc300
						.getCurrentBp(datas.get(datas.size() - 1));// 获取当前血压值
				HighBpEditText.setText(currentBp.toString());
				break;
			case PC300.TOKEN_BP_RESULT:
				int[] bpResult = pc300.getResultBp(datas.get(datas.size() - 1));
				processBpResult(bpResult);
				break;
			case PC300.TOKEN_BO_WAVE:
				for (byte[] data : datas) {
					Log.i("TOKEN_BO_WAVE", Arrays.toString(data));
					int[] value = pc300.getBoWave(data);
					if (null != value)
						updateWaveImage(value);
				}
				break;
			case PC300.TOKEN_BO_PAKAGE:
				Integer spO2 = pc300.getSpO2(datas.get(datas.size() - 1));
				if (!boStable)
					boEditText.setText(spO2.toString());
				break;

			case PC300.TOKEN_TEMP:
				Float temp = pc300.getTemp(datas.get(datas.size() - 1));
				if (!tempStable)
					tempEditText.setText(temp.toString());
				break;
			}
		}
	}

	/**
	 * 更新血氧图
	 * 
	 * @param data
	 */
	private static void updateWaveImage(int[] data) {
		// Log.i(TAG + ".updateWaveImage",
		// Arrays.toString(data));
		for (int each : data) {
			boWaveIndex += 1;
			if (boWaveIndex > 120) {
				boWaveIndex -= 120;
			}
			xSeries.add(boWaveIndex, each);
		}
		// mRenderer.setXAxisMin(boWaveIndex - 55);
		// mRenderer.setXAxisMax(boWaveIndex + 5);
		if (boWaveView != null)
			boWaveView.repaint();
	}

	/**
	 * 处理血压测量结果的显示，分正常和异常两种情况
	 * 
	 * @param bpResult
	 */
	private static void processBpResult(int[] bpResult) {
		if (bpResult[0] == PC300.ERROR_RESULT) {// 测量结果有误
			Log.i(TAG, "ERROR_RESULT:" + bpResult[1]);
			String text = new String();
			switch (bpResult[1]) {
			case PC300.ILLEGAL_PULSE:
				text = "测量不到有效的脉搏";
				break;
			case PC300.BAD_BOUND:
				text = "气袋没有绑好";
				break;
			case PC300.ERROR_VALUE:
				text = "结果数值有误";
				break;
			default:
				text = "未知错误";
			}
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		} else {
			String pulseTag = bpResult[0] == 1 ? "心率不齐" : "心率正常";
			Toast.makeText(context, pulseTag, Toast.LENGTH_LONG).show();
			HighBpEditText.setText(Integer.valueOf(bpResult[1]).toString());
			lowBpEditText.setText(Integer.valueOf(bpResult[2]).toString());
			pulseEditText.setText(Integer.valueOf(bpResult[3]).toString());
		}
		measureButton.setText("重新测量");// 测量完毕后，又可以测量
		stop = false;
	}

	/**
	 * 画血氧波形图
	 * 
	 * @return
	 */
	public View lineView() {
		XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
		XYSeriesRenderer xRenderer = new XYSeriesRenderer();// (类似于一条线对象)

		mDataset.addSeries(xSeries);
		// 设置图表的X轴的当前方向
		mRenderer
				.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setYTitle("血氧值");// 设置y轴的标题
		mRenderer.setAxisTitleTextSize(15);// 设置轴标题文本大小
		mRenderer.setChartTitle("血氧波形图");// 设置图表标题
		mRenderer.setChartTitleTextSize(15);// 设置图表标题文字的大小
		// mRenderer.setLabelsTextSize(18);// 设置标签的文字大小
		mRenderer.setXLabels(0);// 不显示x轴
		mRenderer.setLegendTextSize(20);// 设置图例文本大小
		mRenderer.setPointSize(1f);// 设置点的大小
		mRenderer.setYAxisMin(0);// 设置y轴最小值是0
		mRenderer.setYAxisMax(128);
		mRenderer.setXAxisMax(120);
		mRenderer.setShowGrid(true);// 显示网格
		mRenderer.setMargins(new int[] { 1, 15, 1, 1 });// 设置视图位置

		xRenderer.setColor(Color.BLUE);// 设置颜色
		xRenderer.setPointStyle(PointStyle.CIRCLE);// 设置点的样式
		xRenderer.setFillPoints(true);// 填充点（显示的点是空心还是实心）
		xRenderer.setLineWidth(2);// 设置线宽
		mRenderer.addSeriesRenderer(xRenderer);
		mRenderer.setMarginsColor(Color.WHITE);// 背景设置为白色
		mRenderer.setPanEnabled(true, false);// 设置不能移动曲线
		boWaveView = ChartFactory.getCubeLineChartView(this, mDataset,
				mRenderer, 0.6f);
		boWaveView.setBackgroundColor(Color.WHITE);
		return boWaveView;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// bluetoothService.stop();//
			// 退出activity后关闭蓝牙连接
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
