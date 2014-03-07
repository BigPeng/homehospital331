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
 * ����Ѫѹ
 * 
 */
public class MeasureBp extends BaseActivity {

	private static EditText HighBpEditText = null;// ��ѹ�ı���
	private static EditText lowBpEditText = null;// ��ѹ�ı���
	private static EditText pulseEditText = null;// ����
	private static EditText boEditText = null;// Ѫ��
	private static EditText tempEditText = null;// ����
	private static Button measureButton = null;// ���Բ���Ѫѹ��ť
	private static Button stableBoButton = null;// ���Բ���Ѫ����ť
	private static Button stableTempButton = null;// ���Բ������°�ť
	private static Button homeButton;// ���������水ť
	private static Button returnButton;// ������һ����ť
	private static Button uploadButton;// �ϴ����ݰ�ť
	private static boolean boStable = false;// Ѫ���㶨���
	private static boolean tempStable = false;// ���º㶨���
	private static Button findButton = null;// �����豸��ť
	private static TextView statusView = null;// ��������״̬
	private static ImageView hBpImageView = null;// ����ѹǰ��ͼ��
	private static ImageView lBpImageView = null;// ����ѹǰ��ͼ��
	private static ImageView pulseImageView = null;// ����ǰ��ͼ��
	private static ImageView boImageView = null;// Ѫ��ǰ��ͼ��
	private static ImageView tempImageView = null;// ����ǰ��ͼ��

	private static final boolean DEBUG = true;
	private static final String TAG = "MeasureBp";
	private static BluetoothService bluetoothService = null;
	private static PC300Handler handler = null;

	private static String btName = "PC_300SNT";// ��������
	private static String btMac = null;// ��������
	private static Context context;
	private static boolean stop = false;
	private LinearLayout graphLayout;// װѪ��ͼ�Ĳ���
	private static GraphicalView boWaveView;// Ѫ��ͼ
	static XYSeries xSeries = new XYSeries("Ѫ��");
	XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private static float boWaveIndex = 0;

	private static DatabaseService dbService;// ���ݿ����

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
		bluetoothService = BluetoothService.getService(handler, true);// �첽��ʽ

		graphLayout = (LinearLayout) this.findViewById(R.id.bo_image_view);
		graphLayout.addView(lineView());
		connectPC300();
		setOnClickListener();
		setConnectState();
	}

	/**
	 * ���ü�����
	 */
	private void setOnClickListener() {
		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == measureButton)
					takeMeasureBp();// ��ʼ����
				else if (v == findButton)
					startDeviceListActivity();// ������������activity
				else if (v == stableBoButton) {
					boStable = !boStable;// Ѫ���㶨
					if (boStable)
						stableBoButton.setText("ȡ��Ѫ������");
					else
						stableBoButton.setText("����Ѫ��");
				} else if (v == stableTempButton) {
					tempStable = !tempStable;// ������������activity
					if (tempStable)
						stableTempButton.setText("ȡ����������");
					else
						stableTempButton.setText("��������");
				} else if (v == homeButton) {
					MeasureBp.this.setResult(RESULT_OK);
					MeasureBp.this.finish();
				} else if (v == returnButton) {
					MeasureBp.this.finish();
				} else if (v == uploadButton) {
					uploadButton.setEnabled(false);
					uploadButton.setClickable(false);
					Toast.makeText(context, "��̨��ʼ�ϴ�", Toast.LENGTH_SHORT)
							.show();
					upload();// �ϴ�����
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
		uploadButton.setClickable(false);// ��ʼ��ʱ�����ϴ�����
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
	 * ��ȡ����������Ŀ���еļ�������
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
		dataMap.put(WebService.STATUS, WebService.UNUPLOAD);// ״̬Ϊδ�ϴ�
		return dataMap;
	}

	protected void upload() {
		Tables table = new Tables();
		ExecutorService exec = Executors.newSingleThreadExecutor();// ���̳߳�
		String dbp = lowBpEditText.getText().toString();// ����ѹ
		if (dbp.length() > 0) {// ����ѹ�����ݣ������ϴ�Ѫѹֵ
			String sbp = HighBpEditText.getText().toString();// ����ѹ
			String pulse = pulseEditText.getText().toString();// ����
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
		if (temp.length() > 0) {// ����������
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.TEMP, temp);
			Uploader uploader = new Uploader(dataMap, Cache.TEMP,
					WebService.PATH_TEMP, cache, dbService, handler,
					table.tempTable());
			exec.execute(uploader);
		}
		String bo = boEditText.getText().toString();
		if (bo.length() > 0) {// Ѫ��������
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.BO, bo);
			Uploader uploader = new Uploader(dataMap, Cache.BO,
					WebService.PATH_BO, cache, dbService, handler,
					table.boTable());
			exec.execute(uploader);
		}
	}

	/**
	 * ���ü���button����ʾ������
	 * 
	 * @param status
	 */
	private static void setVisibility() {
		int status;
		if (bluetoothService != null
				&& bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
			status = View.VISIBLE;// ����ʱ���ÿɼ�
		} else {
			status = View.INVISIBLE;// δ����ʱ���ò��ɼ�
		}
		measureButton.setVisibility(status);
		stableBoButton.setVisibility(status);
		stableTempButton.setVisibility(status);
	}

	/**
	 * ��ʼ��id
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
	 * ��������״̬����ʾ
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
	 * ���в���������ֹͣ����
	 */
	private void takeMeasureBp() {
		if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {			
			if (stop == false) {
				bluetoothService.write(PC300.COMMAND_BP_START);				
				initDataTextEdit();
				measureButton.setText("ֹͣѪѹ����");
			} else {
				bluetoothService.write(PC300.COMMAND_BP_STOP);
				measureButton.setText("��ʼѪѹ����");
			}
			stop = !stop;
		}
	}

	/*
	 * ��ʼ��������ʾ����
	 */
	protected void initDataTextEdit() {
		HighBpEditText.setText("");
		lowBpEditText.setText("");
		pulseEditText.setText("");
	}

	/**
	 * ���ӵ�PC300�豸
	 */
	private void connectPC300() {
		if (bluetoothService.getState() == BluetoothService.STATE_NONE) {// ����״̬������

			String address = cache.getDeviceAddress(Cache.PC300);
			btMac = address;
			BluetoothDevice device = bluetoothService
					.getBondedDeviceByAddress(address);
			if (device != null) {
				bluetoothService.connect(device);
				HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
						bluetoothService, PC300.COMMAND_BETTERY, 3000);
				persistWriter.start();// ��������
			} else {
				Toast.makeText(context, "���Ѿ��󶨵�PC300�豸�����Բ����豸...",
						Toast.LENGTH_LONG).show();// û����Թ����豸����������
			}
		}
	}

	/**
	 * ������������PC300�豸
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
		persistWriter.start();// ��������
	}

	/**
	 * �������������豸��activity
	 */
	private void startDeviceListActivity() {
		Intent serverIntent = new Intent(this, BluetoothListActivity.class);
		startActivityForResult(serverIntent,
				BluetoothListActivity.REQUEST_CONNECT_DEVICE);
	}

	/**
	 * �����������û�ָ�������豸�����ؽ�������
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
					uploadButton.setClickable(true);// ���ÿ��Ե��
					Toast.makeText(context, "�����ӵ�$$" + btName,
							Toast.LENGTH_LONG).show();
					setVisibility();// ���ò�����ť�ɼ�
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
				cache.saveDeviceAddress(Cache.PC300, address);// �����ַ,�Ա��´��Դ�����
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
	 * ������������,��������ݿ��ܰ�������Э�����ݣ�����Э��İ�ͷ����������ݷָ
	 * ����ͬһ���͵�����ֻ�������µ���У����ȷ��һ��,���ÿһ���͵�������Ӧ������
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
			int token = map.keyAt(i);// ��ȡtoken
			List<byte[]> datas = map.get(token);
			switch (token) {
			case PC300.TOKEN_BP_CURRENT:
				Integer currentBp = pc300
						.getCurrentBp(datas.get(datas.size() - 1));// ��ȡ��ǰѪѹֵ
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
	 * ����Ѫ��ͼ
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
	 * ����Ѫѹ�����������ʾ�����������쳣�������
	 * 
	 * @param bpResult
	 */
	private static void processBpResult(int[] bpResult) {
		if (bpResult[0] == PC300.ERROR_RESULT) {// �����������
			Log.i(TAG, "ERROR_RESULT:" + bpResult[1]);
			String text = new String();
			switch (bpResult[1]) {
			case PC300.ILLEGAL_PULSE:
				text = "����������Ч������";
				break;
			case PC300.BAD_BOUND:
				text = "����û�а��";
				break;
			case PC300.ERROR_VALUE:
				text = "�����ֵ����";
				break;
			default:
				text = "δ֪����";
			}
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		} else {
			String pulseTag = bpResult[0] == 1 ? "���ʲ���" : "��������";
			Toast.makeText(context, pulseTag, Toast.LENGTH_LONG).show();
			HighBpEditText.setText(Integer.valueOf(bpResult[1]).toString());
			lowBpEditText.setText(Integer.valueOf(bpResult[2]).toString());
			pulseEditText.setText(Integer.valueOf(bpResult[3]).toString());
		}
		measureButton.setText("���²���");// ������Ϻ��ֿ��Բ���
		stop = false;
	}

	/**
	 * ��Ѫ������ͼ
	 * 
	 * @return
	 */
	public View lineView() {
		XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
		XYSeriesRenderer xRenderer = new XYSeriesRenderer();// (������һ���߶���)

		mDataset.addSeries(xSeries);
		// ����ͼ���X��ĵ�ǰ����
		mRenderer
				.setOrientation(XYMultipleSeriesRenderer.Orientation.HORIZONTAL);
		mRenderer.setYTitle("Ѫ��ֵ");// ����y��ı���
		mRenderer.setAxisTitleTextSize(15);// ����������ı���С
		mRenderer.setChartTitle("Ѫ������ͼ");// ����ͼ�����
		mRenderer.setChartTitleTextSize(15);// ����ͼ��������ֵĴ�С
		// mRenderer.setLabelsTextSize(18);// ���ñ�ǩ�����ִ�С
		mRenderer.setXLabels(0);// ����ʾx��
		mRenderer.setLegendTextSize(20);// ����ͼ���ı���С
		mRenderer.setPointSize(1f);// ���õ�Ĵ�С
		mRenderer.setYAxisMin(0);// ����y����Сֵ��0
		mRenderer.setYAxisMax(128);
		mRenderer.setXAxisMax(120);
		mRenderer.setShowGrid(true);// ��ʾ����
		mRenderer.setMargins(new int[] { 1, 15, 1, 1 });// ������ͼλ��

		xRenderer.setColor(Color.BLUE);// ������ɫ
		xRenderer.setPointStyle(PointStyle.CIRCLE);// ���õ����ʽ
		xRenderer.setFillPoints(true);// ���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�
		xRenderer.setLineWidth(2);// �����߿�
		mRenderer.addSeriesRenderer(xRenderer);
		mRenderer.setMarginsColor(Color.WHITE);// ��������Ϊ��ɫ
		mRenderer.setPanEnabled(true, false);// ���ò����ƶ�����
		boWaveView = ChartFactory.getCubeLineChartView(this, mDataset,
				mRenderer, 0.6f);
		boWaveView.setBackgroundColor(Color.WHITE);
		return boWaveView;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// bluetoothService.stop();//
			// �˳�activity��ر���������
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
