package com.health.measurement;

import java.util.Arrays;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.younext.R;

import com.health.bluetooth.BluetoothListActivity;
import com.health.bluetooth.BluetoothService;
import com.health.pc300.PC300;
import com.health.pc300.PC300.PersistWriter;

/**
 * ����Ѫѹ
 * 
 */
public class MeasureBp extends Activity {

	EditText HighBpEditText = null;// ��ѹ�ı���
	EditText lowBpEditText = null;// ��ѹ�ı���
	EditText pulseEditText = null;// ����
	EditText boEditText = null;// Ѫ��
	Button measureButton = null;// ���Բ���Ѫѹ��ť
	Button findButton = null;// �����豸��ť
	TextView statusView = null;// ��������״̬

	private static final boolean DEBUG = true;
	private static final String TAG = "MeasureBp";
	private BluetoothService bluetoothService = null;

	private final static String NAME = "PC_300SNT";

	private boolean stop = false;
	private LinearLayout graphLayout;// װѪ��ͼ�Ĳ���
	private GraphicalView boWaveView;// Ѫ��ͼ
	XYSeries xSeries = new XYSeries("Ѫ��");
	XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private static float boWaveIndex = 0;
	private static int maxBo = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.measure_bp);

		HighBpEditText = (EditText) findViewById(R.id.hp);
		lowBpEditText = (EditText) findViewById(R.id.lp);
		pulseEditText = (EditText) findViewById(R.id.mb);
		boEditText = (EditText) findViewById(R.id.bo);
		measureButton = (Button) findViewById(R.id.getdata);
		findButton = (Button) findViewById(R.id.find_device);
		statusView = (TextView) findViewById(R.id.connect_status);
		bluetoothService = new BluetoothService(handler);
		measureButton.setVisibility(View.GONE);// ��ʼ���ò��ɼ�
		graphLayout = (LinearLayout) this.findViewById(R.id.bo_image_view);
		graphLayout.addView(lineView());
		connectPC300();
		measureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				takeMeasureBp();
			}
		});
		findButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startDeviceListActivity();// ������������activity
			}

		});

	}

	/**
	 * ���в���������ֹͣ����
	 */
	private void takeMeasureBp() {
		if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
			if (stop == false) {
				bluetoothService.write(PC300.COMMAND_BP_START);
				initDataTextEdit();
				measureButton.setText("ֹͣ����");
			} else {
				bluetoothService.write(PC300.COMMAND_BP_STOP);
				measureButton.setText("��ʼ����");
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
		BluetoothDevice device = bluetoothService.getBluetoothDevice(NAME);
		if (device != null) {
			bluetoothService.connect(device);
			PersistWriter persistWriter = new PC300.PersistWriter(
					bluetoothService);
			if (!persistWriter.isAlive())
				persistWriter.start();// ��������
		} else {
			Toast.makeText(getApplicationContext(), "���Ѿ��󶨵�PC300�豸�����Բ����豸...",
					Toast.LENGTH_LONG).show();// û����Թ����豸����������
		}
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DEBUG)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case BluetoothListActivity.REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						BluetoothListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = bluetoothService
						.getRemoteDeviceByAddress(address);
				bluetoothService.connect(device);
			}
			break;
		}
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					statusView.setText(NAME);
					Toast.makeText(getApplicationContext(), "�����ӵ�" + NAME,
							Toast.LENGTH_LONG).show();
					measureButton.setVisibility(View.VISIBLE);// ���ò�����ť�ɼ�
					break;
				case BluetoothService.STATE_CONNECTING:
					statusView.setText("��������...");
					measureButton.setVisibility(View.GONE);
					break;
				case BluetoothService.STATE_NONE:
					statusView.setText("δ����");
					measureButton.setVisibility(View.GONE);
					break;
				}
				break;

			case BluetoothService.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				new String(writeBuf);
				// Toast.makeText(getApplicationContext(),
				// writeMessage,
				// Toast.LENGTH_LONG).show();
				break;
			case BluetoothService.MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;
				processReadData(readBuf);
				if (DEBUG) {
					// Log.d(TAG, readMessage);
					Log.d(TAG, Arrays.toString(readBuf));

				}
				break;
			case BluetoothService.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(BluetoothService.TOAST),
						Toast.LENGTH_SHORT).show();
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
	public void processReadData(byte[] buffer) {
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
					int[] value = pc300.getBoWave(data);
					updateWaveImage(value);
				}
			case PC300.TOKEN_BO_PAKAGE:
				Integer spO2 = pc300.getSpO2(datas.get(datas.size() - 1));
				if (spO2 > maxBo) {
					maxBo = spO2;
					boEditText.setText(spO2.toString());
				}
			}
		}
	}

	/**
	 * ����Ѫ��ͼ
	 * 
	 * @param data
	 */
	private void updateWaveImage(int[] data) {
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
	private void processBpResult(int[] bpResult) {
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
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG)
					.show();
		} else {
			String pulseTag = bpResult[0] == 1 ? "���ʲ���" : "��������";
			Toast.makeText(getApplicationContext(), pulseTag, Toast.LENGTH_LONG)
					.show();
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

		// for (int i = 0; i < 70; i++) {
		// xSeries.add(i * 1.0 / 10, Math.sin(i * 1.0 /
		// 10));
		// }
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
		mRenderer.setMargins(new int[] { 1, 5, 1, 1 });// ������ͼλ��

		xRenderer.setColor(Color.BLUE);// ������ɫ
		xRenderer.setPointStyle(PointStyle.CIRCLE);// ���õ����ʽ
		xRenderer.setFillPoints(true);// ���㣨��ʾ�ĵ��ǿ��Ļ���ʵ�ģ�
		xRenderer.setLineWidth(2);// �����߿�
		mRenderer.addSeriesRenderer(xRenderer);
		mRenderer.setMarginsColor(Color.TRANSPARENT);// ��������Ϊ͸��
		mRenderer.setZoomEnabled(true, true);// ���ò����ƶ�����
		boWaveView = ChartFactory.getCubeLineChartView(this, mDataset,
				mRenderer, 0.6f);
		boWaveView.setBackgroundColor(Color.WHITE);
		return boWaveView;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			bluetoothService.stop();// �˳�activity��ر���������
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
