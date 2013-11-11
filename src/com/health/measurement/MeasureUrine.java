package com.health.measurement;

import java.util.Arrays;
import java.util.List;

import com.health.bluetooth.BluetoothListActivity;
import com.health.bluetooth.BluetoothService;
import com.health.device.BeneCheck;
import com.health.device.GmpUa;
import com.health.device.GmpUa.UaRecord;
import com.health.device.HealthDevice;
import com.health.util.MiniDataBase;

import cn.younext.BatteryView;
import cn.younext.R;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ��Һ������,���Բ��� ��ϸ�� �������� ��ԭ ������ pHֵ ǱѪ ���� ͪ�� ������ ������
 * ά����C 12����Ŀ
 * 
 * @author jiqunpeng
 * 
 *         ����ʱ�䣺2013-10-30 ����10:39:37
 */
public class MeasureUrine extends Activity {
	private static Button homeBottun;// ���������水ť
	private static Button returnBotton;// ������һ����ť
	private static Button getDataButton;// ��ȡ�������ݰ�ť

	private static TextView statusView = null;// ��������״̬
	private static String btName = "BeneCheck";// ��������
	private static UAHandler handler = null;
	private static Context context;
	private static MiniDataBase miniDb;
	private static Button findButton = null;// �����豸��ť
	private static TextView leuTextView;
	private static TextView nitTextView;
	private static TextView ubgTextView;
	private static TextView proTextView;
	private static TextView phTextView;
	private static TextView sgTextView;
	private static TextView bldTextView;
	private static TextView ketTextView;
	private static TextView bilTextView;
	private static TextView gluTextView;
	private static TextView vcTextView;
	private static TextView dateTextView;

	private static final String TAG = "MeasureUrine";
	private static BluetoothService bluetoothService = null;
	OnClickListener clickListener;
	BatteryView myview;

	TextView user;
	String username;
	int userid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.measure_urine);
		initLayout();

	}

	/**
	 * ���Խ���ĸ��ְ�ť���ı�
	 */
	private void initLayout() {
		context = this;
		miniDb = new MiniDataBase(context);
		handler = new UAHandler();
		bluetoothService = BluetoothService.getService(handler, false);
		myview = (BatteryView) findViewById(R.id.batteryview);
		homeBottun = (Button) findViewById(R.id.test_taixin_homeBtn);
		returnBotton = (Button) findViewById(R.id.test_taixin_returnBtn);
		getDataButton = (Button) findViewById(R.id.get_data_button);
		leuTextView = (TextView) findViewById(R.id.leu);
		nitTextView = (TextView) findViewById(R.id.nit);
		ubgTextView = (TextView) findViewById(R.id.ubg);
		proTextView = (TextView) findViewById(R.id.pro);
		phTextView = (TextView) findViewById(R.id.pH);
		sgTextView = (TextView) findViewById(R.id.sg);
		bldTextView = (TextView) findViewById(R.id.bld);
		ketTextView = (TextView) findViewById(R.id.ket);
		bilTextView = (TextView) findViewById(R.id.bil);
		gluTextView = (TextView) findViewById(R.id.glu);
		vcTextView = (TextView) findViewById(R.id.vc);
		dateTextView = (TextView) findViewById(R.id.measure_time);
		statusView = (TextView) findViewById(R.id.connect_status);
		findButton = (Button) findViewById(R.id.find_device);
		clickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {

				if (view == homeBottun) {
					MeasureUrine.this.setResult(RESULT_OK);
					MeasureUrine.this.finish();
				} else if (view == returnBotton) {
					MeasureUrine.this.finish();
				} else if (view == getDataButton) {
					sendCommd(GmpUa.COMMAND_SINGLE_DATA);
				} else if (view == findButton) {
					startDeviceListActivity();// ������������activity
				}
			}
		};
		homeBottun.setOnClickListener(clickListener);
		returnBotton.setOnClickListener(clickListener);
		getDataButton.setOnClickListener(clickListener);
		findButton.setOnClickListener(clickListener);
		connectGmpUa();
		setConnectState();
	}

	/**
	 * ��������״̬
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
	 * �������������豸��activity
	 */
	private void startDeviceListActivity() {
		Intent serverIntent = new Intent(this, BluetoothListActivity.class);
		startActivityForResult(serverIntent,
				BluetoothListActivity.REQUEST_CONNECT_DEVICE);
	}

	/**
	 * ������Һ�������豸
	 */
	private void connectGmpUa() {
		if (bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			String address = miniDb.getDeviceAddress(MiniDataBase.GMPUA);
			BluetoothDevice device = bluetoothService
					.getBondedDeviceByAddress(address);
			if (device != null) {
				bluetoothService.connect(device);// �����豸
				HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
						bluetoothService, GmpUa.CONFIRM, 10000);
				if (!persistWriter.isAlive())
					persistWriter.start();// ��������
			} else {
				Toast.makeText(context, "���Ѿ����豸�����Բ����豸...", Toast.LENGTH_LONG)
						.show();// û����Թ����豸����������
			}
		}
	}

	/**
	 * ��������������Һ�������豸
	 * 
	 * @param address
	 */
	private void connectGmpUa(String address) {
		BluetoothDevice device = bluetoothService
				.getRemoteDeviceByAddress(address);
		bluetoothService.connect(device);
		HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
				bluetoothService, GmpUa.CONFIRM, 10000);
		persistWriter.start();// ��������
	}

	private static class UAHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					statusView.setText(btName);
					Toast.makeText(context, "�����ӵ�" + btName, Toast.LENGTH_LONG)
							.show();
					getDataButton.setVisibility(View.VISIBLE);// ���ò�����ť�ɼ�
					break;
				case BluetoothService.STATE_CONNECTING:
					statusView.setText(R.string.connecting);
					getDataButton.setVisibility(View.GONE);
					break;
				case BluetoothService.STATE_NONE:
					statusView.setText(R.string.unconnect);
					getDataButton.setVisibility(View.GONE);
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
				Log.i(TAG, "get:" + Arrays.toString(readBuf));
				SparseArray<List<byte[]>> map = GmpUa
						.getLegalPatternsFromBuffer(readBuf);
				int dataSize = map.size();
				for (int i = 0; i < dataSize; i++) {
					int token = map.keyAt(i);// ��ȡtoken
					List<byte[]> datas = map.get(token);
					switch (token) {
					case GmpUa.TOKEN_SINGLE_DATA:
						GmpUa.UaRecord record = GmpUa.parseRecord(datas
								.get(datas.size() - 1));
						showRecord(record);// ��ʾ��¼
					}
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
				miniDb.saveDeviceAddress(MiniDataBase.GMPUA, address);// �����ַ,�Ա��´��Դ�����
				break;
			}
		}

	};

	/**
	 * ��������
	 * 
	 * @param command
	 */
	private static void sendCommd(byte[] command) {
		Log.i(TAG, "send:" + Arrays.toString(command));
		if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED
				&& command != null)
			bluetoothService.write(command);
	}

	/**
	 * ��ʾ��¼
	 * 
	 * @param record
	 */
	public static void showRecord(UaRecord record) {
		leuTextView.setText(record.leu);
		nitTextView.setText(record.nit);
		ubgTextView.setText(record.ubg);
		proTextView.setText(record.pro);
		phTextView.setText(record.ph);
		sgTextView.setText(record.sg);
		bldTextView.setText(record.bld);
		ketTextView.setText(record.ket);
		bilTextView.setText(record.bil);
		gluTextView.setText(record.glu);
		vcTextView.setText(record.vc);
		dateTextView.setText(record.date);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BluetoothListActivity.REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						BluetoothListActivity.EXTRA_DEVICE_ADDRESS);
				connectGmpUa(address);
			}
			break;
		}
	}

	protected void onResume() {
		super.onResume();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {

			MeasureUrine.this.setResult(RESULT_OK);
			MeasureUrine.this.finish();
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

}
