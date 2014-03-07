package com.health.util;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.younext.R;

/**
 * 显示所有记录的适配器，设置第一行为标题行，背景与内容行不同
 * 
 * @author jiqunpeng
 * 
 *         创建时间：2013-12-20 下午3:10:17
 */
public class AllRecordAdpter extends BaseAdapter {
	private List<String[]> datas;
	private Context context;

	public AllRecordAdpter(Context context, List<String[]> datas) {
		this.context = context;
		this.datas = datas;
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
		// 只有为空时才构造
		if (convertView == null) {
			holder = new Holder();
			holder.dataET = new TextView[line.length];
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.list_item_all_record, null);
			holder.dataET[0] = (TextView) convertView
					.findViewById(R.id.list_data_1);
			holder.dataET[1] = (TextView) convertView
					.findViewById(R.id.list_data_2);
			holder.dataET[2] = (TextView) convertView
					.findViewById(R.id.list_data_3);
			holder.layout = (RelativeLayout) convertView
					.findViewById(R.id.record_layout);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		for (int i = 0; i < line.length; i++) {
			holder.dataET[i].setText(line[i]);
		}
		if (position == 0) {
			holder.layout.setBackgroundResource(R.color.record_title);
		} else {
			// 设置内容栏的背景
			holder.layout.setBackgroundResource(R.color.record_content);
		}
		return convertView;
	}

	/***
	 * 用于暂存数据
	 * 
	 * @author jiqunpeng
	 * 
	 *         创建时间：2013-12-20 下午3:11:21
	 */
	class Holder {
		TextView[] dataET;// 显示的内容
		RelativeLayout layout;// 设定背景
	}
}
