package com.health.measurement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.database.Cache;
import com.health.database.DatabaseService;
import com.health.database.Tables;
import com.health.util.TimeHelper;
import com.health.web.Uploader;
import com.health.web.WebService;

/***
 * 手写测量
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-12-18 下午3:01:58
 */
public class HandInputMeasure extends BaseActivity {

	private static Button homeBtn;
	private static Button returnBtn;
	private static Button machineinput;

	private static Button handinputButton;

	private static Button xueYaMaiLvBtn;
	private static Button xueYangBtn;
	private static Button tiWenBtn;
	private static Button xueTangBtn;
	private static Button niaoSuanBtn;
	private static Button zhongDanGuCunBtn;
	private static Button niaoYeFenXiBtn;

	private static EditText gaoYaET;
	private static EditText diYaET;
	private static EditText maiLvET;
	private static EditText xueYangET;
	private static EditText tiWenET;
	private static EditText xueTangET;
	private static EditText niaoSuanET;
	private static EditText zhongDanGuCunET;
	private static EditText baiXiBaoET;
	private static EditText yaXiaoSuanYanET;
	private static EditText niaoDanYuanET;
	private static EditText danBaiZhiET;
	private static EditText phZhiET;
	private static EditText qianXueET;
	private static EditText tongTiET;
	private static EditText danHongShuET;
	private static EditText puTaoTangET;
	private static EditText weiShengShuCET;
	private static EditText biZhongET;

	private static DatabaseService dbService;
	private static Context context;
	private static HandInputHandler handler;
	private static ExecutorService exec = Executors.newSingleThreadExecutor();// 单线程池

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_handinput);

		context = this;
		dbService = new DatabaseService(context);
		cache = new Cache(context);
		findView();
		handler = new HandInputHandler();

		setOnClickListener();

	}

	private static class HandInputHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == Uploader.MESSAGE_UPLOADE_RESULT) {
				Bundle bundler = msg.getData();
				// String item =
				// bundler.getString(Cache.ITEM);
				int status = bundler.getInt(Uploader.STUTAS);
				switch (status) {
				case Uploader.OK:
					Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
					break;
				case Uploader.FAILURE:
					Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show();
					break;
				case Uploader.NET_ERROR:
					Toast.makeText(context, "网络异常,上传失败", Toast.LENGTH_SHORT)
							.show();
					break;
				default:
					break;
				}
				/*
				 * if (Cache.BP.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } } if
				 * (Cache.BO.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } } if
				 * (Cache.TEMP.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } } if
				 * (Cache.GLU.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } } if
				 * (Cache.UA.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } } if
				 * (Cache.CHOL.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } } if
				 * (Cache.URINE.equals(item)) { switch
				 * (status) { case 0:
				 * Toast.makeText(context, "上传成功",
				 * Toast.LENGTH_SHORT) .show(); break;
				 * case 1: Toast.makeText(context,
				 * "上传失败", Toast.LENGTH_SHORT) .show();
				 * break; default: break; } }
				 */
			}
		}
	}

	private void setOnClickListener() {
		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == xueYaMaiLvBtn) {
					String diYa = diYaET.getText().toString(); // 舒张压
					String gaoYa = gaoYaET.getText().toString(); // 收缩压
					String maiLv = maiLvET.getText().toString(); // 脉率

					if (diYa.length() > 0 && gaoYa.length() > 0
							&& maiLv.length() > 0) {
						xueYaMaiLvBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的血压和脉率",
								Toast.LENGTH_SHORT).show();
						uploadXueYaMaiLv();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == xueYangBtn) {
					String xueYang = xueYangET.getText().toString();

					if (xueYang.length() > 0) {
						xueYangBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的血氧", Toast.LENGTH_SHORT)
								.show();
						uploadXueYang();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == tiWenBtn) {
					String tiWen = tiWenET.getText().toString();

					if (tiWen.length() > 0) {
						tiWenBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的体温", Toast.LENGTH_SHORT)
								.show();
						uploadTiWen();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == xueTangBtn) {
					String xueTang = xueTangET.getText().toString();

					if (xueTang.length() > 0) {
						xueTangBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的血糖", Toast.LENGTH_SHORT)
								.show();
						uploadXueYang();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == niaoSuanBtn) {
					String niaoSuan = niaoSuanET.getText().toString();
					if (niaoSuan.length() > 0) {
						niaoSuanBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的尿酸", Toast.LENGTH_SHORT)
								.show();
						uploadNiaoSuan();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == zhongDanGuCunBtn) {
					String zhongDanGuCun = zhongDanGuCunET.getText().toString();
					if (zhongDanGuCun.length() > 0) {
						zhongDanGuCunBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的总胆固醇",
								Toast.LENGTH_SHORT).show();
						uploadZhongDanGuCun();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == niaoYeFenXiBtn) {
					String baiXiBao = baiXiBaoET.getText().toString();
					String yaXiaoSuanYan = yaXiaoSuanYanET.getText().toString();
					String niaoDanYuan = niaoDanYuanET.getText().toString();
					String danBaiZhi = danBaiZhiET.getText().toString();
					String phZhi = phZhiET.getText().toString();
					String qianXue = qianXueET.getText().toString();
					String tongTi = tongTiET.getText().toString();
					String danHongShu = danHongShuET.getText().toString();
					String puTaoTang = puTaoTangET.getText().toString();
					String weiShengShuC = weiShengShuCET.getText().toString();
					String biZhong = biZhongET.getText().toString();
					if (baiXiBao.length() > 0 && yaXiaoSuanYan.length() > 0
							&& niaoDanYuan.length() > 0
							&& danBaiZhi.length() > 0 && phZhi.length() > 0
							&& qianXue.length() > 0 && tongTi.length() > 0
							&& danHongShu.length() > 0
							&& puTaoTang.length() > 0
							&& weiShengShuC.length() > 0
							&& biZhong.length() > 0) {

						niaoYeFenXiBtn.setClickable(false);
						Toast.makeText(context, "开始上传您的尿液分析",
								Toast.LENGTH_SHORT).show();
						uploadNiaoYeFenXi();
					} else {
						Toast.makeText(context, "请完整输入您的数据", Toast.LENGTH_SHORT)
								.show();
					}

				} else if (v == homeBtn) {
					HandInputMeasure.this.setResult(RESULT_OK);
					HandInputMeasure.this.finish();
				} else if (v == returnBtn) {
					HandInputMeasure.this.finish();
				} else if (v == machineinput) {
					HandInputMeasure.this.finish();
				}
			}
		};

		xueYaMaiLvBtn.setOnClickListener(onClickListener);
		xueYangBtn.setOnClickListener(onClickListener);
		tiWenBtn.setOnClickListener(onClickListener);
		xueTangBtn.setOnClickListener(onClickListener);
		niaoSuanBtn.setOnClickListener(onClickListener);
		zhongDanGuCunBtn.setOnClickListener(onClickListener);
		niaoYeFenXiBtn.setOnClickListener(onClickListener);
		homeBtn.setOnClickListener(onClickListener);
		returnBtn.setOnClickListener(onClickListener);
		machineinput.setOnClickListener(onClickListener);

	}

	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {

			HandInputMeasure.this.setResult(RESULT_OK);
			HandInputMeasure.this.finish();

			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	private void findView() {
		homeBtn = (Button) findViewById(R.id.test_handinput_homeBtn);
		returnBtn = (Button) findViewById(R.id.test_handinput_returnBtn);
		machineinput = (Button) findViewById(R.id.test_machinebtn);
		handinputButton = (Button) findViewById(R.id.test_handbtn);
		machineinput.setSelected(false);
		handinputButton.setSelected(true);
		xueYaMaiLvBtn = (Button) findViewById(R.id.test_handinput_BPBtn);
		xueTangBtn = (Button) findViewById(R.id.test_handinput_XTBtn);
		tiWenBtn = (Button) findViewById(R.id.test_handinput_TWBtn);
		xueYangBtn = (Button) findViewById(R.id.test_handinput_XYBtn);
		niaoSuanBtn = (Button) findViewById(R.id.test_handinput_NSBtn);
		zhongDanGuCunBtn = (Button) findViewById(R.id.test_handinput_ZDGCBtn);
		niaoYeFenXiBtn = (Button) findViewById(R.id.test_handinput_NYFXBtn);

		gaoYaET = (EditText) findViewById(R.id.test_handinput_HBGedittext);
		diYaET = (EditText) findViewById(R.id.test_handinput_LBGedittext);
		xueTangET = (EditText) findViewById(R.id.test_handinput_XTedittext);
		tiWenET = (EditText) findViewById(R.id.test_handinput_TWedittext);
		xueYangET = (EditText) findViewById(R.id.test_handinput_XYedittext);
		maiLvET = (EditText) findViewById(R.id.test_handinput_maiLvET);
		niaoSuanET = (EditText) findViewById(R.id.test_handinput_NSedittext);
		zhongDanGuCunET = (EditText) findViewById(R.id.test_handinput_ZDGCedittext);
		baiXiBaoET = (EditText) findViewById(R.id.test_handinput_baiXiBaoET);
		yaXiaoSuanYanET = (EditText) findViewById(R.id.test_handinput_yaXiaoSuanYanET);
		niaoDanYuanET = (EditText) findViewById(R.id.test_handinput_niaoDanYuanET);
		danBaiZhiET = (EditText) findViewById(R.id.test_handinput_danBaiZiET);
		phZhiET = (EditText) findViewById(R.id.test_handinput_phZhiET);
		qianXueET = (EditText) findViewById(R.id.test_handinput_qianXueET);
		tongTiET = (EditText) findViewById(R.id.test_handinput_tongTiET);
		danHongShuET = (EditText) findViewById(R.id.test_handinput_danHongSuET);
		puTaoTangET = (EditText) findViewById(R.id.test_handinput_puTaoTangET);
		weiShengShuCET = (EditText) findViewById(R.id.test_handinput_weiShengSuCET);
		biZhongET = (EditText) findViewById(R.id.test_handinput_biZhongET);
	}

	/***
	 * 获取几个测量项目都有的几个属性
	 * 
	 * @return
	 */
	public Map<String, String> getDefaltAttrs() {
		String time = TimeHelper.getCurrentTime();
		String idCard = cache.getUserId();
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put(Tables.TIME, time);
		dataMap.put(Tables.CARDNO, idCard);
		dataMap.put(WebService.STATUS, WebService.UNUPLOAD);// 状态为未上传
		return dataMap;
	}

	public void uploadXueYaMaiLv() {
		Tables table = new Tables();
		String diYa = diYaET.getText().toString(); // 舒张压
		String gaoYa = gaoYaET.getText().toString(); // 收缩压
		String maiLv = maiLvET.getText().toString(); // 脉率

		if (diYa.length() > 0 && gaoYa.length() > 0 && maiLv.length() > 0) {
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.SBP, gaoYa);
			dataMap.put(Tables.DBP, diYa);
			dataMap.put(Tables.PULSE, maiLv);
			Uploader xueYaMaiLvUploader = new Uploader(dataMap, Cache.BP,
					WebService.PATH_BP, cache, dbService, handler,
					table.bpTable());
			exec.execute(xueYaMaiLvUploader);
		}
	}

	public void uploadXueYang() {
		Tables table = new Tables();
		String xueYang = xueYangET.getText().toString();

		if (xueYang.length() > 0) {
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.BO, xueYang);
			Uploader xueYangUploader = new Uploader(dataMap, Cache.BO,
					WebService.PATH_BO, cache, dbService, handler,
					table.boTable());
			exec.execute(xueYangUploader);
		}
	}

	public void uploadTiWen() {
		Tables table = new Tables();
		String tiWen = tiWenET.getText().toString();

		if (tiWen.length() > 0) {
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.TEMP, tiWen);
			Uploader tiWenUploader = new Uploader(dataMap, Cache.TEMP,
					WebService.PATH_TEMP, cache, dbService, handler,
					table.tempTable());
			exec.execute(tiWenUploader);
		}
	}

	public void uploadXueTang() {
		Tables table = new Tables();
		String xueTang = xueTangET.getText().toString();

		if (xueTang.length() > 0) {
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.GLU, xueTang);
			Uploader xueTangUploader = new Uploader(dataMap, Cache.GLU,
					WebService.PATH_GLU, cache, dbService, handler,
					table.gluTable());
			exec.execute(xueTangUploader);
		}
	}

	public void uploadNiaoSuan() {
		Tables table = new Tables();
		String niaoSuan = niaoSuanET.getText().toString();
		if (niaoSuan.length() > 0) {
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.UA, niaoSuan);
			Uploader niaoSuanUploader = new Uploader(dataMap, Cache.UA,
					WebService.PATH_UA, cache, dbService, handler,
					table.uaTable());
			exec.execute(niaoSuanUploader);
		}
	}

	public void uploadZhongDanGuCun() {
		Tables table = new Tables();
		String zhongDanGuCun = zhongDanGuCunET.getText().toString();
		if (zhongDanGuCun.length() > 0) {
			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.CHOL, zhongDanGuCun);
			Uploader zhongDanGuCunUploader = new Uploader(dataMap, Cache.CHOL,
					WebService.PATH_CHOL, cache, dbService, handler,
					table.cholTable());
			exec.execute(zhongDanGuCunUploader);
		}
	}

	public void uploadNiaoYeFenXi() {
		Tables table = new Tables();
		String baiXiBao = baiXiBaoET.getText().toString();
		String yaXiaoSuanYan = yaXiaoSuanYanET.getText().toString();
		String niaoDanYuan = niaoDanYuanET.getText().toString();
		String danBaiZhi = danBaiZhiET.getText().toString();
		String phZhi = phZhiET.getText().toString();
		String qianXue = qianXueET.getText().toString();
		String tongTi = tongTiET.getText().toString();
		String danHongShu = danHongShuET.getText().toString();
		String puTaoTang = puTaoTangET.getText().toString();
		String weiShengShuC = weiShengShuCET.getText().toString();
		String biZhong = biZhongET.getText().toString();
		if (baiXiBao.length() > 0 && yaXiaoSuanYan.length() > 0
				&& niaoDanYuan.length() > 0 && danBaiZhi.length() > 0
				&& phZhi.length() > 0 && qianXue.length() > 0
				&& tongTi.length() > 0 && danHongShu.length() > 0
				&& puTaoTang.length() > 0 && weiShengShuC.length() > 0
				&& biZhong.length() > 0) {

			Map<String, String> dataMap = getDefaltAttrs();
			dataMap.put(Tables.LEU, baiXiBao);
			dataMap.put(Tables.NIT, yaXiaoSuanYan);
			dataMap.put(Tables.UBG, niaoDanYuan);
			dataMap.put(Tables.PRO, danBaiZhi);
			dataMap.put(Tables.PH, phZhi);
			dataMap.put(Tables.BLD, qianXue);
			dataMap.put(Tables.KET, tongTi);
			dataMap.put(Tables.BIL, danHongShu);
			dataMap.put(Tables.UGLU, puTaoTang);
			dataMap.put(Tables.VC, weiShengShuC);
			dataMap.put(Tables.SG, biZhong);
			Uploader niaoYeFenXiUploader = new Uploader(dataMap, Cache.URINE,
					WebService.PATH_URINE, cache, dbService, handler,
					table.urineTable());
			exec.execute(niaoYeFenXiUploader);
		}
	}

}
