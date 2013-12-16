package com.health;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.younext.R;

import com.health.bluetooth.BluetoothService;
import com.health.database.Cache;
import com.health.util.MyProgressDialog;
import com.health.web.WebService;

public class Login extends BaseActivity {
	private static final String COUNT = "330772197805283758";
	private static final String PSWD = "abcd4321";
	protected static final int MESSAGE_TOAST = 10000;
	private static final String TOAST = "toast";
	private static final int ENABLED = 10001;
	private static EditText countET;
	private static EditText passwordET;
	private static Button loginButton;
	private static Button logupButton;
	private static Context context;
	private static ExecutorService exec = Executors.newSingleThreadExecutor();
	private static MyProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		context = this;
		cache = new Cache(Login.this);
		dialog = new MyProgressDialog(context);
		countET = (EditText) this.findViewById(R.id.cardNumAuto);
		passwordET = (EditText) this.findViewById(R.id.et_passworld);

		countET.setText(COUNT);
		passwordET.setText(PSWD);
		countET.clearFocus();
		passwordET.clearFocus();
		loginButton = (Button) this.findViewById(R.id.loginButton);
		logupButton = (Button) this.findViewById(R.id.logupButton);
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == loginButton) {
					dialog.show();
					loginButton.setEnabled(false);// ��¼ʱ�����ٰ���¼
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
			statusMessage = "�����쳣...";
			// ����ʱ����Ҫ��¼
			Intent intent = new Intent(Login.this, MainUi.class);
			startActivity(intent);
			finish();
		}

		else {
			int status = result.getInt(WebService.STATUS_CODE);
			statusMessage = "��¼�ɹ���";

			if (status == WebService.OK) {
				cache.saveUserId(count);// �����û�id
				JSONObject userInfo = result.getJSONObject(WebService.DATA);
				cache.saveUserInfo(userInfo);
				Intent intent = new Intent(Login.this, MainUi.class);
				startActivity(intent);
				finish();
			} else {
				statusMessage = "�û������������";
			}
		}
		Message Message = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, statusMessage);
		Message.setData(bundle);
		handler.sendMessage(Message);
	}
}
