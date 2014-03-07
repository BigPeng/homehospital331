package com.health.measurement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.bluetooth.BluetoothListActivity;
import com.health.bluetooth.BluetoothService;
import com.health.database.Cache;
import com.health.database.DatabaseService;
import com.health.database.Tables;
import com.health.device.GmpUa;
import com.health.device.GmpUa.UaRecord;
import com.health.device.HealthDevice;
import com.health.web.Uploader;
import com.health.web.WebService;

/**
 * 尿液分析仪,可以测量 白细胞 亚硝酸盐 尿胆原 蛋白质 pH值 潜血 比重 酮体 胆红素 葡萄糖
 * 维生素C 12个项目
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-10-30 上午10:39:37
 */
public class MeasureUrine extends BaseActivity {

	private static TextView statusView = null;// 蓝牙连接状态
	private static String btName = "BeneCheck";// 蓝牙名称
	private static String btMac = null;// 蓝牙mac
	private static UAHandler handler = null;
	private static Context context;
	private static Button homeButton;// 返回主界面按钮
	private static Button returnButton;// 返回上一步按钮
	private static Button uploadButton;// 上次数据按钮
	private static Button getDataButton;// 获取最新数据按钮
	private static Button findButton;// 查找设备按钮
	private static EditText leuEditText;
	private static EditText nitEditText;
	private static EditText ubgEditText;
	private static EditText proEditText;
	private static EditText phEditText;
	private static EditText sgEditText;
	private static EditText bldEditText;
	private static EditText ketEditText;
	private static EditText bilEditText;
	private static EditText gluEditText;
	private static EditText vcEditText;
	private static EditText dateEditText;

	private static ImageView imageView1;
	private static ImageView imageView2;
	private static ImageView imageView3;

	private static DatabaseService dbService;
	private static final String TAG = "MeasureUrine";
	private static BluetoothService bluetoothService = null;
	OnClickListener clickListener;
	EditText user;
	String username;
	int userid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measure_urine);
		context = this;
		dbService = new DatabaseService(context);
		cache = new Cache(context);
		if (handler == null)
			handler = new UAHandler();
		findViewid();
		setOnClickListener();// 设置监听器
		bluetoothService = BluetoothService.getService(handler, false);
		connectGmpUa();
		setConnectState();
		uploadButton.setEnabled(false);
	}

	/**
	 * 设置监听器
	 */
	private void setOnClickListener() {
		clickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {

				if (view == homeButton) {
					MeasureUrine.this.setResult(RESULT_OK);
					MeasureUrine.this.finish();
				} else if (view == returnButton) {
					MeasureUrine.this.finish();
				} else if (view == getDataButton) {
					sendCommd(GmpUa.COMMAND_SINGLE_DATA);
					uploadButton.setEnabled(true);// 获取数据后才能上传数据
				} else if (view == findButton) {
					startDeviceListActivity();// 开启查找蓝牙activity
				} else if (view == uploadButton) {
					uploadButton.setEnabled(false);// 按钮不可用，避免多次上传
					upload();
					Toast.makeText(context, "后台开始上传", Toast.LENGTH_SHORT)
							.show();
				}
			}
		};
		homeButton.setOnClickListener(clickListener);
		returnButton.setOnClickListener(clickListener);
		uploadButton.setOnClickListener(clickListener);
		returnButton.setOnClickListener(clickListener);
		getDataButton.setOnClickListener(clickListener);
		findButton.setOnClickListener(clickListener);
	}

	protected void upload() {
		ExecutorService exec = Executors.newSingleThreadExecutor();// 单线程池
		String leu = leuEditText.getText().toString();
		if (leu.length() > 0) {
			String nit = nitEditText.getText().toString();
			String ubg = ubgEditText.getText().toString();
			String pro = proEditText.getText().toString();
			String ph = phEditText.getText().toString();
			String sg = sgEditText.getText().toString();
			String bld = bldEditText.getText().toString();
			String ket = ketEditText.getText().toString();
			String bil = bilEditText.getText().toString();
			String uglu = gluEditText.getText().toString();
			String vc = vcEditText.getText().toString();
			String time = dateEditText.getText().toString();
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.LEU, leu);
			dataMap.put(Tables.NIT, nit);
			dataMap.put(Tables.UBG, ubg);
			dataMap.put(Tables.PRO, pro);
			dataMap.put(Tables.PH, ph);
			dataMap.put(Tables.SG, sg);
			dataMap.put(Tables.BLD, bld);
			dataMap.put(Tables.KET, ket);
			dataMap.put(Tables.BLD, bld);
			dataMap.put(Tables.BIL, bil);
			dataMap.put(Tables.UGLU, uglu);
			dataMap.put(Tables.VC, vc);
			dataMap.put(Tables.TIME, time);
			Tables tables = new Tables();
			Uploader uploader = new Uploader(dataMap, Cache.URINE,
					WebService.PATH_URINE, cache, dbService, handler,
					tables.urineTable());
			exec.execute(uploader);
		}
	}

	/***
	 * 获取几个测量项目都有的几个属性
	 * 
	 * @return
	 */
	private Map<String, String> getDefaltAttrs() {
		String idCard = cache.getUserId();
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put(Tables.DEVICEMAC, btMac);
		dataMap.put(Tables.DEVICENAME, btName);
		dataMap.put(Tables.CARDNO, idCard);
		dataMap.put(WebService.STATUS, WebService.UNUPLOAD);// 状态为未上传
		return dataMap;
	}

	/**
	 * 初始化控件
	 */
	private void findViewid() {
		imageView1 = (ImageView) findViewById(R.id.urine_status_image1);
		imageView2 = (ImageView) findViewById(R.id.urine_status_image2);
		imageView3 = (ImageView) findViewById(R.id.urine_status_image3);
		homeButton = (Button) findViewById(R.id.to_home_button);
		returnButton = (Button) findViewById(R.id.return_button);
		uploadButton = (Button) findViewById(R.id.upload_button);
		getDataButton = (Button) findViewById(R.id.get_data_button);
		leuEditText = (EditText) findViewById(R.id.leu);
		nitEditText = (EditText) findViewById(R.id.nit);
		ubgEditText = (EditText) findViewById(R.id.ubg);
		proEditText = (EditText) findViewById(R.id.pro);
		phEditText = (EditText) findViewById(R.id.pH);
		sgEditText = (EditText) findViewById(R.id.sg);
		bldEditText = (EditText) findViewById(R.id.bld);
		ketEditText = (EditText) findViewById(R.id.ket);
		bilEditText = (EditText) findViewById(R.id.bil);
		gluEditText = (EditText) findViewById(R.id.glu);
		vcEditText = (EditText) findViewById(R.id.vc);
		dateEditText = (EditText) findViewById(R.id.measure_time);
		statusView = (TextView) findViewById(R.id.connect_status);
		findButton = (Button) findViewById(R.id.find_device);
	}

	/**
	 * 设置连接状态
	 */
	private void setConnectState() {
		setVisibility();
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
	 * 开启查找蓝牙设备的activity
	 */
	private void startDeviceListActivity() {
		Intent serverIntent = new Intent(this, BluetoothListActivity.class);
		startActivityForResult(serverIntent,
				BluetoothListActivity.REQUEST_CONNECT_DEVICE);
	}

	/**
	 * 连接尿液分析仪设备
	 */
	private void connectGmpUa() {
		if (bluetoothService.getState() == BluetoothService.STATE_NONE) {
			String address = cache.getDeviceAddress(Cache.GMPUA);
			btMac = address;
			BluetoothDevice device = bluetoothService
					.getBondedDeviceByAddress(address);
			if (device != null) {
				bluetoothService.connect(device);// 连接设备
				HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
						bluetoothService, GmpUa.CONFIRM, 10000);
				if (!persistWriter.isAlive())
					persistWriter.start();// 持续握手
			} else {
				Toast.makeText(context, "无已经绑定设备，试试查找设备...", Toast.LENGTH_LONG)
						.show();// 没有配对过得设备，启动查找
			}
		}
	}

	/**
	 * 连接搜索到的尿液分析仪设备
	 * 
	 * @param address
	 */
	private void connectGmpUa(String address) {
		btMac = address;
		BluetoothDevice device = bluetoothService
				.getRemoteDeviceByAddress(address);
		bluetoothService.connect(device);
		HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
				bluetoothService, GmpUa.CONFIRM, 10000);
		persistWriter.start();// 持续握手
	}

	private static class UAHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					statusView.setText(btName);
					Toast.makeText(context, "已连接到" + btName, Toast.LENGTH_LONG)
							.show();
					setVisibility();// 设置测量按钮可见
					break;
				case BluetoothService.STATE_CONNECTING:
					statusView.setText(R.string.connecting);
					setVisibility();
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
				Log.i(TAG, "get:" + Arrays.toString(readBuf));
				SparseArray<List<byte[]>> map = GmpUa
						.getLegalPatternsFromBuffer(readBuf);
				int dataSize = map.size();
				for (int i = 0; i < dataSize; i++) {
					int token = map.keyAt(i);// 获取token
					List<byte[]> datas = map.get(token);
					switch (token) {
					case GmpUa.TOKEN_SINGLE_DATA:
						GmpUa.UaRecord record = GmpUa.parseRecord(datas
								.get(datas.size() - 1));
						if (null == record) {
							Toast.makeText(context, "无存储数据", Toast.LENGTH_LONG)
									.show();
						} else
							showRecord(record);// 显示记录
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
				cache.saveDeviceAddress(Cache.GMPUA, address);// 保存地址,以便下次自带连接
				break;
			case Uploader.MESSAGE_UPLOADE_RESULT:
				Bundle bundler = msg.getData();
				int status = bundler.getInt(Uploader.STUTAS);
				String result = "上传失败";
				if (status == Uploader.OK) {
					result = "上传成功";
					setImageViews(status);
				}
				if (status == Uploader.NET_ERROR) {
					result = "网络异常";
					setImageViews(status);
				}
				Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

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
		getDataButton.setVisibility(status);
	}

	/**
	 * 发送命令
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
	 * 显示记录
	 * 
	 * @param record
	 */
	public static void showRecord(UaRecord record) {
		leuEditText.setText(record.leu);
		nitEditText.setText(record.nit);
		ubgEditText.setText(record.ubg);
		proEditText.setText(record.pro);
		phEditText.setText(record.ph);
		sgEditText.setText(record.sg);
		bldEditText.setText(record.bld);
		ketEditText.setText(record.ket);
		bilEditText.setText(record.bil);
		gluEditText.setText(record.glu);
		vcEditText.setText(record.vc);
		dateEditText.setText(record.date);
	}

	private static void setImageViews(int status) {
		setImageView(imageView1, status);
		setImageView(imageView2, status);
		setImageView(imageView3, status);
	}

	private static void setImageView(ImageView imageview, int status) {
		if (status == Uploader.FAILURE || status == Uploader.NET_ERROR)
			imageview.setImageResource(R.drawable.light_red);
		else if (status == Uploader.OK)
			imageview.setImageResource(R.drawable.light_greed);
		else
			imageview.setImageResource(R.drawable.light_blue);
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
