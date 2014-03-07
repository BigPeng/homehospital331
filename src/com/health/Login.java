package com.health;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cn.younext.R;

import com.health.database.Cache;
import com.health.util.MyProgressDialog;
import com.health.web.WebService;

public class Login extends BaseActivity {
	private static final String UID = "330772197805283758";
	private static final String PSW = "abcd4321";
	protected static final int MESSAGE_TOAST = 10000;
	private static final String TOAST = "toast";
	private static final int ENABLED = 10001;
	private static AutoCompleteTextView countET;
	private static EditText passwordET;
	private static Button loginButton;
	private static Button logupButton;
	private static Context context;
	private static ExecutorService exec = Executors.newSingleThreadExecutor();
	private static MyProgressDialog dialog;

	private static final String FILE_NAME = "accountsAndPasswords";
	private Map<String, String> accountPasswordMap = new TreeMap<String, String>();
	private List<String> accounts = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		context = this;
		cache = new Cache(Login.this);
		dialog = new MyProgressDialog(context);
		countET = (AutoCompleteTextView) this.findViewById(R.id.cardNumAuto);
		passwordET = (EditText) this.findViewById(R.id.et_passworld);

		readAccountsAndPasswords();

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, accounts);
		countET = (AutoCompleteTextView) findViewById(R.id.cardNumAuto);
		countET.setAdapter(arrayAdapter);
		countET.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String s = accounts.get(position);
				if (accountPasswordMap.containsKey(s)) {
					passwordET.setText(accountPasswordMap.get(s));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		}

		);

		passwordET = (EditText) findViewById(R.id.et_passworld);
		passwordET.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String s = countET.getText().toString();
				if (accountPasswordMap.containsKey(s)) {
					passwordET.setText(accountPasswordMap.get(s));
				}
				return false;
			}
		});

		// countET.setText(COUNT);
		// passwordET.setText(PSWD);
		// countET.clearFocus();
		// passwordET.clearFocus();
		loginButton = (Button) this.findViewById(R.id.loginButton);
		logupButton = (Button) this.findViewById(R.id.logupButton);
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkTestTime() == false) {
					Toast.makeText(context, "使用期已过,不能再使用", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (v == loginButton) {
					dialog.show();
					loginButton.setEnabled(false);// 登录时不可再按登录
					exec.execute(new Loginer());
				} else if (v == logupButton) {
					Intent intent = new Intent(Login.this, Logup.class);
					startActivity(intent);
				}
			}

		};
		loginButton.setOnClickListener(listener);
		logupButton.setOnClickListener(listener);

	}

	private boolean checkTestTime() {
		try {
			DateFormat formater = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date stopDate = formater.parse("2014/3/15 11:59:59");
			Date current = new Date();
			if (current.before(stopDate))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	class Loginer implements Runnable {

		@Override
		public void run() {
			try {
				login();
				handler.obtainMessage(ENABLED, 0, -1).sendToTarget();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_TOAST:
				dialog.cancel();
				Toast.makeText(context, msg.getData().getString(TOAST),
						Toast.LENGTH_SHORT).show();
				loginButton.setEnabled(true);
				break;

			}
		}
	};

	private void login() throws JSONException {
		String count = countET.getText().toString();
		String password = passwordET.getText().toString();
		JSONObject result = WebService.login(count, password);
		String statusMessage;
		if (result == null) {
			statusMessage = "网络异常...";
//			Intent intent = new Intent(Login.this, MainUi.class);
//			startActivity(intent);

//			finish();
		} else {
			int status = result.getInt(WebService.STATUS_CODE);
			statusMessage = "登录成功！";
			if (status == WebService.OK) {
				if (!accountPasswordMap.containsKey(count)) {
					writeAccountAndPassword();
				}
				cache.saveUserId(count);// 保存用户id
				JSONObject userInfo = result.getJSONObject(WebService.DATA);
				cache.saveUserInfo(userInfo);
				Intent intent = new Intent(Login.this, MainUi.class);
				startActivity(intent);
				finish();
			} else {
				statusMessage = "用户名或密码错误！";
			}
		}
		Message Message = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, statusMessage);
		Message.setData(bundle);
		handler.sendMessage(Message);
	}

	/**
	 * Read accounts and passwords from file and store
	 * them in a map
	 */
	private void readAccountsAndPasswords() {
		try {
			FileInputStream fis = openFileInput(FILE_NAME);
			byte[] buffer = new byte[1024];
			int count = 0;
			StringBuilder stringBuilder = new StringBuilder("");

			while ((count = fis.read(buffer)) > 0) {
				stringBuilder.append(new String(buffer, 0, count));
			}

			String[] lines = stringBuilder.toString().split("\n");
			for (int i = 0; i < lines.length; i++) {
				String[] accountAndPassword = lines[i].split(",");
				accountPasswordMap.put(accountAndPassword[0],
						accountAndPassword[1]);
			}

			// Get all entries into a set
			Set<Map.Entry<String, String>> entrySet = accountPasswordMap
					.entrySet();

			for (Map.Entry<String, String> entry : entrySet) {
				accounts.add(entry.getKey());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the account and password into the file in
	 * append way
	 */
	private void writeAccountAndPassword() {
		try {
			FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND);
			PrintStream ps = new PrintStream(fos);
			String account = countET.getText().toString();
			String password = passwordET.getText().toString();
			ps.println(account + "," + password);
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
