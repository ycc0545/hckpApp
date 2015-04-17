package com.haven.hckp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haven.hckp.R;
import com.haven.hckp.api.ApiClient;
import com.haven.hckp.bean.News;
import com.haven.hckp.bean.Team;
import com.haven.hckp.bean.URLs;
import com.haven.hckp.common.UIHelper;
import com.haven.hckp.ui.MainActivity;
import com.haven.hckp.ui.TeamFindActivity;
import com.haven.hckp.widght.CustomDialog;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.HashMap;
import java.util.List;

public class TeamViewNewsAdapter extends BaseAdapter {
	private Context context;// 运行上下文
	private List<Team> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	static class ListItemView { // 自定义控件集合
		public TextView teamTitle;
		public TextView teamDesc;
	}

	/**
	 * 实例化Adapter
	 *
	 * @param context
	 * @param data
	 * @param resource
	 */
	public TeamViewNewsAdapter(Context context, List<Team> data, int resource) {
		this.context = context;
		this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
		this.itemViewResource = resource;
		this.listItems = data;
	}

	public int getCount() {
		return listItems.size();
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(final int position, View convertView, ViewGroup parent) {
		// Log.d("method", "getView");

		// 自定义视图
		ListItemView listItemView = null;

		if (convertView == null) {
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);

			listItemView = new ListItemView();
			// 获取控件对象
            listItemView.teamTitle = (TextView) convertView.findViewById(R.id.team_title);
            listItemView.teamDesc = (TextView) convertView.findViewById(R.id.team_desc);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置文字和图片
		final Team news = listItems.get(position);

		listItemView.teamTitle.setText(news.getTp_tc_name());
		listItemView.teamTitle.setTag(news);
        listItemView.teamDesc.setText(news.getTp_tc_phone());
		//审核按钮
		TextView textStatus = (TextView) convertView.findViewById(R.id.icon_status);
		if(textStatus != null){
			if("1".equals(news.getTp_tc_status())){
				textStatus.setVisibility(View.VISIBLE);
			}else{
				textStatus.setVisibility(View.GONE);
			}
		}
		//解挂靠按钮
		final String tcId = news.getTp_tc_id();
		ImageView btn = (ImageView) convertView.findViewById(R.id.button2);
		if(btn != null){
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					CustomDialog.Builder builder = new CustomDialog.Builder(TeamViewNewsAdapter.this.context);
					builder.setTitle("提示");
					builder.setMessage("您确定解除挂靠此车队吗？");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final ProgressDialog pd = ProgressDialog.show(context,null,"请稍后...");
							String newUrl = ApiClient._MakeURL(URLs.TEAM_DEL_POST, new HashMap<String, Object>());
							RequestParams params = new RequestParams();
							params.addBodyParameter("tc_id", tcId);
							HttpUtils http = new HttpUtils();
							http.send(HttpRequest.HttpMethod.POST, newUrl, params, new RequestCallBack<String>() {
								@Override
								public void onSuccess(ResponseInfo<String> objectResponseInfo) {
									pd.dismiss();
									JSONObject obj = JSON.parseObject(objectResponseInfo.result);
									String code = obj.get("code").toString();
									if (code.equals("1")) {
										TeamViewNewsAdapter.this.listItems.remove(position);
										TeamViewNewsAdapter.this.notifyDataSetChanged();
										UIHelper.ToastMessage(TeamViewNewsAdapter.this.context, obj.get("msg").toString());
									} else {
										UIHelper.ToastMessage(TeamViewNewsAdapter.this.context, obj.get("msg").toString());
									}
								}

								@Override
								public void onFailure(HttpException e, String s) {
									pd.dismiss();
								}
							});
							dialog.dismiss();
						}
					});

					builder.setNegativeButton("取消",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});

					builder.create().show();

				}
			});
		}
		return convertView;
	}
}
