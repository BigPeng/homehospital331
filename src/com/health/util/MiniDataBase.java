package com.health.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * �������ݵ�����SharedPreferences
 * 
 * @author jiqunpeng
 * 
 *         ����ʱ�䣺2013-10-25 ����10:22:42
 */
public class MiniDataBase {
	public static final String PC300 = "PC300";
	public static final String BENECHECK = "BeneCheck";//�ٽ�Ѫ���豸
	public static final String GMPUA = "GmpUa";//��Һ������

	Context context;
	private SharedPreferences sharedPrefrences;
	private Editor editor;

	public MiniDataBase(Context context) {
		this.context = context;
		sharedPrefrences = context.getSharedPreferences("padhealth",
				Context.MODE_PRIVATE);
		editor = sharedPrefrences.edit();
	}

	/**
	 * �����豸��ַ
	 * 
	 * @param device
	 * @param address
	 */
	public void saveDeviceAddress(String device, String address) {
		editor.putString(device, address);
		editor.commit();// �ύ
	}

	/**
	 * ��ȡ�豸�ĵ�ַ
	 * 
	 * @param device
	 * @return
	 */
	public String getDeviceAddress(String device) {
		return sharedPrefrences.getString(device, null);
	}
}
