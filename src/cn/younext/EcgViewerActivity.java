package cn.younext;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.android.etcomm.sdk.FileItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * �ĵ�鿴��
 * 
 * @author sjh
 * 
 */
public class EcgViewerActivity extends Activity {
	int userid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		

		Intent in = getIntent();

		if (in != null) {
			FileItem file = (FileItem) in.getSerializableExtra("data");

			setContentView(new EcgViewer(this, file));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			return true;
		}
		else if(keyCode==KeyEvent.KEYCODE_MENU){
			finish();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
    private static class EcgViewer extends View {
    	private final static float ADJUST_VALUE = 1010;
    	private final static int ZOOM_LEVEL = 50;
    	private final static int DATA_OFFSET = 3552;
    	
    	private float[] _points = null;
    	
        private final Paint paint = new Paint();
        
        public EcgViewer(Context context, FileItem file) {
            super(context);
            setFocusable(true);
            setFocusableInTouchMode(true);

            paint.setAntiAlias(true);
            paint.setColor(Color.YELLOW);
    		
            if (file == null) return;
    		if (file.fileData == null) return;
    		
    		// �ĵ������ֽ���
    		int dataLength = file.fileData.length - DATA_OFFSET;
    		
    		// �ĵ����ݵ���
    		int ecgPoints = dataLength / 2;

    		ByteArrayInputStream stream = new ByteArrayInputStream(file.fileData, DATA_OFFSET, dataLength);
    		
    		_points = new float[ecgPoints];

			int pos = 0;

			while (stream.available() > 0) {
				int low = stream.read();
				int high = stream.read();

				short y = (short) ((high << 8) + low);

				_points[pos++] = y / ADJUST_VALUE * ZOOM_LEVEL;
			}
			
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
        
        private int startX = 0;
        private int currX = 0;

        private int drawIndex = 0;
        private int currIndex = 0;
        
        @Override
        protected void onDraw(Canvas canvas) {
    		if (_points == null) return;
    		
    		float height = getHeight();

    		float prevx = 0;
    		float prevy = 0;
    		
    		prevx = 0;
    		prevy = height / 2 - _points[drawIndex];
    		
    		int width = getWidth();
    		
    		for (int i = 1; i < width; i++) {
    			float x = i;
    			float y = height / 2 - _points[drawIndex + i];
    			
    			canvas.drawLine(prevx, prevy, x, y, paint);
    			
    			prevx = x;
    			prevy = y;
    		}
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int offset = 0;
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                	startX = currX = x;
                    currIndex = drawIndex;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                	currX = x;
                    offset = startX - currX;
                    drawIndex = currIndex + offset;
                    
                    if (drawIndex < 0)
                    	drawIndex = 0;
                    else if (drawIndex > _points.length - getWidth())
                    	drawIndex = _points.length - getWidth();
                    
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    invalidate();
                    break;
            }
            
            return true;
        }
    }

    
}
