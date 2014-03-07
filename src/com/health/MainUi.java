package com.health;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import org.apache.http.util.EncodingUtils;

import com.health.bluetooth.BluetoothService;
import com.health.measurement.Measurement;
import com.health.myhealth.Myhealth;

import android.util.Log;
import android.view.KeyEvent;
import cn.younext.DatabaseHelper;
import cn.younext.R;
import cn.younext.calendar_medical;
import cn.younext.healthinformation;
import cn.younext.healthreport_alarm;
import cn.younext.teleference;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class MainUi extends BaseActivity {
	/** Called when the activity is first created. */
	Button main_myhealth;
	Button main_test;
	Button main_teleference;
	Button main_calendar;
	Button main_healthinformation;

	OnClickListener btnClick;
	// Spinner myspinner;
	DatabaseHelper helper;
	SQLiteDatabase db;
	Cursor c;
	int userid = 1;
	final String USERIDFILE = "userid.txt";
	final String FILE_PATH = "/data/data/cn.younext/";
	File useridfile;
	File healthreport_tag;
	FileOutputStream out;
	FileInputStream in;
	String username = "";
	TextView spinnertext;
	TextView dianliang;
	private static Spinner spinner;

	// private static final String[]
	// users={"用户一","用户二","用户三","用户四","用户五"};
	// private ArrayAdapter<String> adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		spinner = (Spinner) this.findViewById(R.id.user_name_spinner);
		setSinppner();
		try {
			// 创建“tag1”文件
			useridfile = new File(FILE_PATH, USERIDFILE);
			if (!useridfile.exists()) {
				useridfile.createNewFile();
				out = new FileOutputStream(useridfile);
				String text = Integer.toString(userid);
				Log.v("out", "out");
				out.write(text.getBytes());
				out.close();
			}

			in = new FileInputStream(useridfile);
			int length = (int) useridfile.length();
			byte[] temp = new byte[length];
			in.read(temp, 0, length);
			String text = EncodingUtils.getString(temp, "UTF-8");
			userid = Integer.parseInt(text);
			Log.v("userid", String.valueOf(userid));
			in.close();

		} catch (IOException e) {
			// 将出错信息打印到Logcat
			Log.e("I/O", e.toString());
			// this.finish();
		}

		try {
			String DATABASE_DIR_PATH = this
					.getDir("databases", MODE_WORLD_WRITEABLE).getParentFile()
					.getAbsolutePath()
					+ "/databases"; // 得到数据库文件夹目录在系统中的绝对路径
			String databasefilename = DATABASE_DIR_PATH + "/" + "testrecord"; // 得到数据库在系统中的绝对路径

			File databases = new File(DATABASE_DIR_PATH);
			if (!databases.exists())
				databases.mkdir(); // 如果数据库目录不存在，则新建一个
			Log.e("before executing", "before copying");
			if (!(new File(databasefilename)).exists()) {
				InputStream is = getResources().openRawResource(
						R.raw.testrecord);
				FileOutputStream fos = new FileOutputStream(databasefilename);
				byte buffer[] = new byte[8192];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}

				fos.close();
				is.close();
				Log.e("good news", "can copy this file");
			}
		} catch (Exception e) {

		}

		main_myhealth = (Button) findViewById(R.id.main_myhealthid);
		main_test = (Button) findViewById(R.id.main_testid);
		main_teleference = (Button) findViewById(R.id.main_teleferenceid);
		main_calendar = (Button) findViewById(R.id.main_calendarid);
		main_healthinformation = (Button) findViewById(R.id.main_healthinformationid);

		// xueyaList=(ListView)findViewById(R.id.xueyarecord);
		helper = new DatabaseHelper(MainUi.this, DatabaseHelper.DATABASE_NAME,
				null, DatabaseHelper.Version);
		db = helper.getWritableDatabase();// 打开数据库

		c = db.query("usermanage", null, null, null, null, null, "_id asc",
				null);// 用户名字查询
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainUi.this,
				android.R.layout.simple_spinner_item, c,
				new String[] { DatabaseHelper.USER_NAME },
				new int[] { R.id.text1 });
		adapter.setDropDownViewResource(R.layout.main_spinner_dropdown);

		btnClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if (v == main_myhealth) {
					intent.setClass(MainUi.this, Myhealth.class);
					intent.putExtra("userid", userid);
					intent.putExtra("username", username);
					// i.putExtra("userneme",
					// username);
					startActivity(intent);
				} else if (v == main_test) {
					intent.setClass(MainUi.this, Measurement.class);
					intent.putExtra("userid", userid);
					intent.putExtra("username", username);
					startActivity(intent);
				} else if (v == main_teleference) {
					intent.setClass(MainUi.this, teleference.class);
					intent.putExtra("userid", userid);
					intent.putExtra("username", username);
					startActivity(intent);
				} else if (v == main_calendar) {
					intent.setClass(MainUi.this, calendar_medical.class);
					intent.putExtra("userid", userid);
					intent.putExtra("username", username);
					startActivity(intent);
				} else if (v == main_healthinformation) {
					intent.setClass(MainUi.this, healthinformation.class);
					intent.putExtra("userid", userid);
					intent.putExtra("username", username);
					startActivity(intent);
				}
			}

		};
		main_myhealth.setOnClickListener(btnClick);
		main_test.setOnClickListener(btnClick);
		main_teleference.setOnClickListener(btnClick);
		main_calendar.setOnClickListener(btnClick);
		main_healthinformation.setOnClickListener(btnClick);

		int intervalTime = 600000;// 150分钟
		int intervalweek = 86400000 * 7;
		Calendar alarm = Calendar.getInstance();
		Calendar currentTime = Calendar.getInstance();
		currentTime.setTimeInMillis(System.currentTimeMillis());

		Log.v("week", String.valueOf(alarm.get(Calendar.DAY_OF_WEEK)));

		alarm.set(Calendar.HOUR_OF_DAY, 11);
		alarm.set(Calendar.MINUTE, 10);
		alarm.set(Calendar.SECOND, 0);
		alarm.set(Calendar.MILLISECOND, 0);
		Intent intent = new Intent(MainUi.this, healthreport_alarm.class);
		intent.putExtra("userid", userid);
		intent.putExtra("username", "username");
		PendingIntent sender = PendingIntent.getBroadcast(MainUi.this, 11,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am;
		am = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(),
				intervalTime, sender);
		Log.v("main", "main set alarm");
	}

	@Override
	public void onStart() {
		super.onStart();
		setSinppner();
	}

	private void setSinppner() {
		String[] spinnerTitle = { userName, "切换用户" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, spinnerTitle);

		// 设置下拉列表风格
		adapter.setDropDownViewResource(R.layout.main_spinner_dropdown);
		spinner.setAdapter(adapter);
		// 添加Spinner事件监听
		spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int groupId, long arg3) {
				arg0.setVisibility(View.VISIBLE);
				// 切换用户
				if (groupId == 1) {
					Intent intent = new Intent(MainUi.this, Login.class);
					startActivity(intent);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected void onResume() {
		super.onResume();

	}

	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出！",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				BluetoothService.close();// 关闭蓝牙
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}