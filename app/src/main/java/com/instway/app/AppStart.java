package com.instway.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.instway.app.R;
import com.instway.app.api.ApiClient;
import com.instway.app.bean.URLs;
import com.instway.app.ui.HomeActivity;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.HashMap;

/**
 * 启动界面
 */
public class AppStart extends Activity {

//    @ViewInject(R.id.main_imageview)
//    private LoadingView mainImageview;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private AppContext appContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = (AppContext) getApplicationContext();

        final View view = View.inflate(this, R.layout.activity_start, null);
        setContentView(view);
        ViewUtils.inject(this);
        //initLoadingImages();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                redirectTo();
                finish();
            }
        }, 1500);
        startLocation();
    }

    /**
     * 定位
     */
    private void startLocation() {
        mLocationClient = new LocationClient(appContext);     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(60000);//设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null)
                return;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("latitude", location.getLatitude());
            params.put("longitude", location.getLongitude());
            params.put("code", location.getLocType());
            params.put("time", location.getTime());
            //地址投递
            String newUrl = ApiClient._MakeURL(URLs.LOCATION_DRIVER, params, appContext);
            HttpUtils http = new HttpUtils();
            http.send(HttpRequest.HttpMethod.POST, newUrl, null, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> objectResponseInfo) {
                }

                @Override
                public void onFailure(HttpException e, String s) {
                }
            });
        }
    }


//    private void initLoadingImages() {
//        int[] imageIds = new int[6];
//        imageIds[0] = R.drawable.loader_frame_1;
//        imageIds[1] = R.drawable.loader_frame_2;
//        imageIds[2] = R.drawable.loader_frame_3;
//        imageIds[3] = R.drawable.loader_frame_4;
//        imageIds[4] = R.drawable.loader_frame_5;
//        imageIds[5] = R.drawable.loader_frame_6;
//
//        mainImageview.setImageIds(imageIds);
//        new Thread() {
//            @Override
//            public void run() {
//                mainImageview.startAnim();
//            }
//        }.start();
//    }

    /**
     * 跳转到...
     */
    private void redirectTo() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}