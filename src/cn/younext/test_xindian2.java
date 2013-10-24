package cn.younext;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;

import com.android.etcomm.sdk.BluetoothAdapterNotFoundException;
import com.android.etcomm.sdk.CreateSocketFailedException;
import com.android.etcomm.sdk.CreateStreamFailedException;
import com.android.etcomm.sdk.DeviceDisconnectedException;
import com.android.etcomm.sdk.FileItem;
import com.android.etcomm.sdk.OnFileTransferListener;
import com.android.etcomm.sdk.ServiceDiscoveryFailedException;
import com.android.etcomm.sdk.UnableToStartServiceDiscoveryException;
import com.android.etcomm.sdk.libecg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class test_xindian2 extends Activity{
	Button homeBtn;
	Button returnBtn;
	OnClickListener btnClick;
	BatteryView myview;
	TextView user;
	int userid;
	String username;
	String cardNum;
	
	Cursor c;
	DatabaseHelper helper;
	SQLiteDatabase db;
	
	private libecg ecg = new libecg();
	private String mac;
	//private EditText txtDevice = null;
	private ListView lstData = null;
	ProgressBar pbrCurrent = null;
	//ProgressBar pbrTotal = null;
	ArrayAdapter<FileItem> files = null;
	FileItem file = null;
	
	private byte[] buff = null;
	
    @SuppressWarnings("null")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
        
        setContentView(R.layout.test_xindian2);
        DigitalClock myclock=(DigitalClock)findViewById(R.id.test_xindian2_clock);
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

        user=(TextView)findViewById(R.id.test_xindian2_user);
        user.setText(getString(R.string.myhealth_Welcome)+username);
        
		//if (!ecg.isOpen())
			//ecg.openDevice(txtDevice.getText().toString());
        
        
        
        homeBtn = (Button)findViewById(R.id.test_xindian_homeBtn3);
        returnBtn=(Button)findViewById(R.id.test_xindian_returnBtn3);
        
        helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
 	    db = helper.getWritableDatabase();//�����ݿ�
 	    c=db.query(DatabaseHelper.BLUETOOTH_MACTABLE, null, DatabaseHelper.ID+"=2", null, null, null,DatabaseHelper.ID+" desc",null);
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
        			if (ecg.isOpen())
        			ecg.closeDevice();
    			    test_xindian2.this.setResult(RESULT_OK);
    		        test_xindian2.this.finish();
        		}
        		else if(v==returnBtn){
        			if (ecg.isOpen())
        			ecg.closeDevice();        			
        			test_xindian2.this.finish();	
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
		//txtDevice = (EditText) findViewById(R.id.ecg_device);
		lstData = (ListView) findViewById(R.id.ecg_data);
		pbrCurrent = (ProgressBar) findViewById(R.id.ecg_current);
		//pbrTotal = (ProgressBar) findViewById(R.id.ecg_total);

		files = new ArrayAdapter<FileItem>(test_xindian2.this, R.layout.file);

		lstData.setAdapter(files);

		for (int i = 0; i < fileList().length; i++) {
			String file = fileList()[i];

			try {
				InputStream stream = openFileInput(file);

				int length = stream.available();
				byte[] data = new byte[length];

				stream.read(data);

				FileItem item = new FileItem(i, data, length);
				helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
			    db = helper.getWritableDatabase();//�����ݿ�
			    String[] filename1=item.fileName.split(" ");
			    String[] filename2=filename1[0].split("-");
			    String[] filename3=filename1[1].split(":");
			    String fileName=filename2[0]+filename2[1]+filename2[2]+filename3[0]+filename3[1]+filename3[2];
			    c=db.query(DatabaseHelper.ECGFILE_TABLE, null,DatabaseHelper.ECGFILE_USERID+"="+userid+" and "+ DatabaseHelper.ECGFILE_FILENAME+"="+fileName, null, null, null,DatabaseHelper.ID+" desc",null);
			    Log.v("filename2", fileName);
			    Log.v("userid2", String.valueOf(userid));
			    c.moveToFirst();
			    if(!(c.isAfterLast())){
			    	Log.v("true", "true");
			    	files.add(item);
			    }
			    db.close();
				
//			    files.add(item);
				stream.close();
				
				
				/*FormFile[] xx=new FormFile[1];
				xx[0]=new FormFile(this.getDir("files", MODE_WORLD_WRITEABLE).getParentFile().getAbsolutePath()+"/files/"+item.fileName, item.fileData, "form1", null);
				Log.v("filedata",item.fileData.toString());
				Map<String, String> params=new HashMap<String, String>();
				//params.put("aa", "bb");

				//params.put("blood", "25");
				post("http://www.lkang.org/receive.aspx",params,xx);
				//String uriAPI="http://219.223.239.143/revReport.aspx";
            	//String queryString = "http://219.223.239.143/default.aspx?name=7&email=8"; 
            	//change space in the url into "%20"*/
            	//URL aURL = new URL(queryString.replace(" ", "%20"));*/
				
				
				
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		
		
		
		lstData.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu arg0, View arg1, ContextMenuInfo arg2) {
				arg0.setHeaderTitle(R.string.ecg_choose);

				MenuItem item = null;

				item = arg0.add(R.string.ecg_view);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (file != null) viewFile(file);
						return false;
					}
				});

				item = arg0.add(R.string.ecg_delete);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (file != null) {
							test_xindian2.this.deleteFile(file.fileName);
							files.remove(file);
						}

						file = null;
						return false;
					}
				});

				item = arg0.add(R.string.ecg_clear);
				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						for (int i = 0; i < files.getCount(); i++) {
							test_xindian2.this.deleteFile(files.getItem(i).fileName);
						}
						
						files.clear();
						file = null;
						return false;
					}
				});
			}
		});

		lstData.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				file = (FileItem) arg0.getItemAtPosition(arg2);
				if (file != null)
					viewFile(file);
			}
		});

		lstData.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				file = (FileItem) arg0.getItemAtPosition(arg2);
				return false;
			}
		});

		// ����Ѫѹ�����¼�������
		ecg.setOnFileTransfer(transfer);
		if (!ecg.isOpen())
			ecg.openDevice(mac);
    }
	private void viewFile(FileItem file) {
		Intent in = new Intent(this, EcgViewerActivity.class);
		
		in.putExtra("data", file);

		startActivity(in);
	}

	/**
	 * �ļ������¼�������
	 */
	private OnFileTransferListener transfer = new OnFileTransferListener() {
		/**
		 * �ļ����䷢���쳣�¼�
		 * 
		 * @param exception
		 *            �쳣��Ϣ
		 */
		@Override
		public void onException(Exception exception) {
			if (ecg.isOpen())
				ecg.closeDevice();

			if (exception instanceof BluetoothAdapterNotFoundException) {
				// δ��⵽����������
				Toast.makeText(test_xindian2.this, R.string.BluetoothAdapterNotFound, Toast.LENGTH_LONG).show();
				//test_xindian2.this.finish();
			} else if (exception instanceof UnableToStartServiceDiscoveryException) {
				// �ֻ�����δ����
				Toast.makeText(test_xindian2.this, R.string.UnableToStartServiceDiscovery, Toast.LENGTH_LONG).show();
				//test_xindian2.this.finish();
			} else if (exception instanceof CreateSocketFailedException) {
				// ���ֻ���������ʧ��
				Toast.makeText(test_xindian2.this, R.string.CreateSocketFailed, Toast.LENGTH_LONG).show();
				//test_xindian2.this.finish();
			} else if (exception instanceof ServiceDiscoveryFailedException) {
				// �ĵ�������δ����
				Toast.makeText(test_xindian2.this, R.string.ServiceDiscoveryFailed, Toast.LENGTH_LONG).show();
				//test_xindian2.this.finish();
			} else if (exception instanceof CreateStreamFailedException) {
				// ���ֻ���������ͨѶʧ��
				Toast.makeText(test_xindian2.this, R.string.CreateStreamFailed, Toast.LENGTH_LONG).show();
				//test_xindian2.this.finish();
			} else if (exception instanceof DeviceDisconnectedException) {
				// �豸�����ѶϿ��쳣
				Toast.makeText(test_xindian2.this, R.string.DeviceDisconnected, Toast.LENGTH_LONG).show();
				//test_xindian2.this.finish();
			}
		}

		private int _fileCount = 0;

		/**
		 * �ļ����俪ʼ�¼�
		 * 
		 * @param fileCount
		 *            ��������ļ�����Ŀ
		 */
		@Override
		public void onStart(int fileCount) {
			_fileCount = fileCount;

			pbrCurrent.setMax(0);
			pbrCurrent.setProgress(0);

			//pbrTotal.setMax(fileCount);
			//pbrTotal.setProgress(1);
		}

		/**
		 * �ļ���������¼�
		 * 
		 * @param current
		 *            ��ǰ�Ѵ������ݰ���Ŀ
		 * @param total
		 *            ������������ݰ���Ŀ
		 */
		@Override
		public void onProgress(int current, int total) {
			pbrCurrent.setMax(total);
			pbrCurrent.setProgress(current);
		}

		/**
		 * �����ļ��ѽ����¼�
		 * 
		 * @param file
		 *            �ѽ��յ����ļ�
		 */
		@Override
		public void onItemReceived(FileItem file) {
			//int progress = file.fileID < _fileCount ? file.fileID + 1 : file.fileID;

			//pbrTotal.setProgress(progress);

			files.add(file);
			
			for (int i = 0; i < files.getCount(); i++) {
				FileItem file1 = files.getItem(i);
				if (file1 == null)
					continue;

				try {
					OutputStream stream = openFileOutput(file1.fileName, Context.MODE_PRIVATE);
					stream.write(file1.fileData);
					stream.close();
					
					

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			
			
			helper = new DatabaseHelper(getBaseContext(), DatabaseHelper.DATABASE_NAME, null,DatabaseHelper.Version);     
		    db = helper.getWritableDatabase();//�����ݿ�
		    ContentValues values = new ContentValues();
		    String[] filename1=file.fileName.split(" ");
		    String[] filename2=filename1[0].split("-");
		    String[] filename3=filename1[1].split(":");
		    String fileName=filename2[0]+filename2[1]+filename2[2]+filename3[0]+filename3[1]+filename3[2];
		    Log.v("filename1", fileName);
		    Log.v("userid1", String.valueOf(userid));
		    values.put(DatabaseHelper.ECGFILE_FILENAME,fileName);
		    values.put(DatabaseHelper.ECGFILE_USERID, userid);
		    db.insert(DatabaseHelper.ECGFILE_TABLE, null, values);
		    db.close();
			edu.hitsz.android.Client client = new edu.hitsz.android.Client();
			            // ����һ���ļ�   
			    File myFile = new File(getDir("files", MODE_WORLD_WRITEABLE).getParentFile().getAbsolutePath()+"/files/"+file.fileName);   
			   
			            // �ж��ļ��Ƿ����   
			    if (myFile.exists()) {   
	                try {   
		   
			                    // ������   
			           FileInputStream inputStream = new FileInputStream(myFile);   
		               buff = new byte[inputStream.available()];   
		               Log.v("bufferlength", String.valueOf(buff.length));
                       inputStream.read(buff); ////////////////////���ļ�����һ��byte[]������  
		               inputStream.close();   
			   
			        } 
	                catch (Exception e) {   
			           Log.v("myfile", e.getMessage());   
			        }// end of try   
			    }// end of if(myFile)   
  
			//ǰ����Ҫ�ǻ���ļ��������byte[]������ͨ������client.insertECG()��һ���������û��Ŀ��ţ��ڶ���������Ҫ�����ļ���byte[] 
			try{
				Log.v("cardnumber",cardNum);
				String k=client.insertECG(cardNum, buff);
//				Log.v("k",k);
			}catch(Exception e){
//				Log.v("e",e.getMessage());
				Toast.makeText(test_xindian2.this,R.string.NetworkDisconneted, Toast.LENGTH_LONG).show();
			}
			
			
			/*FormFile[] xx=new FormFile[1];
			xx[0]=new FormFile(getDir("files", MODE_WORLD_WRITEABLE).getParentFile().getAbsolutePath()+"/files/"+file.fileName, file.fileData, "form1", null);
			Log.v("filedata",file.fileData.toString());
			Map<String, String> params=new HashMap<String, String>();

			try{
				post("http://www.lkang.org/receive.aspx",params,xx);
			}
			catch(Exception e){
				Toast.makeText(test_xindian2.this, R.string.NetworkDisconneted, Toast.LENGTH_LONG).show();
				
			}*/

			
			
			

		}

		/**
		 * �ļ���������¼�
		 * 
		 * @param fileCount
		 *            �ѽ��յ��ļ���Ŀ
		 */
		@Override
		public void onComplete(int fileCount) {
			pbrCurrent.setProgress(pbrCurrent.getMax());
			//pbrTotal.setProgress(pbrTotal.getMax());
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		// �ر��豸
		if (ecg.isOpen())
			ecg.closeDevice();

		/*for (int i = 0; i < files.getCount(); i++) {
			FileItem file = files.getItem(i);
			if (file == null)
				continue;

			try {
				OutputStream stream = openFileOutput(file.fileName, Context.MODE_PRIVATE);
				stream.write(file.fileData);
				stream.close();
				
				

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
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
			if (!ecg.isOpen())
				//ecg.openDevice(txtDevice.getText().toString());
			break;

		case R.id.close:
			if (ecg.isOpen())
				ecg.closeDevice();
			break;
		}

		return true;
	}*/
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
			if (ecg.isOpen())
				ecg.closeDevice();
			test_xindian2.this.finish();
            return true;

        }
        if(keyCode == KeyEvent.KEYCODE_MENU){
        	   
			if (ecg.isOpen())
			ecg.closeDevice();
		    test_xindian2.this.setResult(RESULT_OK);
	        test_xindian2.this.finish();
	        return true;
        }
        return super.onKeyDown(keyCode, event);
    } 

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
		}
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
            conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);  
      
            StringBuilder sb = new StringBuilder();  
              
            //�ϴ��ı��������֣���ʽ��ο�����  
           /* for (Map.Entry<String, String> entry : params.entrySet()) {//�������ֶ�����  
                sb.append("--");  
                sb.append(BOUNDARY);  
                sb.append("\r\n");  
                sb.append("Content-Disposition: form-data; name=\""+ entry.getKey() + "\"\r\n\r\n");  
                sb.append(entry.getValue());  
                sb.append("\r\n");  
            }  */
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());  
           //outStream.write(sb.toString().getBytes());//���ͱ��ֶ�����  
             
            //�ϴ����ļ����֣���ʽ��ο�����  
            for(FormFile file : files){  
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
                
            }  
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

	
}