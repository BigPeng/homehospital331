package cn.younext;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.TextView;

import com.health.BaseActivity;

public class teleference extends BaseActivity {
	Button homeBtn;
	Button personBtn1;
	Button personBtn2;
	Button personBtn3;
	Button personBtn4;
	Button personBtn5;
	Button dialBtn1;
	Button dialBtn2;
	Button dialBtn3;
	Button dialBtn4;
	Button dialBtn5;

	// Button xiugaidialog_okBtn;
	// Button xiugaidialog_cancelBtn;
	// Button bodaBtn;
	// Button guaduanBtn;
	OnClickListener btnClick;
	OnClickListener xiugaibtnClick;
	// ListView contactorView;

	DatabaseHelper helper;
	SQLiteDatabase db;
	Cursor c;

	TextView user;
	int userid;
	String username;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.teleference);
		
		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			userid = extra.getInt("userid");
			username = extra.getString("username");
			username = username == null ? "" : username;
			// Toast.makeText(myhealth.this,username,
			// Toast.LENGTH_LONG).show();
			// Log.v("userid_Myhealth",
			// String.valueOf(userid));
			// Log.v("username_myhealth", username);
		}

		user = (TextView) findViewById(R.id.teleference_user);
		user.setText(getString(R.string.myhealth_Welcome) + username);

		homeBtn = (Button) findViewById(R.id.teleference_homeBtn);
		personBtn1 = (Button) findViewById(R.id.teleference_personBtn1);
		personBtn2 = (Button) findViewById(R.id.teleference_personBtn2);
		personBtn3 = (Button) findViewById(R.id.teleference_personBtn3);
		personBtn4 = (Button) findViewById(R.id.teleference_personBtn4);
		personBtn5 = (Button) findViewById(R.id.teleference_personBtn5);
		dialBtn1 = (Button) findViewById(R.id.teleference_dialBtn1);
		dialBtn2 = (Button) findViewById(R.id.teleference_dialBtn2);
		dialBtn3 = (Button) findViewById(R.id.teleference_dialBtn3);
		dialBtn4 = (Button) findViewById(R.id.teleference_dialBtn4);
		dialBtn5 = (Button) findViewById(R.id.teleference_dialBtn5);

		helper = new DatabaseHelper(teleference.this,
				DatabaseHelper.DATABASE_NAME, null, DatabaseHelper.Version);
		db = helper.getWritableDatabase();// 打开数据库

		/*
		 * c=db.query("usermanage",
		 * null,DatabaseHelper.ID+"="+userid,null,
		 * null,null,"_id desc",null); c.moveToNext();
		 * username=c.getString(1);
		 * spinnertext.setText(username);
		 */

		// c=db.query("usermanage", null, null, null,
		// null,null,"_id asc",null);//用户名字查询
		c = db.query(DatabaseHelper.PHONETABLE, null, null, null, null, null,
				"_id asc", null);
		c.moveToNext();
		personBtn1.setText(c.getString(1));
		c.moveToNext();
		personBtn2.setText(c.getString(1));
		c.moveToNext();
		personBtn3.setText(c.getString(1));
		c.moveToNext();
		personBtn4.setText(c.getString(1));
		c.moveToNext();
		personBtn5.setText(c.getString(1));

		// xiugaidialog_okBtn=(Button)findViewById(R.id.xiugaidialog_okBtn);
		// xiugaidialog_cancelBtn=(Button)findViewById(R.id.xiugaidialog_cancelBtn);

		// bodaBtn=(Button)findViewById(R.id.teleference_bodaBtn);
		// guaduanBtn=(Button)findViewById(R.id.teleference_guaduanBtn);
		// contactorView=(ListView)findViewById(R.id.contactor);
		btnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (v == homeBtn) {
					teleference.this.finish();
				} else if (v == dialBtn1) {
					dialBtn1.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated
							// method stub
							c = db.query(DatabaseHelper.PHONETABLE, null,
									"_id=1", null, null, null, "_id asc", null);
							c.moveToNext();
							Intent i = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel://" + c.getString(2)));
							startActivity(i);
						}
					});

				} else if (v == dialBtn2) {
					dialBtn2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated
							// method stub
							c = db.query(DatabaseHelper.PHONETABLE, null,
									"_id=2", null, null, null, "_id asc", null);
							c.moveToNext();
							Intent i = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel://" + c.getString(2)));
							startActivity(i);
						}
					});

				} else if (v == dialBtn3) {
					dialBtn3.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated
							// method stub
							c = db.query(DatabaseHelper.PHONETABLE, null,
									"_id=3", null, null, null, "_id asc", null);
							c.moveToNext();
							Intent i = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel://" + c.getString(2)));
							startActivity(i);
						}
					});

				} else if (v == dialBtn4) {
					dialBtn4.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated
							// method stub
							c = db.query(DatabaseHelper.PHONETABLE, null,
									"_id=4", null, null, null, "_id asc", null);
							c.moveToNext();
							Intent i = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel://" + c.getString(2)));
							startActivity(i);
						}
					});

				} else if (v == dialBtn5) {
					dialBtn5.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated
							// method stub
							c = db.query(DatabaseHelper.PHONETABLE, null,
									"_id=5", null, null, null, "_id asc", null);
							c.moveToNext();
							Intent i = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel://" + c.getString(2)));
							startActivity(i);
						}
					});

				} else if (v == personBtn1) {
					Intent i = new Intent();
					i.putExtra("_id", "1");
					i.setClass(teleference.this, teleference_xiugaidialog.class);
					startActivityForResult(i, 1);
					/*
					 * LayoutInflater factory =
					 * LayoutInflater
					 * .from(teleference.this); final
					 * View xiugaidialog =
					 * factory.inflate
					 * (R.layout.teleference_xiugaidialog
					 * , null); final AlertDialog
					 * dlg=new
					 * AlertDialog.Builder(teleference
					 * .this)
					 * //dlg=new(Dialog)findViewById
					 * (R.layout
					 * .teleference_xiugaidialog);
					 * .setView(xiugaidialog)
					 * .setIcon(R
					 * .drawable.xiugaidialog_icon)
					 * .setTitle("请输入")
					 * 
					 * 
					 * .setPositiveButton("确定", new
					 * DialogInterface
					 * .OnClickListener() {
					 * 
					 * @Override public void
					 * onClick(DialogInterface dialog,
					 * int which) { // TODO
					 * Auto-generated method stub
					 * 
					 * } }) .setNegativeButton("取消",
					 * new
					 * DialogInterface.OnClickListener
					 * () {
					 * 
					 * @Override public void
					 * onClick(DialogInterface dialog,
					 * int which) { // TODO
					 * Auto-generated method stub
					 * 
					 * } }) .create(); dlg.show();
					 */
				} else if (v == personBtn2) {
					Intent i = new Intent();
					i.putExtra("_id", "2");
					i.setClass(teleference.this, teleference_xiugaidialog.class);
					startActivityForResult(i, 1);
				} else if (v == personBtn3) {
					Intent i = new Intent();
					i.putExtra("_id", "3");
					i.setClass(teleference.this, teleference_xiugaidialog.class);
					startActivityForResult(i, 1);
				} else if (v == personBtn4) {
					Intent i = new Intent();
					i.putExtra("_id", "4");
					i.setClass(teleference.this, teleference_xiugaidialog.class);
					startActivityForResult(i, 1);
				} else if (v == personBtn5) {
					Intent i = new Intent();
					i.putExtra("_id", "5");
					i.setClass(teleference.this, teleference_xiugaidialog.class);
					startActivityForResult(i, 1);
				}
				// else if(v==bodaBtn){
				// }
				// else if(v==guaduanBtn){
				// }
				else {
				}
			}
		};
		homeBtn.setOnClickListener(btnClick);
		personBtn1.setOnClickListener(btnClick);
		personBtn2.setOnClickListener(btnClick);
		personBtn3.setOnClickListener(btnClick);
		personBtn4.setOnClickListener(btnClick);
		personBtn5.setOnClickListener(btnClick);
		dialBtn1.setOnClickListener(btnClick);
		dialBtn2.setOnClickListener(btnClick);
		dialBtn3.setOnClickListener(btnClick);
		dialBtn4.setOnClickListener(btnClick);
		dialBtn5.setOnClickListener(btnClick);

		// bodaBtn.setOnClickListener(btnClick);
		// guaduanBtn.setOnClickListener(btnClick);

		// xueyaList=(ListView)findViewById(R.id.xueyarecord);
		// helper = new
		// DatabaseHelper(myhealth_testrecord.this,
		// DatabaseName, null,Version);
		// db = helper.getWritableDatabase();//打开数据库
		// Uri
		// uri=Uri.parse("content://contacts/people");

		// c =
		// getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,
		// null, null, null);

		// c.moveToNext();

		// c=db.query("xueya", null, null, null,
		// null,null,"_id desc",null);//机场名字查询
		// c.moveToNext();
		// Log.v("before executing",c.getString(3));
		// c.moveToFirst();
		// String
		// name=c.getString(c.getColumnIndexOrThrow(Contacts.People.NAME));
		// TextView
		// text=(TextView)findViewById(R.id.contactorText);
		// text.setText(name);

		/*
		 * ListAdapter adapter = new
		 * SimpleCursorAdapter(teleference.this,
		 * R.layout.simple_list_item_contactor, c, new
		 * String[] {"DISPLAY_NAME"}, new
		 * int[]{R.id.contactor});
		 * 
		 * contactorView.setAdapter(adapter);
		 * 
		 * contactorView.setOnItemClickListener(new
		 * OnItemClickListener(){
		 * 
		 * @Override public void
		 * onItemClick(AdapterView<?> arg0, View arg1,
		 * int arg2, long arg3) { Cursor c1=c;
		 * c1.moveToPosition(arg2); String
		 * telename=c1.getString
		 * (c.getColumnIndexOrThrow("DISPLAY_NAME"));
		 * String contactId =
		 * c1.getString(c1.getColumnIndex
		 * (ContactsContract.Contacts._ID)); String
		 * hasPhone =
		 * c1.getString(c1.getColumnIndex(ContactsContract
		 * .Contacts.HAS_PHONE_NUMBER)); c1.close();
		 * //c.close(); Log.v("name",telename);
		 * if(hasPhone.compareTo("1") == 0){ Cursor
		 * phones =
		 * getContentResolver().query(ContactsContract
		 * .CommonDataKinds
		 * .Phone.CONTENT_URI,null,ContactsContract
		 * .CommonDataKinds.Phone.CONTACT_ID +" = "+
		 * contactId,null, null); phones.moveToFirst();
		 * String telenumber=
		 * phones.getString(phones.getColumnIndex
		 * (ContactsContract
		 * .CommonDataKinds.Phone.NUMBER));
		 * phones.close(); //Log.v("tele",telenumber);
		 * Intent i=new
		 * Intent(Intent.ACTION_DIAL,Uri.parse
		 * ("tel://"+telenumber)); Intent ii=new
		 * Intent(
		 * "android.intent.action.CALL",Uri.parse
		 * ("tel://"+telenumber)); startActivity(ii);
		 * 
		 * }
		 * 
		 * 
		 * } }); Log.v("before executing",
		 * "afterexcute");//适配器绑定 //
		 * setContentView(R.id.tab1); //显示机场列表
		 * 
		 * //c.close(); //db.close();
		 */

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				c = db.query(DatabaseHelper.PHONETABLE, null, null, null, null,
						null, "_id asc", null);
				c.moveToNext();
				personBtn1.setText(c.getString(1));
				c.moveToNext();
				personBtn2.setText(c.getString(1));
				c.moveToNext();
				personBtn3.setText(c.getString(1));
				c.moveToNext();
				personBtn4.setText(c.getString(1));
				c.moveToNext();
				personBtn5.setText(c.getString(1));
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

			teleference.this.finish();

			return true;

		}
		return super.onKeyDown(keyCode, event);
	}
}
