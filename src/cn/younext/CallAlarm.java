package cn.younext;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.Bundle;

public class CallAlarm extends BroadcastReceiver{
	String text;
	String tishi;
	@Override
	public void onReceive(Context context,Intent intent)
	{
		Bundle extra=intent.getExtras();
        if(extra!=null)
        {
        	text=extra.getString("text");
        	tishi=extra.getString("tishi");
        }

		Intent i = new Intent(context, AlarmAlert.class);
		
		Bundle bundleRet = new Bundle();
		bundleRet.putString("STR_CALLER","");
		i.putExtras(bundleRet);
		i.putExtra("text", text);
		i.putExtra("tishi", tishi);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		
	}
}