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
import com.health.device.BeneCheck;
import com.health.device.HealthDevice;
import com.health.util.TimeHelper;
import com.health.web.Uploader;
import com.health.web.WebService;

/**
 * 血糖测量
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-10-25 上午9:15:11
 */
public class MeasureGlucose extends BaseActivity {
	private static EditText glueEditText = null;// 血糖测量结果
	private static EditText uaEditText = null;// 尿酸测量结果
	private static EditText cholEditText = null;// 总胆固醇测量结果
	private static TextView statusView = null;// 蓝牙连接状态
	private static Button getDataButton = null;// 获取数据按钮
	private static String btName = "BeneCheck";// 蓝牙名称
	private static String btMac = null;// 蓝牙mac地址
	private static BCHandler handler = null;
	private static Context context;
	private static int noDataCount = 0;// 没有数据的项的个数，noDataCount为3是表示三项都没有
	private static final String TAG = "MeasureGlucose";
	private static BluetoothService bluetoothService = null;
	private static Button findButton = null;// 查找设备按钮
	private static Button homeButton;// 返回主界面按钮
	private static Button returnButton;// 返回上一级按钮
	private static Button uploadButton;// 上传数据button
	private static ImageView gluImageView;
	private static ImageView uaImageView;
	private static ImageView cholImageView;

	private static String gluDate;
	private static String uaDate;
	private static String cholDate;

	private static DatabaseService dbService;

	OnClickListener clickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measure_glucose);
		context = this;
		cache = new Cache(context);
		dbService = new DatabaseService(context);
		findViewId();// 初始化控件
		setOnClickListener();// 设置监听器
		if (handler == null)// 只创建一个handler
			handler = new BCHandler();
		bluetoothService = BluetoothService.getService(handler, false);// 同步方式
		connectBeneCheck();// 连接百捷
		setConnectState();
	}

	/**
	 * 初始化控件
	 */
	private void setOnClickListener() {
		clickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (view == homeButton) {// 返回主界面
					MeasureGlucose.this.setResult(RESULT_OK);
					MeasureGlucose.this.finish();
				} else if (view == returnButton) {// 返回上一个界面
					MeasureGlucose.this.finish();
				} else if (view == findButton) {
					startDeviceListActivity();// 开启查找蓝牙activity
				} else if (view == getDataButton) {// 测量血糖
					sendCommd(BeneCheck.QUERY_GLU_NUM);// 查询记录数量，再查询最新的记录
					sendCommd(BeneCheck.QUERY_UA_NUM);
					sendCommd(BeneCheck.QUERY_CHOL_NUM);
					uploadButton.setEnabled(true);// 设置上传按钮可以点击
				} else if (view == uploadButton) {
					uploadButton.setEnabled(false);// 上传后button不可点击
					upload();
					Toast.makeText(context, "后台开始上传", Toast.LENGTH_SHORT)
							.show();
				}
			}
		};
		homeButton.setOnClickListener(clickListener);
		returnButton.setOnClickListener(clickListener);
		uploadButton.setOnClickListener(clickListener);
		getDataButton.setOnClickListener(clickListener);
		findButton.setOnClickListener(clickListener);
	}

	/**
	 * 上传数据
	 */
	protected void upload() {
		Tables tables = new Tables();
		ExecutorService exec = Executors.newSingleThreadExecutor();// 单线程池
		String glu = glueEditText.getText().toString();// 舒张压
		if (glu.length() > 0) {// 葡萄糖有数据，可以上传
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.GLU, glu);
			dataMap.put(Tables.TIME, gluDate);
			Uploader uploader = new Uploader(dataMap, Cache.GLU,
					WebService.PATH_GLU, cache, dbService, handler,
					tables.gluTable());
			exec.execute(uploader);
		}
		String ua = uaEditText.getText().toString();// 舒张压
		if (ua.length() > 0) {// 尿酸有数据，可以上传
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.UA, ua);
			dataMap.put(Tables.TIME, uaDate);
			Uploader uploader = new Uploader(dataMap, Cache.UA,
					WebService.PATH_UA, cache, dbService, handler,
					tables.uaTable());
			exec.execute(uploader);
		}
		String chol = cholEditText.getText().toString();// 舒张压
		if (chol.length() > 0) {// 总胆固醇有数据，可以上传
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.CHOL, chol);
			dataMap.put(Tables.TIME, cholDate);
			Uploader uploader = new Uploader(dataMap, Cache.CHOL,
					WebService.PATH_CHOL, cache, dbService, handler,
					tables.cholTable());
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
	private void findViewId() {
		glueEditText = (EditText) this.findViewById(R.id.gluse_value_et);
		uaEditText = (EditText) this.findViewById(R.id.ua_value_et);
		cholEditText = (EditText) this.findViewById(R.id.chol_value_et);
		statusView = (TextView) this.findViewById(R.id.connect_status);
		findButton = (Button) this.findViewById(R.id.find_device);
		homeButton = (Button) findViewById(R.id.to_home_button);
		returnButton = (Button) findViewById(R.id.return_button);
		getDataButton = (Button) findViewById(R.id.measure_getdata);
		homeButton = (Button) findViewById(R.id.to_home_button);
		returnButton = (Button) findViewById(R.id.return_button);
		uploadButton = (Button) findViewById(R.id.upload_button);
		gluImageView = (ImageView) findViewById(R.id.glu_image);
		uaImageView = (ImageView) findViewById(R.id.ua_image);
		cholImageView = (ImageView) findViewById(R.id.chol_image);
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
		getDataButton.setVisibility(status);		
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
	 * 连接百捷
	 */
	private void connectBeneCheck() {
		if (bluetoothService.getState() == BluetoothService.STATE_NONE) {// 空闲状态下才连接
			String address = cache.getDeviceAddress(Cache.BENECHECK);
			btMac = address;
			BluetoothDevice device = bluetoothService
					.getBondedDeviceByAddress(address);
			if (device != null) {
				bluetoothService.connect(device);// 连接设备
				HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
						bluetoothService, BeneCheck.ECHO, 1000);
				if (!persistWriter.isAlive())
					persistWriter.start();// 持续握手
			} else {
				Toast.makeText(context, "无已经绑定设备，试试查找设备...", Toast.LENGTH_LONG)
						.show();// 没有配对过得设备，启动查找
			}
		}

	}

	/**
	 * 连接搜索到的百捷设备
	 * 
	 * @param address
	 */
	private void connectBeneCheck(String address) {
		btMac = address;
		BluetoothDevice device = bluetoothService
				.getRemoteDeviceByAddress(address);
		bluetoothService.connect(device);
		HealthDevice.PersistWriter persistWriter = new HealthDevice.PersistWriter(
				bluetoothService, BeneCheck.ECHO, 1000);
		persistWriter.start();// 持续握手
	}

	/**
	 * 查找蓝牙后，用户指定连接设备，返回进行连接
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BluetoothListActivity.REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						BluetoothListActivity.EXTRA_DEVICE_ADDRESS);
				connectBeneCheck(address);
			}
			break;
		}
	}

	protected void onResume() {
		super.onResume();
		// 注册消息处理器

	}

	private static class BCHandler extends Handler {
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
				SparseArray<List<byte[]>> map = BeneCheck
						.getLegalPatternsFromBuffer(readBuf);
				int dataSize = map.size();
				for (int i = 0; i < dataSize; i++) {
					int token = map.keyAt(i);// 获取token
					List<byte[]> datas = map.get(token);
					switch (token) {
					case BeneCheck.TOKEN_GLU_NUM:// 以前总感觉switch的break怪怪的.break让分支独立
					case BeneCheck.TOKEN_CHOL_NUM:// 现在不加break时，会顺序执行，这在多个case值需要
					case BeneCheck.TOKEN_UA_NUM:// 相同的处理时很有用，避免的代码的重复
						int num = BeneCheck.getNum(datas.get(datas.size() - 1));
						if (num >= 1) {// 有记录时才去数据
							byte[] command = BeneCheck.getLatestRecordCommand(
									token, 1);
							sendCommd(command);// 得到了记录数目后,马上发送查询记录命令
						} else {
							noDataCount++;
							if (noDataCount >= 3)
								Toast.makeText(context, "无数据",
										Toast.LENGTH_LONG).show();
						}
						break;
					case BeneCheck.TOKEN_GLU_RECORD:
						BeneCheck.Record gluRecord = BeneCheck.getRecord(datas
								.get(datas.size() - 1));
						gluDate = gluRecord.date;
						glueEditText.setText("" + gluRecord.value);
						break;
					case BeneCheck.TOKEN_UA_RECORD:
						BeneCheck.Record uaRecord = BeneCheck.getRecord(datas
								.get(datas.size() - 1));
						uaDate = uaRecord.date;
						uaEditText.setText("" + uaRecord.value);
						break;
					case BeneCheck.TOKEN_CHOL_RECORD:
						BeneCheck.Record cholRecord = BeneCheck.getRecord(datas
								.get(datas.size() - 1));
						cholDate = cholRecord.date;
						cholEditText.setText("" + cholRecord.value);
						break;
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
				cache.saveDeviceAddress(Cache.BENECHECK, address);// 保存地址,以便下次自带连接
				break;
			case Uploader.MESSAGE_UPLOADE_RESULT:
				Bundle bundler = msg.getData();
				String item = bundler.getString(Cache.ITEM);
				int status = bundler.getInt(Uploader.STUTAS);
				if (Cache.GLU.equals(item)) {
					setImageView(gluImageView, status);
				}
				if (Cache.UA.equals(item))
					setImageView(uaImageView, status);
				if (Cache.CHOL.equals(item))
					setImageView(cholImageView, status);
				break;
			}

		}
	};

	private static void setImageView(ImageView imageview, int color) {
		if (color == Uploader.FAILURE || color == Uploader.NET_ERROR)
			imageview.setImageResource(R.drawable.light_red);
		else if (color == Uploader.OK)
			imageview.setImageResource(R.drawable.light_greed);
		else
			imageview.setImageResource(R.drawable.light_blue);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			MeasureGlucose.this.setResult(RESULT_OK);
			MeasureGlucose.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
}
