package com.health.measurement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.younext.R;

import com.health.BaseActivity;

public class Measurement extends BaseActivity {
	Button homeButton;

	Button gluButton;
	Button boBotton;
	Button bpButton;

	Button tempButton;// ÌåÎÂ°´Å¥
	Button uaButton;// ÄòËá°´Å¥
	Button cholButton;// ×Üµ¨¹Ì´¼°´Å¥
	Button urineButton;// ÄòÒº·ÖÎö°´Å¥
	Button deviceMeasureButton;
	Button handinputButton;
	int userid;
	String username;
	OnClickListener onClickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.measurement);
		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			userid = extra.getInt("userid");
			username = extra.getString("username");
		}
		handinputButton = (Button) findViewById(R.id.test_handbtn);
		deviceMeasureButton = (Button) this.findViewById(R.id.test_machinebtn);
		deviceMeasureButton.setSelected(true);
		handinputButton.setSelected(false);
		homeButton = (Button) findViewById(R.id.test_homebutton);
		boBotton = (Button) findViewById(R.id.test_xueyang);
		gluButton = (Button) findViewById(R.id.test_xuetang);

		tempButton = (Button) this.findViewById(R.id.test_tiwen);
		uaButton = (Button) this.findViewById(R.id.test_niaosuan);
		cholButton = (Button) this.findViewById(R.id.test_zongdanguchun);
		urineButton = (Button) this.findViewById(R.id.test_niaoyifenxi);
		bpButton = (Button) findViewById(R.id.test_bp);		
		onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if (v == homeButton) {
					Measurement.this.finish();
				} else if (v == bpButton || v == boBotton || v == tempButton) {
					intent.setClass(Measurement.this, MeasureBp.class);
					startActivityForResult(intent, 1);
				} else if (v == handinputButton) {
					intent.setClass(Measurement.this, HandInputMeasure.class);
					intent.putExtra("userid", userid);
					intent.putExtra("username", username);
					Log.v("handinputbtn", "press");
					startActivityForResult(intent, 1);
				} else if (v == gluButton || uaButton == v || cholButton == v) {
					intent.setClass(Measurement.this, MeasureGlucose.class);
					startActivityForResult(intent, 1);
				} else if (urineButton == v) {
					intent.setClass(Measurement.this, MeasureUrine.class);
					startActivityForResult(intent, 1);
				}
			}
		};
		homeButton.setOnClickListener(onClickListener);
		boBotton.setOnClickListener(onClickListener);
		handinputButton.setOnClickListener(onClickListener);
		gluButton.setOnClickListener(onClickListener);
		bpButton.setOnClickListener(onClickListener);
		tempButton.setOnClickListener(onClickListener);
		uaButton.setOnClickListener(onClickListener);
		cholButton.setOnClickListener(onClickListener);
		urineButton.setOnClickListener(onClickListener);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				Measurement.this.finish();
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
			Measurement.this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
