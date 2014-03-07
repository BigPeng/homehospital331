package com.health.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.younext.R;

import com.health.web.ReUploader;
import com.health.web.WebService;

/**
 * 每一行6个元素的listview,用于显示最近的测量
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-11-28 上午9:20:40
 */
public class LatestRecordAdapter extends BaseAdapter {
	public static final int TITLE = 0;// 标题栏不显示按钮
	public static final int UPLOADED = 1;// 已经上传，按钮不可点击
	public static final int UNUPLOAD = 2;// 显示按钮
	private List<String[]> datas;
	private final static int TEXT_NUM = 5;// datas.get(TEXT_NUM-1)是上传状态
	private Context context;
	private List<JSONObject> jsonDatas;
	ExecutorService exec = Executors.newSingleThreadExecutor();// 单线程池

	public LatestRecordAdapter(Context context, List<String[]> datas,
			List<JSONObject> jsonDatas) {
		this.datas = datas;
		this.context = context;

		this.jsonDatas = jsonDatas;
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		String[] line = datas.get(position);
		if (convertView == null) {
			holder = new Holder();
			holder.dataET = new TextView[TEXT_NUM];
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.list_item_record, null);
			holder.layout = (RelativeLayout) convertView
					.findViewById(R.id.record_linearlayout);
			holder.dataET[0] = (TextView) convertView
					.findViewById(R.id.list_data_1);
			holder.dataET[1] = (TextView) convertView
					.findViewById(R.id.list_data_2);
			holder.dataET[2] = (TextView) convertView
					.findViewById(R.id.list_data_3);
			holder.dataET[3] = (TextView) convertView
					.findViewById(R.id.list_data_4);
			holder.dataET[4] = (TextView) convertView
					.findViewById(R.id.list_data_5);
			holder.button = (Button) convertView
					.findViewById(R.id.list_upload_button);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		for (int i = 0; i < TEXT_NUM; i++) {
			holder.dataET[i].setText(line[i]);
		}
		if (WebService.UPLOADED.equals(line[TEXT_NUM - 1])) {
			holder.button.setEnabled(false);
		} else {
			holder.button.setEnabled(true);
		}
		if (position == 0) {// 第一个是标题
			// 设置标题栏的上传按钮不可见
			holder.button.setVisibility(View.INVISIBLE);
			// 设置标题栏的背景
			holder.layout.setBackgroundResource(R.color.record_title);
		} else {
			// 设置内容栏的背景
			holder.layout.setBackgroundResource(R.color.record_content);
		}
		addListener(convertView, position);
		return convertView;
	}

	/***
	 * 添加上传点击按钮
	 * 
	 * @param convertView
	 * @param position
	 */
	private void addListener(View convertView, final int position) {
		((Button) convertView.findViewById(R.id.list_upload_button))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (v.isEnabled() == true)
							v.setEnabled(false);
						// 数据里面有标题栏，要比jsonDatas多1
						ReUploader reUploader = new ReUploader(context,
								jsonDatas.get(position - 1));
						exec.execute(reUploader);
						Toast.makeText(context, "开始上传,点击刷新查看上传结果",
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	class Holder {
		TextView[] dataET;
		Button button;
		RelativeLayout layout;
	}
}
