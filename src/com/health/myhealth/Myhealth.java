package com.health.myhealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import cn.younext.R;
import com.health.BaseActivity;

public class Myhealth extends BaseActivity {
	Button homeBtn;
	Button healthreport;
	Button testrecord;

	// private static Button lastedRecordButton;
	OnClickListener btnClick;
	TextView user;
	int userid;
	String username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myhealth);
		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			userid = extra.getInt("userid");
			username = extra.getString("username");
			username = username == null ? "" : username;
		}

		user = (TextView) findViewById(R.id.myhealth_user);
		user.setText(getString(R.string.myhealth_Welcome) + " " + username);

		homeBtn = (Button) findViewById(R.id.homebutton);
		healthreport = (Button) findViewById(R.id.myhealth_healthreport);
		testrecord = (Button) findViewById(R.id.myhealth_testrecord);
		btnClick = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				if (v == homeBtn) {
					Myhealth.this.finish();
				} else if (v == testrecord) {
					i.setClass(Myhealth.this, TableRecord.class);
					i.putExtra("userid", userid);
					i.putExtra("username", username);
					startActivityForResult(i, 1);
				} else if (v == healthreport) {
					i.setClass(Myhealth.this, LatestMeasure.class);
					i.putExtra("userid", userid);
					i.putExtra("username", username);
					startActivityForResult(i, 1);
				}
			}

		};
		homeBtn.setOnClickListener(btnClick);
		healthreport.setOnClickListener(btnClick);
		testrecord.setOnClickListener(btnClick);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {

				Myhealth.this.finish();
			}
			break;
		default:
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

			Myhealth.this.finish();

			return true;

		}
		return super.onKeyDown(keyCode, event);
	}
}
