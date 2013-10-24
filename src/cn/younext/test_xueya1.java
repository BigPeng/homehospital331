package cn.younext;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


import cn.younext.test_xindian2.FormFile;

import com.android.etcomm.sdk.BluetoothAdapterNotFoundException;
import com.android.etcomm.sdk.CreateSocketFailedException;
import com.android.etcomm.sdk.CreateStreamFailedException;
import com.android.etcomm.sdk.DeviceDisconnectedException;
import com.android.etcomm.sdk.MeasureResult;
import com.android.etcomm.sdk.OnMeasureListener;
import com.android.etcomm.sdk.ServiceDiscoveryFailedException;
import com.android.etcomm.sdk.UnableToStartServiceDiscoveryException;
import com.android.etcomm.sdk.libbp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class test_xueya1 extends Activity{
	Button homeBtn;
	Button returnBtn;
	OnClickListener btnClick;
	private libbp bp = new libbp();
	BatteryView myview;

	//private EditText txtDevice = null;
	private EditText txtSBP = null;
	private EditText txtDBP = null;
	private EditText txtPulse = null;

	TextView user;
	int userid;
	String username;
	String cardNum;
	
	
	//public static final String DatabaseName="xueyarecord";
	//public static final int Version = 1;
	Cursor c;
	DatabaseHelper helper;
	SQLiteDatabase db;
	
	
	private int mHour;
	private int mMinute;
	private int mYear;
	private int mMonth;
	private int mDay;
	
	private String mac;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);       
        setContentView(R.layout.test_xueya1);

        DigitalClock myclock=(DigitalClock)findViewById(R.id.test_xueya1_clock);
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

        user=(TextView)findViewById(R.id.test_xueya1_user);
        user.setText(getString(R.string.myhealth_Welcome)+username);
        
        homeBtn = (Button)findViewById(R.id.test_xueya1_homeBtn);
        returnBtn=(Button)findViewById(R.id.test_xueya1_returnBtn);
        
        helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
 	    db = helper.getWritableDatabase();//�����ݿ�
 	    c=db.query(DatabaseHelper.BLUETOOTH_MACTABLE, null, DatabaseHelper.ID+"=1", null, null, null,DatabaseHelper.ID+" desc",null);
 	    c.moveToNext();
 	    mac = c.getString(1);
 	    //Log.e("mac: =======>>",mac);
 	    mac = "00:06:66:42:AC:FC";
 	    db.close();
 	    
 	    helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
	    db = helper.getWritableDatabase();//�����ݿ�
	    c=db.query(DatabaseHelper.USER_MANAGE, null, DatabaseHelper.ID+"="+userid, null, null, null,DatabaseHelper.ID+" desc",null);
	    c.moveToNext();
	    cardNum = c.getString(2);
	    db.close();
        
       
        
      /*  try{
        	String DATABASE_DIR_PATH=this.getDir("databases", MODE_WORLD_WRITEABLE).getParentFile().getAbsolutePath()+"/databases"; //�õ����ݿ��ļ���Ŀ¼��ϵͳ�еľ���·��
        	String databasefilename=DATABASE_DIR_PATH+"/"+"xueyarecord";  //�õ����ݿ���ϵͳ�еľ���·��
        
        	File databases=new File(DATABASE_DIR_PATH);
        	if(!databases.exists())
        		databases.mkdir();  //������ݿ�Ŀ¼�����ڣ����½�һ��
        	Log.v("before executing", "before copying");
        	if(!(new File(databasefilename)).exists())
        	{
        		InputStream is=getResources().openRawResource(R.raw.xueyarecord);
        		FileOutputStream fos=new FileOutputStream(databasefilename);
        		byte buffer[]=new byte[8192];
        		int count=0;
        		while((count=is.read(buffer))>0)
        		{
        			fos.write(buffer, 0, count);
        		}

        		
        		fos.close();
        		is.close();
        		Log.v("good news", "can copy this file");
        	}
        }catch(Exception e){
        	
        }*/
        
        
        
        btnClick= new OnClickListener(){
    		@Override
    		public void onClick(View v) {

        		if(v==homeBtn){      
        			if (bp.isOpen())
        				bp.closeDevice();
    			    test_xueya1.this.setResult(RESULT_OK);
    		        test_xueya1.this.finish();
        		}
        		else if(v==returnBtn){
        			if (bp.isOpen())
        				bp.closeDevice();
        			test_xueya1.this.finish();	
        		}
        		//else if(v==guaduanBtn){		
        		//}
        		else{  			
        		}	
    		}
        };
        homeBtn.setOnClickListener(btnClick);
        returnBtn.setOnClickListener(btnClick);
        
		// ��ȡ�ؼ�����
		//txtDevice = (EditText) findViewById(R.id.bp_device);
		txtSBP = (EditText) findViewById(R.id.bp_sbp);
		txtDBP = (EditText) findViewById(R.id.bp_dbp);
		txtPulse = (EditText) findViewById(R.id.bp_pulse);
		
		
       /*FormFile[] xx=new FormFile[1];
		//xx[0]=new FormFile(this.getDir("files", MODE_WORLD_WRITEABLE).getParentFile().getAbsolutePath()+"/files/"+item.fileName, item.fileData, "form1", null);
		//Log.v("filedata",item.fileData.toString());
		Map<String, String> params=new HashMap<String, String>();
		params.put("form", "Ѫѹ");
		params.put("��ѹ", "120");
		params.put("��ѹ", "120");
		params.put("����", "120");
		params.put("��", "120");
		params.put("��", "120");
		params.put("��", "120");
		params.put("ʱ", "120");
		params.put("��", "120");
		

		//params.put("blood", "25");
		post("http://www.lkang.org/receive.aspx",params,xx);*/

		// ����Ѫѹ�����¼�������
		
		bp.setOnMeasure(measurer);
		if (!bp.isOpen()){
			
			bp.openDevice(mac);
		}
			

    }
    /**
	 * �����¼�������
	 */
	private OnMeasureListener measurer = new OnMeasureListener() {
		/**
		 * ���ղ�������¼�
		 * 
		 * @param result
		 *            �������
		 */
		@Override
		public void onMeasure(MeasureResult result) {
			txtSBP.setText(String.format("%1$d mmHg", result.SBP));
			txtDBP.setText(String.format("%1$d mmHg", result.DBP));
			txtPulse.setText(String.format("%1$d bpm/min", result.Pulse));
			
	        final Calendar c = Calendar.getInstance();
	        mYear = c.get(Calendar.YEAR); //��ȡ��ǰ���

	        mMonth = c.get(Calendar.MONTH)+1;//��ȡ��ǰ�·�
	        if(mMonth==13){
	        	mMonth=1;	        	
	        }
	        mDay = c.get(Calendar.DAY_OF_MONTH);//��ȡ��ǰ�·ݵ����ں���
	        mHour = c.get(Calendar.HOUR_OF_DAY);//��ȡ��ǰ��Сʱ��
	        mMinute = c.get(Calendar.MINUTE);//��ȡ��ǰ�ķ�����

			
		/*	helper = new DatabaseHelper(test_xueya1.this,DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);
			db = helper.getWritableDatabase();
			db.execSQL("INSERT INTO "+DatabaseHelper.TB_NAME+
					"("+DatabaseHelper.GAOYA+","+DatabaseHelper.DIYA+","
					+DatabaseHelper.XINLV+","+DatabaseHelper.YEAR+","+DatabaseHelper.MONTH+","+DatabaseHelper.DAY+","+DatabaseHelper.HOUR+","+DatabaseHelper.MINUTE+") VALUES "+"("+result.SBP+","
					+result.DBP+","+result.Pulse+","+mYear+","+mMonth+","+mDay+","+mHour+","+mMinute+")");
			db.close();*/
	        
	        helper = new DatabaseHelper(test_xueya1.this,DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);
			db = helper.getWritableDatabase();
			db.execSQL("INSERT INTO "+DatabaseHelper.XUEYATABLE+
					"("+DatabaseHelper.GAOYA+","+DatabaseHelper.DIYA+","
					+DatabaseHelper.YEAR+","+DatabaseHelper.MONTH+","+DatabaseHelper.DAY+","+DatabaseHelper.HOUR+","+DatabaseHelper.MINUTE+","+DatabaseHelper.USER_ID+") VALUES "+"("+result.SBP+","
					+result.DBP+","+mYear+","+mMonth+","+mDay+","+mHour+","+mMinute+","+userid+")");
			db.execSQL("INSERT INTO "+DatabaseHelper.MAILVTABLE+
					"("+DatabaseHelper.MAILV+","+DatabaseHelper.YEAR+","+DatabaseHelper.MONTH+","+DatabaseHelper.DAY+","+DatabaseHelper.HOUR+","+DatabaseHelper.MINUTE+","+DatabaseHelper.USER_ID+") VALUES "+"("+result.Pulse+","+mYear+","+mMonth+","+mDay+","+mHour+","+mMinute+","+userid+")");
			db.close();
					
			
			edu.hitsz.android.Client client = new edu.hitsz.android.Client();
			try {
				client.insertBPWithpulse(cardNum, String.valueOf(result.SBP), String.valueOf(result.DBP),String.valueOf(result.Pulse));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast.makeText(test_xueya1.this, R.string.NetworkDisconneted, Toast.LENGTH_LONG).show();
			}
	       /* FormFile[] xx=new FormFile[1];
			//xx[0]=new FormFile(this.getDir("files", MODE_WORLD_WRITEABLE).getParentFile().getAbsolutePath()+"/files/"+item.fileName, item.fileData, "form1", null);
			//Log.v("filedata",item.fileData.toString());
			Map<String, String> params=new HashMap<String, String>();
			params.put("form", "Ѫѹ");
			params.put("��ѹ", String.valueOf(result.SBP));
			params.put("��ѹ", String.valueOf(result.DBP));
			params.put("����", String.valueOf(result.Pulse));
			params.put("��", String.valueOf(mYear));
			params.put("��", String.valueOf(mMonth));
			params.put("��", String.valueOf(mDay));
			params.put("ʱ", String.valueOf(mHour));
			params.put("��", String.valueOf(mMinute));
			

			//params.put("blood", "25");
			try{
				post("http://219.223.239.143/receive.aspx",params,xx);				
			}

			catch(Exception e){
				Toast.makeText(test_xueya1.this, R.string.NetworkDisconneted, Toast.LENGTH_LONG).show();
				
			}*/
			//String uriAPI="http://219.223.239.143/revReport.aspx";
	    	//String queryString = "http://219.223.239.143/default.aspx?name=7&email=8"; 
	    	//change space in the url into "%20"*/
	    	//URL aURL = new URL(queryString.replace(" ", "%20"));*/
		}

		/**
		 * ���������쳣�¼�
		 * 
		 * @param exception
		 *            �쳣��Ϣ
		 */
		@Override
		public void onException(Exception exception) {
			if (bp.isOpen())
				bp.closeDevice();
		
			if (exception instanceof BluetoothAdapterNotFoundException) {
				// δ��⵽����������
				Toast.makeText(test_xueya1.this, R.string.BluetoothAdapterNotFound, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof UnableToStartServiceDiscoveryException) {
				// �ֻ�����δ����
				Toast.makeText(test_xueya1.this, R.string.UnableToStartServiceDiscovery, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof CreateSocketFailedException) {
				// ���ֻ���������ʧ��
				Toast.makeText(test_xueya1.this, R.string.CreateSocketFailed, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof ServiceDiscoveryFailedException) {
				// Ѫѹ������δ����
				Toast.makeText(test_xueya1.this, R.string.ServiceDiscoveryFailed, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof CreateStreamFailedException) {
				// ���ֻ���������ͨѶʧ��
				Toast.makeText(test_xueya1.this, R.string.CreateStreamFailed, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			} else if (exception instanceof DeviceDisconnectedException) {
				// �豸�����ѶϿ��쳣
				Toast.makeText(test_xueya1.this, R.string.DeviceDisconnected, Toast.LENGTH_LONG).show();
				//test_xueya1.this.finish();
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		// �ر��豸
		if (bp.isOpen())
			bp.closeDevice();
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (bp.isOpen())
				bp.closeDevice();
			test_xueya1.this.finish();

            return true;

        }
        else if(keyCode==KeyEvent.KEYCODE_MENU){
            
			if (bp.isOpen())
				bp.closeDevice();
		    test_xueya1.this.setResult(RESULT_OK);
	        test_xueya1.this.finish();
		
        }
        return super.onKeyDown(keyCode, event);
    } 
    
    
    
    
    
    
    
    
    
    public static String post(String actionUrl, Map<String, String> params, FormFile[] files) {  
        try {             
            String BOUNDARY = "---------7d4a6d158c9"; //���ݷָ���  
            String MULTIPART_FORM_DATA = "multipart/form-data";  
              
            URL url = new URL(actionUrl);  
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
            conn.setDoInput(true);//��������  
            conn.setDoOutput(true);//�������  
            conn.setUseCaches(false);//��ʹ��Cache  
            conn.setRequestMethod("POST");            
            conn.setRequestProperty("Connection", "Keep-Alive");  
            conn.setRequestProperty("Charset", "UTF-8");  
 
      
            StringBuilder sb = new StringBuilder();
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            xml.append("<params>");
              
            //�ϴ��ı��������֣���ʽ��ο�����  
            for (Map.Entry<String, String> entry : params.entrySet()) {//�������ֶ�����  
                //sb.append("--");  
                //sb.append(BOUNDARY);  
                //sb.append("\r\n");  
                //sb.append("Content-Disposition: form-data; name=\""+ entry.getKey() + "\"\r\n\r\n");  
                sb.append(entry.getValue());
                
                xml.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
                
                //sb.append("\r\n");  
            } 
            xml.append("</params>");
            
            //outStream.write(sb.toString().getBytes());//���ͱ��ֶ�����  
            byte[] xmlbyte = xml.toString().getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(xmlbyte.length));
            conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8"); 
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream()); 
            outStream.write(xmlbyte);
            //�ϴ����ļ����֣���ʽ��ο�����  
            /*for(FormFile file : files){  
                StringBuilder split = new StringBuilder();  
                //split.append("--");  
                //split.append(BOUNDARY);  
                //split.append("\r\n");  
                //split.append("Content-Disposition: form-data;name=\""+ file.getFormname()+"\";filename=\""+ file.getFilname() + "\"\r\n");  
                //split.append("Content-Type: "+ file.getContentType()+"\r\n\r\n");  
                //split.append("Content-Disposition: form-data;name=\""+ file.getFormname()+"\";filename=\""+ file.getFilname() + "\"");  
                //split.append("Content-Type: "+ file.getContentType());
                //outStream.write(split.toString().getBytes());  
                outStream.write(file.getData(), 0, file.getData().length);  
                //outStream.write("\r\n".getBytes());  
                Log.v("file", split.toString()+file.getData().toString());
                
            }  */
            //byte[] end_data = ("--" + BOUNDARY + "--\r\n").getBytes();//���ݽ�����־           
            //outStream.write(end_data);  
            outStream.flush();  
            int cah = conn.getResponseCode();  
            if (cah != 200) throw new RuntimeException("����urlʧ��");  
            InputStream is = conn.getInputStream();  
            Log.v("test", "wangye");
            int ch;  
            StringBuilder b = new StringBuilder();  
            while( (ch = is.read()) != -1 ){  
                b.append((char)ch);  
            }  
            outStream.close();  
            conn.disconnect();  
            Log.v("b", b.toString());
            return b.toString();  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }
    
    
    
    
    
    public class FormFile {  
        /* �ϴ��ļ������� */  
        private byte[] data;  
        /* �ļ����� */  
        private String filname;  
        /* ���ֶ�����*/  
        private String formname;  
        /* �������� */  
        private String contentType = "application/octet-stream"; //��Ҫ������ص�����  
          
        public FormFile(String filname, byte[] data, String formname, String contentType) {  
            this.data = data;  
            this.filname = filname;  
            this.formname = formname;  
            if(contentType!=null) this.contentType = contentType;  
        }  
      
        public byte[] getData() {  
            return data;  
        }  
      
        public void setData(byte[] data) {  
            this.data = data;  
        }  
      
        public String getFilname() {  
            return filname;  
        }  
      
        public void setFilname(String filname) {  
            this.filname = filname;  
        }  
      
        public String getFormname() {  
            return formname;  
        }  
      
        public void setFormname(String formname) {  
            this.formname = formname;  
        }  
      
        public String getContentType() {  
            return contentType;  
        }  
      
        public void setContentType(String contentType) {  
            this.contentType = contentType;  
        }  
          
    } 



	/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// �������˵�
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.open:
			if (!bp.isOpen())
				//bp.openDevice(txtDevice.getText().toString());
			break;

		case R.id.close:
			if (bp.isOpen())
				bp.closeDevice();
			break;
		}

		return true;
	}*/
}