package cn.younext;

import java.util.Calendar;


import org.hitsz.libfat.*;

import com.android.etcomm.sdk.DeviceDisconnectedException;

import android.app.Activity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.etcomm.sdk.BluetoothAdapterNotFoundException;
import com.android.etcomm.sdk.CreateSocketFailedException;
import com.android.etcomm.sdk.CreateStreamFailedException;
import com.android.etcomm.sdk.MeasureResult;
import com.android.etcomm.sdk.OnMeasureListener;
import com.android.etcomm.sdk.ServiceDiscoveryFailedException;
import com.android.etcomm.sdk.UnableToStartServiceDiscoveryException;
import com.android.etcomm.sdk.libbp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class test_zhifang1 extends Activity{

	Button homeBtn;
	Button returnBtn;
	BatteryView myview;
	EditText zhifanglv;

	OnClickListener btnClick;
	
	TextView user;
	String username;
	int userid;
	String cardNum;
	
	Cursor c;
	DatabaseHelper helper;
	SQLiteDatabase db;
	
	private LibFat fr = new LibFat();
	private String mac;
	
	
	private int mHour;
	private int mMinute;
	private int mYear;
	private int mMonth;
	private int mDay;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);       
        setContentView(R.layout.test_zhifang1);
        myview=(BatteryView)findViewById(R.id.batteryview);
        registerReceiver(mIntentReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
           
        Bundle extra=getIntent().getExtras();
        if(extra!=null)
        {
        	userid=extra.getInt("userid");
        	username=extra.getString("username");
        	//Toast.makeText(myhealth.this,username, Toast.LENGTH_LONG).show();
        	//Log.v("userid_Myhealth", String.valueOf(userid));
        	//Log.v("username_myhealth", username);
        }

        user=(TextView)findViewById(R.id.test_zhifang1_user);
        user.setText(getString(R.string.myhealth_Welcome)+username);
        
        homeBtn = (Button)findViewById(R.id.test_zhifang1_homeBtn);
        returnBtn=(Button)findViewById(R.id.test_zhifang1_returnBtn);
        zhifanglv=(EditText)findViewById(R.id.test_zhifang1_edittext);
        
        
        helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
 	    db = helper.getWritableDatabase();//�����ݿ�
 	    c=db.query(DatabaseHelper.BLUETOOTH_MACTABLE, null, DatabaseHelper.ID+"=4", null, null, null,DatabaseHelper.ID+" desc",null);
 	    c.moveToNext();
 	    mac = c.getString(1);
 	    db.close();
 	    
 	    helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
	    db = helper.getWritableDatabase();//�����ݿ�
	    c=db.query(DatabaseHelper.USER_MANAGE, null, DatabaseHelper.ID+"="+userid, null, null, null,DatabaseHelper.ID+" desc",null);
	    c.moveToNext();
	    cardNum = c.getString(2);
	    db.close();

        btnClick= new OnClickListener(){
    		@Override
    		public void onClick(View v) {

        		if(v==homeBtn){    
        			if (fr.isOpen())
        				fr.closeDevice();
    			    test_zhifang1.this.setResult(RESULT_OK);
    		        test_zhifang1.this.finish();
        		}
        		else if(v==returnBtn){
        			if (fr.isOpen())
        				fr.closeDevice();
        			test_zhifang1.this.finish();	
        		}

        		else{  			
        		}	
    		}
        };
        homeBtn.setOnClickListener(btnClick);
        returnBtn.setOnClickListener(btnClick);
        fr.setOnMeasureListener(listener);
		 if(!fr.isOpen()){
	        	if(fr.OpenDevice(mac)){
	        	}
	     }

    }
    
    
    
    
    private org.hitsz.libfat.OnMeasureListener listener = new org.hitsz.libfat.OnMeasureListener(){

		@Override
		public void onException(Exception exception) {
			// TODO Auto-generated method stub	

			if (fr.isOpen())
				fr.closeDevice();
		
			if (exception instanceof BluetoothAdapterNotFoundException) {
				// δ��⵽����������
				Toast.makeText(test_zhifang1.this, R.string.BluetoothAdapterNotFound, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof UnableToStartServiceDiscoveryException) {
				// �ֻ�����δ����
				Toast.makeText(test_zhifang1.this, R.string.UnableToStartServiceDiscovery, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof CreateSocketFailedException) {
				// ���ֻ���������ʧ��
				Toast.makeText(test_zhifang1.this, R.string.CreateSocketFailed, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof ServiceDiscoveryFailedException) {
				// Ѫѹ������δ����
				Toast.makeText(test_zhifang1.this, R.string.ServiceDiscoveryFailed, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof CreateStreamFailedException) {
				// ���ֻ���������ͨѶʧ��
				Toast.makeText(test_zhifang1.this, R.string.CreateStreamFailed, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof DeviceDisconnectedException) {
				// �豸�����ѶϿ��쳣
				Toast.makeText(test_zhifang1.this, R.string.DeviceDisconnected, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			}
		
			if(exception instanceof DeviceDisconnectedException){
				//����֬�����ڶ�����ʧ��ʱΨһ�׳����쳣
				//test_zhifang1.this.result.setText("����������쳣");
				Toast.makeText(test_zhifang1.this, "����������쳣", Toast.LENGTH_LONG).show();
				
			}
			//Toast.makeText(MainActivity.this, R.string.error , Toast.LENGTH_LONG).show();
		}



		@Override
		public void onMeasure(FatMeasureResult fr) {

//			StringBuilder sb = new StringBuilder();
//			sb.append("�û�id"+String.valueOf(fr.userid) +";");//1-9
//			sb.append("�û����"+String.valueOf(fr.height) +";");
//			sb.append("�û�����"+String.valueOf(fr.weight) +";");
//			sb.append("�û�����"+String.valueOf(fr.age) +";");
//			sb.append("�û��Ա�"+String.valueOf(fr.gender) +";");//1�У�0Ů
//			//�û��Ĳ���ʱ�����û�ж���ʱ����˽����ȡϵͳ�ĵ�ǰʱ��
//			sb.append("�û�����ʱ��"+String.valueOf(fr.year) +":"+String.valueOf(fr.month)+":"+String.valueOf(fr.date)+":"+String.valueOf(fr.hours)+" ;"+String.valueOf(fr.minutes));
//			
//			sb.append("�û�֬������"+String.valueOf(fr.FatContent)+";");//float���ͣ���18.7��ʾ18.7%
//			sb.append("�û�bmiֵ"+String.valueOf(fr.BMI));//ͬ��
//			sb.append("�û�bmrֵ"+String.valueOf(fr.BMR));
//			sb.append("�û�BMIResult" +String.valueOf(fr.BMIResult));//����ֵ��1��ƫ�ͣ�2����׼��3��ƫ�ߣ�4�ߣ� 
//			sb.append("�û�fatresult"+String.valueOf(fr.FatResult));//����ֵ 1�����ݣ�2����׼��3�������Է��֣�4 �����Է���/��׳��5 ����
//			//MainActivity.this.result.setText(sb.toString());
//			//Toast.makeText(MainActivity.this, R.string.openright , Toast.LENGTH_LONG).show();
			final Calendar c = Calendar.getInstance();
	        mYear = c.get(Calendar.YEAR); //��ȡ��ǰ���

	        mMonth = c.get(Calendar.MONTH)+1;//��ȡ��ǰ�·�
	        if(mMonth==13){
	        	mMonth=1;	        	
	        }
	        mDay = c.get(Calendar.DAY_OF_MONTH);//��ȡ��ǰ�·ݵ����ں���
	        mHour = c.get(Calendar.HOUR_OF_DAY);//��ȡ��ǰ��Сʱ��
	        mMinute = c.get(Calendar.MINUTE);//��ȡ��ǰ�ķ�����
	        helper = new DatabaseHelper(test_zhifang1.this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
	        db = helper.getWritableDatabase();
	        db.execSQL("INSERT INTO "+DatabaseHelper.ZHIFANGTABLE+
					"("+DatabaseHelper.ZHIFANGLV+","+DatabaseHelper.YEAR+","+DatabaseHelper.MONTH+","+DatabaseHelper.DAY+","+DatabaseHelper.HOUR+","+DatabaseHelper.MINUTE+","+DatabaseHelper.USER_ID+") VALUES "+"("+fr.FatContent*100+","+mYear+","+mMonth+","+mDay+","+mHour+","+mMinute+","+userid+")");
			db.close();
			zhifanglv.setText(String.valueOf(fr.FatContent*100)+"%");
			
			
			edu.hitsz.android.Client client = new edu.hitsz.android.Client();
			try{
				client.insertFat(cardNum, String.valueOf(fr.FatContent*100));
			}catch(Exception e){
				Toast.makeText(test_zhifang1.this, R.string.NetworkDisconneted, Toast.LENGTH_LONG).show();
			}
			
			
		
			
		}

   	
    };
    protected void onResume()
    {
      super.onResume();    
      // ע����Ϣ������
      registerReceiver(mIntentReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
   
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

   	 public void onReceive(Context context, Intent intent){
   		 
            String action = intent.getAction();
          
            //Ҫ�����ǲ�������Ҫ�������Ϣ
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

           	 //���״̬
           	 if(intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)==BatteryManager.BATTERY_STATUS_CHARGING){
           		myview.tag=0;
           	
           		 Log.v("����",Integer.toString(intent.getIntExtra("level", 0)));
           		 myview.invalidate();
           	 }
           	 else if(intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)==BatteryManager.BATTERY_STATUS_NOT_CHARGING){
           		 myview.tag = 1;
           		 myview.dianliang=intent.getIntExtra("level", 0);
           		 myview.invalidate();
          
           	 }
              

            }
            else{
           		 myview.tag = 1;
          		 myview.dianliang=intent.getIntExtra("level", 0);
          		 myview.invalidate();

          	 }
   	 }
   	 
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (fr.isOpen())
				fr.closeDevice();
			test_zhifang1.this.finish();

            return true;

        }
        else if(keyCode == KeyEvent.KEYCODE_MENU){
            
			if (fr.isOpen())
				fr.closeDevice();
		    test_zhifang1.this.setResult(RESULT_OK);
	        test_zhifang1.this.finish();
		
        }
        return super.onKeyDown(keyCode, event);
    } 


}
