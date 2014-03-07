package com.health.myhealth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import cn.younext.R;

import com.health.BaseActivity;
import com.health.MainUi;
import com.health.database.Tables;
import com.health.util.AllRecordAdpter;
import com.health.util.MyProgressDialog;
import com.health.util.TimeHelper;
import com.health.web.WebService;

/***
 * 表格显示所有记录
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-12-19 上午10:00:56
 */
public class TableRecord extends BaseActivity {

	public static final String SELECTED_TOKEN = "selectedToken";
	public static final String SELECTED_PATH = "selectedPath";
	private static final Integer PAGE_COUNT = 10;
	private static final int OK = 0;
	private static final int NET_ERROR = 1;
	private static final int NO_DATA = 2;

	private Context context;
	private Button bpButton;// 血压
	private Button pulseButton;// 脉率
	private Button boButton;// 血氧
	private Button tempButton;// 体温
	private Button gluButton;// 血糖
	private Button uaButton;// 尿酸
	private Button cholButton;// 总胆固醇
	private Button urineButton;// 尿常规
	private Button tableShowButton;// 表格
	private Button curveShowButton;// 曲线
	private Button homeButton;// 回主界面
	private Button backButton;// 后退
	private Button lastButton = null;// 上次点击点按钮
	private ListView listview;// 显示表格的listview
	private AllRecordAdpter mAdpter;
	private List<String[]> datas = new ArrayList<String[]>();// 显示数据
	private String[] titles = { "测量时间", "测量值", "分析结果" };
	private int pageIndex = 0;// 默认为第一页
	private boolean hasMore = false;// 还有更多数据显示
	private String cPath = WebService.PATH_QUERY_BP;// 当前项目的后缀
	private String cToken = Tables.SBP;// 当前项目
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private MyProgressDialog mDiaog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_record);
		context = this;
		parseBundle();
		mDiaog = new MyProgressDialog(context);
		initView();
		setButtonListener();
		tableShowButton.setSelected(true);
		curveShowButton.setSelected(false);
		setSelected(cToken);
		clearAndaddTitles();
		mAdpter = new AllRecordAdpter(context, datas);
		listview.setAdapter(mAdpter);
		setListViewListener();
		mDiaog.show();
		exec.execute(new DataDenerator(mHanlder, cPath, cToken, pageIndex));
	}

	/***
	 * 设置初始时的选中button
	 * 
	 * @param token
	 */
	private void setSelected(String token) {
		Button cButton;
		if (Tables.SBP.equals(token))
			cButton = bpButton;
		else if (Tables.PULSE.equals(token))
			cButton = pulseButton;
		else if (Tables.SBP.equals(token))
			cButton = bpButton;
		else if (Tables.BO.equals(token))
			cButton = boButton;
		else if (Tables.TEMP.equals(token))
			cButton = tempButton;
		else if (Tables.GLU.equals(token))
			cButton = gluButton;
		else if (Tables.UA.equals(token))
			cButton = uaButton;
		else if (Tables.CHOL.equals(token))
			cButton = cholButton;
		else
			cButton = urineButton;
		setSelected(cButton);
	}

	/***
	 * 解析bundle的数据
	 * 
	 * @param savedInstanceState
	 */
	private void parseBundle() {
		Bundle state = getIntent().getExtras();
		if (state != null) {
			String path = state.getString(TableRecord.SELECTED_PATH);
			if (null != path)
				cPath = path;
			String token = state.getString(TableRecord.SELECTED_TOKEN);
			if (null != token)
				cToken = token;
		}
	}

	private void setListViewListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == datas.size() - 1 && hasMore) {
					datas.remove(position);
					mDiaog.show();
					pageIndex++;
					exec.execute(new DataDenerator(mHanlder, cPath, cToken,
							pageIndex));
				}
			}
		});
	}

	/**
	 * 设置选中按钮,取消上次按钮的选择
	 * 
	 * @param button
	 */
	private void setSelected(Button button) {
		if (lastButton != null)
			lastButton.setSelected(false);
		lastButton = button;
		lastButton.setSelected(true);
	}

	/***
	 * 设置监听器
	 */
	private void setButtonListener() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == curveShowButton) {
					jump2curve();
				} else if (v == tableShowButton) {

				} else if (v == homeButton) {
					TableRecord.this.setResult(RESULT_OK);
					TableRecord.this.finish();
				} else if (v == backButton) {
					TableRecord.this.finish();
				} else {
					String path = WebService.PATH_QUERY_BP;
					;
					String token = Tables.SBP;
					if (v == bpButton) {
						path = WebService.PATH_QUERY_BP;
						token = Tables.SBP;// 代表舒张压和收缩压
					} else if (v == pulseButton) {
						path = WebService.PATH_QUERY_PULSE;
						token = Tables.PULSE;
					} else if (v == boButton) {
						path = WebService.PATH_QUERY_BO;
						token = Tables.BO;
					} else if (v == tempButton) {
						path = WebService.PATH_QUERY_TEMP;
						token = Tables.TEMP;
					} else if (v == gluButton) {
						path = WebService.PATH_QUERY_GLU;
						token = Tables.GLU;
					} else if (v == uaButton) {
						path = WebService.PATH_QUERY_UA;
						token = Tables.UA;
					} else if (v == cholButton) {
						path = WebService.PATH_QUERY_CHOL;
						token = Tables.CHOL;
					} else if (v == urineButton) {
						path = WebService.PATH_QUERY_URINE;
						token = Tables.LEU;// 代表尿常规11项
					}
					// 按了新的按鈕
					if (!cToken.equals(token)) {
						cToken = token;
						cPath = path;
						// 页号清0
						pageIndex = 0;
						setSelected((Button) v);
						clearAndaddTitles();
						mAdpter.notifyDataSetChanged();
						mDiaog.show();
						exec.execute(new DataDenerator(mHanlder, path, token,
								pageIndex));

					}

				}
			}

			private void jump2curve() {
				Intent intent;
				if (cToken.equals(Tables.LEU)) {
					intent = new Intent(TableRecord.this, UrineChart.class);
				} else {
					intent = new Intent(TableRecord.this, CurveRecord.class);
					Bundle bundle = new Bundle();
					bundle.putString(TableRecord.SELECTED_TOKEN, cToken);
					bundle.putString(TableRecord.SELECTED_PATH, cPath);
					intent.putExtras(bundle);
				}
				startActivity(intent);
				TableRecord.this.finish();
			}
		};
		bpButton.setOnClickListener(listener);
		pulseButton.setOnClickListener(listener);
		boButton.setOnClickListener(listener);
		tempButton.setOnClickListener(listener);
		gluButton.setOnClickListener(listener);
		uaButton.setOnClickListener(listener);
		cholButton.setOnClickListener(listener);
		urineButton.setOnClickListener(listener);
		tableShowButton.setOnClickListener(listener);
		curveShowButton.setOnClickListener(listener);
		homeButton.setOnClickListener(listener);
		backButton.setOnClickListener(listener);
	}

	Handler mHanlder = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mDiaog.cancel();
			switch (msg.what) {
			case OK:
				List<String[]> recoreds = (List<String[]>) msg.obj;
				datas.addAll(recoreds);
				if (recoreds.size() == PAGE_COUNT) {
					hasMore = true;// 还有更多
					datas.add(new String[] { "", "加载更多", "" });
				}
				mAdpter.notifyDataSetChanged();
				break;
			case NET_ERROR:
				if (pageIndex == 0) {// 网络异常,且在0页时,需要把显示的其他项的数据给删掉
					clearAndaddTitles();
					mAdpter.notifyDataSetChanged();
				}
				Toast.makeText(context, "网络错误！", Toast.LENGTH_SHORT).show();
				break;
			case NO_DATA:
				if (pageIndex == 0) {// 第0页没有数据时,需要把显示的其他项的数据给删掉
					clearAndaddTitles();
					mAdpter.notifyDataSetChanged();
				}
				Toast.makeText(context, "无数据！", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private class DataDenerator implements Runnable {
		public Handler handler;
		private String path;
		private String token;
		private int pageIndex;

		public DataDenerator(Handler handler, String path, String token,
				int pageIndex) {
			this.handler = handler;
			this.path = path;
			this.token = token;
			this.pageIndex = pageIndex;
		}

		@Override
		public void run() {
			List<String[]> recoreds = getQueueDatas(path, pageIndex, token);

			hasMore = false;// 先假设已经没有更多
			if (recoreds != null) {
				if (recoreds.size() == 0) {
					handler.obtainMessage(NO_DATA).sendToTarget();
				} else {
					handler.obtainMessage(OK, recoreds).sendToTarget();
				}
			} else
				handler.obtainMessage(NET_ERROR).sendToTarget();
		}

	}

	/***
	 * 添加标题,当有数据时先清除数据
	 */
	private void clearAndaddTitles() {
		datas.clear();
		datas.add(titles);
	}

	/***
	 * 获取查询数据的参数，这里所有的查询参数都是相同的，只有查询路径不同
	 * 
	 * @param pageIndex
	 * @return
	 */
	private Map<String, String> getQueueParas(Integer pageIndex) {
		Map<String, String> paras = new HashMap<String, String>();
		String startTime = TimeHelper.getBeforeTime(365);
		String endTime = TimeHelper.getCurrentTime();
		paras.put(WebService.ENDTIME, endTime);
		paras.put(WebService.STARTTIME, startTime);
		paras.put(Tables.CARDNO, cache.getUserId());
		paras.put(WebService.PAGECOUNT, PAGE_COUNT.toString());
		paras.put(WebService.PAGEINDEX, pageIndex.toString());
		return paras;
	}

	/***
	 * 获取查询得到的数据
	 * 
	 * @param pageIndex
	 * @param path
	 * @param token
	 * @return
	 */
	private List<String[]> getQueueDatas(String path, Integer pageIndex,
			String token) {
		Map<String, String> paras = getQueueParas(pageIndex);
		try {
			List<String[]> recoreds = DataService.getDataForTable(context,
					paras, path, token);
			return recoreds;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 初始化控件
	 */
	private void initView() {
		bpButton = (Button) this.findViewById(R.id.show_bp);
		pulseButton = (Button) this.findViewById(R.id.show_pulse);
		boButton = (Button) this.findViewById(R.id.show_bo);
		tempButton = (Button) this.findViewById(R.id.show_temp);
		gluButton = (Button) this.findViewById(R.id.show_glu);
		uaButton = (Button) this.findViewById(R.id.show_ua);
		cholButton = (Button) this.findViewById(R.id.show_chol);
		urineButton = (Button) this.findViewById(R.id.show_urine);
		tableShowButton = (Button) this.findViewById(R.id.table_show);
		curveShowButton = (Button) this.findViewById(R.id.curve_show);
		homeButton = (Button) this.findViewById(R.id.to_home_button);
		backButton = (Button) this.findViewById(R.id.return_button);
		listview = (ListView) this.findViewById(R.id.show_table_listview);
	}

}
