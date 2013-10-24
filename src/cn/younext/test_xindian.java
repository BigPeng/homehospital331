package cn.younext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.TextView;

public class test_xindian extends Activity{
	Button homeBtn;
	Button returnBtn;
	Button nextBtn;
	OnClickListener btnClick;
	BatteryView myview;

	TextView user;
	int userid;
	String username;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.test_xindian);
        
        DigitalClock myclock=(DigitalClock)findViewById(R.id.test_xindian_clock);
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

        user=(TextView)findViewById(R.id.test_xindian_user);
        user.setText(getString(R.string.myhealth_Welcome)+username);
        
        homeBtn = (Button)findViewById(R.id.test_xindian_homeBtn);
        returnBtn=(Button)findViewById(R.id.test_xindian_returnBtn);
        nextBtn=(Button)findViewById(R.id.test_xindian_nextBtn);
        btnClick= new OnClickListener(){
    		@Override
    		public void onClick(View v) {
                
        		if(v==homeBtn){      		
    			    test_xindian.this.setResult(RESULT_OK);
    		        test_xindian.this.finish();
        		}
        		else if(v==returnBtn){
        			
        			
       			 FileOutputStream out;
  			    final String FILE_PATH="/sys/class/";
  			    final String LEDFILE="ledctrl";
  			    File ledfile;
  			    try{
  			    	ledfile= new File(FILE_PATH ,LEDFILE);
  			    	

  	               	 //д�롰edittext01���ļ�
  			    	
  	 
  	               	 out = new FileOutputStream(ledfile);
  	               	 //String test=String.valueOf(userid);
  	               	 String test=Integer.toString(0);
  	               	 out.write(test.getBytes());
  	               	 out.close();
  	               	 Log.v("write", "write");
  	               	
  	               	 
  	               } catch (IOException e){
  	               	 //��������Ϣ��ӡ��Logcat
  	               	 Log.e("writefile", e.toString());
  	               //	drug_calendar.this.finish();
  	                } 
        			
        			
        			
        			
        			test_xindian.this.finish();	
        		}
        		else if(v==nextBtn){
        			Intent i=new Intent();
        			i.setClass(test_xindian.this, test_xindian2.class);
        			i.putExtra("userid", userid);
    			    i.putExtra("username", username);
        			startActivityForResult(i,1);
        		}
        		//else if(v==guaduanBtn){		
        		//}
        		else{  			
        		}	
    		}
        };
        homeBtn.setOnClickListener(btnClick);
        returnBtn.setOnClickListener(btnClick);
        nextBtn.setOnClickListener(btnClick);

    }
    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
       switch (requestCode) { 
       case 1: 
           if (resultCode == RESULT_OK) 
           { 
        	   test_xindian.this.setResult(RESULT_OK);
               test_xindian.this.finish();
           } 
           break; 
       default: 
           break; 
       } 
    }
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
        if (keyCode == KeyEvent.KEYCODE_MENU) {
        	   
        	test_xindian.this.setResult(RESULT_OK);
	        test_xindian.this.finish();
		

            return true;

        }
        return super.onKeyDown(keyCode, event);
    }
}
