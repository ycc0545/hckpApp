package com.instway.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.instway.app.AppContext;
import com.instway.app.AppException;
import com.instway.app.R;
import com.instway.app.adapter.CarViewNewsAdapter;
import com.instway.app.bean.Car;
import com.instway.app.bean.CarList;
import com.instway.app.bean.Notice;
import com.instway.app.common.StringUtils;
import com.instway.app.common.UIHelper;
import com.instway.app.widght.NewDataToast;
import com.instway.app.widght.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.util.LogUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyCarsActivity extends BaseActivity {

    @ViewInject(R.id.title_tv)
    private TextView mTitleTv;

    @ViewInject(R.id.back_img)
    private ImageView backBtn;

    @ViewInject(R.id.right_img)
    private ImageView rightImg;

    private AppContext appContext;

    private Handler lvNewsHandler;
    private List<Car> lvNewsData = new ArrayList<Car>();
    private int lvNewsSumData;
    private CarViewNewsAdapter lvNewsAdapter;

    private TextView lvNews_foot_more;
    private ProgressBar lvNews_foot_progress;
    private PullToRefreshListView lvNews;

    private View lvNews_footer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycars);
        ViewUtils.inject(this);
        mTitleTv.setText(R.string.my_cars);
        //显示返回按钮
        backBtn.setVisibility(View.VISIBLE);
        appContext = (AppContext) getApplicationContext();

        rightImg.setVisibility(View.VISIBLE);
        rightImg.setImageDrawable(getResources().getDrawable(R.drawable.add_btn));
        initViews();
    }

    @Override
    protected void onResume() {
        LogUtils.i("--------------onResume---------------");
        super.onResume();
        loadLvNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_INIT);
    }
    @Override
    protected void onPause() {
        super.onPause();
        loadLvNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_INIT);
    }

    @OnClick({R.id.back_img, R.id.right_img})
    public void buttonClick(View v) {

        switch (v.getId()) {
            case R.id.back_img:
                this.finish();
                break;
            case R.id.right_img:
                UIHelper.showMyCarAddRedirect(appContext);
                break;
        }
    }


    public void initViews() {
        //表格初始化
        this.initNewsListView();
        this.initFrameListViewData();
    }

    /**
     * 初始化所有ListView数据
     */
    public void initFrameListViewData() {
        LogUtils.i("initFrameListViewData---->");
        // 初始化Handler
        lvNewsHandler = this.getLvHandler(lvNews, lvNewsAdapter, lvNews_foot_more, lvNews_foot_progress, AppContext.PAGE_SIZE);
        // 加载资讯数据
        if (lvNewsData.isEmpty()) {
            loadLvNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_INIT);
        }
    }

    /**
     * listview数据处理
     *
     * @param what       数量
     * @param obj        数据
     * @param objtype    数据类型
     * @param actiontype 操作类型
     * @return notice 通知信息
     */
    private Notice handleLvData(int what, Object obj, int objtype, int actiontype) {
        Notice notice = null;
        switch (actiontype) {
            case UIHelper.LISTVIEW_ACTION_INIT:
            case UIHelper.LISTVIEW_ACTION_REFRESH:
            case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
                int newdata = 0;// 新加载数据-只有刷新动作才会使用到
                CarList nlist = (CarList) obj;
                notice = nlist.getNotice();
                lvNewsSumData = what;
                if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    if (lvNewsData.size() > 0) {
                        for (Car news1 : nlist.getNewslist()) {
                            boolean b = false;
                            for (Car news2 : lvNewsData) {
                                if (news1.getId() == news2.getId()) {
                                    b = true;
                                    break;
                                }
                            }
                            if (!b)
                                newdata++;
                        }
                    } else {
                        newdata = what;
                    }
                }
                lvNewsData.clear();// 先清除原有数据
                lvNewsData.addAll(nlist.getNewslist());

                if (actiontype == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    // 提示新加载数据
                    if (newdata > 0) {
                        NewDataToast.makeText(this, getString(R.string.new_data_toast_message, newdata)).show();
                    } else {
                        NewDataToast.makeText(this, getString(R.string.new_data_toast_none, false)).show();
                    }
                }
                break;
            case UIHelper.LISTVIEW_ACTION_SCROLL:
                CarList list = (CarList) obj;
                notice = list.getNotice();
                lvNewsSumData += what;
                if (lvNewsData.size() > 0) {
                    for (Car news1 : list.getNewslist()) {
                        boolean b = false;
                        for (Car news2 : lvNewsData) {
                            if (news1.getId() == news2.getId()) {
                                b = true;
                                break;
                            }
                        }
                        if (!b)
                            lvNewsData.add(news1);
                    }
                } else {
                    lvNewsData.addAll(list.getNewslist());
                }
                break;
        }
        return notice;
    }


    /**
     * 初始化新闻列表
     */
    private void initNewsListView() {
        lvNews_footer = getLayoutInflater().inflate(R.layout.listview_footer,
                null);
        lvNews_foot_progress = (ProgressBar) lvNews_footer.findViewById(R.id.listview_foot_progress);
        lvNews_foot_more = (TextView) lvNews_footer.findViewById(R.id.listview_foot_more);
        lvNews_foot_progress = (ProgressBar) lvNews_footer.findViewById(R.id.listview_foot_progress);

        lvNewsAdapter = new CarViewNewsAdapter(this, lvNewsData, R.layout.car_list_item);

        lvNews = (PullToRefreshListView) findViewById(R.id.listview_cars);
        lvNews.addFooterView(lvNews_footer);// 添加底部视图 必须在setAdapter前
        lvNews.setAdapter(lvNewsAdapter);

        lvNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //点击头部、底部栏无效
                if (position == 0 || view == lvNews_footer)
                    return;

                Car news = null;
                // 判断是否是TextView
                if (view instanceof TextView) {
                    news = (Car) view.getTag();
                } else {
                    TextView tv = (TextView) view
                            .findViewById(R.id.car_no);
                    news = (Car) tv.getTag();
                }
                if (news == null)
                    return;

                // 跳转到新闻详情
                //UIHelper.showCarDetailRedirect(appContext, news);
                UIHelper.showMyEditAddRedirect(appContext, news);
            }
        });
        lvNews.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                lvNews.onScrollStateChanged(view, scrollState);

                // 数据为空--不用继续下面代码了
                if (lvNewsData.isEmpty())
                    return;

                // 判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvNews_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }
                LogUtils.i("scroll 到底了 = " + scrollEnd);
                int lvDataState = StringUtils.toInt(lvNews.getTag());
                if (scrollEnd && lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    LogUtils.i("scroll 到底了...加载数据");
                    lvNews.setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    lvNews_foot_more.setText(R.string.load_ing);
                    lvNews_foot_progress.setVisibility(View.VISIBLE);
                    // 当前pageIndex
                    int pageIndex = lvNewsSumData / AppContext.PAGE_SIZE;
                    loadLvNewsData(pageIndex, lvNewsHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lvNews.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        lvNews.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadLvNewsData(0, lvNewsHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }


    private void loadLvNewsData(final int pageIndex, final Handler handler, final int action) {
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    //额外参数
                    Map<String, Object> params = new HashMap<String, Object>();
                    CarList list = appContext.getCarList(pageIndex, isRefresh, params);
                    msg.what = 0;
                    msg.obj = list;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;
                msg.arg2 = UIHelper.LISTVIEW_DATATYPE_NEWS;
                handler.sendMessage(msg);
            }
        }.start();
    }


    /**
     * 获取listview的初始化Handler
     *
     * @param lv
     * @param adapter
     * @return
     */
    private Handler getLvHandler(final PullToRefreshListView lv, final BaseAdapter adapter, final TextView more, final ProgressBar progress, final int pageSize) {
        return new Handler() {

            public void handleMessage(Message msg) {
                Notice notice = handleLvData(msg.what, msg.obj, msg.arg2, msg.arg1);
                if (msg.what >= 0) {
                    if (msg.what < pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_FULL);
                        adapter.notifyDataSetChanged();
//                        more.setText(R.string.load_full);
                        more.setText(R.string.load_full);
                    } else if (msg.what == pageSize) {
                        lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                        adapter.notifyDataSetChanged();
                        more.setText(R.string.load_more);
                    }
                } else if (msg.what == -1) {
                    // 有异常--显示加载出错 & 弹出错误消息
                    lv.setTag(UIHelper.LISTVIEW_DATA_MORE);
                    if (!notice.getCode().equals("1")) {
                        NewDataToast.makeText(appContext, notice.getMsg()).show();
                    }
                }
                if (adapter.getCount() == 0) {
                    lv.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
                }
                progress.setVisibility(ProgressBar.GONE);
                if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    lv.onRefreshComplete();
                    lv.setSelection(0);
                } else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
                    lv.onRefreshComplete();
                    lv.setSelection(0);
                }
            }
        };
    }
}
