package com.example.leoymr.insta;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.leoymr.insta.Adapter.CommentAdapter;
import com.example.leoymr.insta.Data.footprint_Info.UtilAngle;
import com.example.leoymr.insta.Data.footprint_Info.comment_Info;
import com.example.leoymr.insta.Data.footprint_Info.content_Info;
import com.example.leoymr.insta.GeoHash.geoHash;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wx.goodview.GoodView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by leoymr on 8/4/17.
 */

public class mapActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private String TAG = "mapActivity";

    private ListView content_listView;

    private GoodView goodView;
    private boolean likeFlag;

    private List<content_Info> content_infos = new ArrayList<>();//存放每条足迹content
    private CommentAdapter commentAdapter = null;

    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;

    private double latitude;
    private double longitude;

    private List<String> geoHashList;//存放9个当前位置周围的geohash


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //地理位置服务
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_footprint);

        //初始化百度地图view
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        List<String> permissionList = new ArrayList<>();

        //检测权限list，若无则添加
        if (ContextCompat.checkSelfPermission(mapActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(mapActivity.this,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(mapActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(mapActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        //moments内容 listview
        content_listView = (ListView) findViewById(R.id.footprint_Listview);



        goodView = new GoodView(this);

    }

    /**
     * 点赞按钮事件
     *
     * @param v
     */

    public void likeBtn(View v) {
        if (!likeFlag) {
            ((ImageView) v).setImageResource(R.mipmap.collection_checked);
            goodView.setText("+1");
            goodView.show(v);
            likeFlag = true;
        } else {
            ((ImageView) v).setImageResource(R.mipmap.collection);
            goodView.setText("-1");
            goodView.show(v);
            likeFlag = false;
        }

    }

    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }

        MyLocationData.Builder locationBuiler = new MyLocationData.Builder();
        locationBuiler.latitude(location.getLatitude());
        latitude = location.getLatitude();
        Log.d("latitude", String.valueOf(latitude));

        locationBuiler.longitude(location.getLongitude());
        longitude = location.getLongitude();
        Log.d("Longitude", String.valueOf(longitude));

        MyLocationData locationData = locationBuiler.build();
        baiduMap.setMyLocationData(locationData);

        //获取当前位置的周围9个Geohash
        geoHash g = new geoHash(latitude, longitude);  //先输入纬度，再输入经度
        System.out.println(g.getGeoHashBase32());
        geoHashList = g.getGeoHashBase32For9();
        //测试获取geohash是否成功
        for (String base : geoHashList) {
            Log.d("9 geohash", base);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    private void requestLocation() {
        //initLocation();
        mLocationClient.start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    /*//悬浮按钮点击事件
    public void onFABClick(View v) {
        switch (v.getId()) {
            case R.id.fabBtn:
                *//*Snackbar.make(v, "刷新成功", Snackbar.LENGTH_SHORT).show();
                if (content_infos != null)
                    content_infos.clear();*//*
                //new mapAsyncTask().execute();
                buttonAnimation(buttonItems,0);
                break;
        }

    }*/

    public void onClick(View v) {
    }

    class mapAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject json = sendRequestWithOkHttp();
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            JSONArray jsonArray_trace = null;
            JSONArray jsonArray_comment = null;

            try {
                jsonArray_trace = jsonObject.getJSONArray("trace_data");
                jsonArray_comment = jsonObject.getJSONArray("comment_data");

                for (int i = 0; i < jsonArray_trace.length(); i++) {
                    String date = jsonArray_trace.getJSONObject(i).getString("date");
                    String text = jsonArray_trace.getJSONObject(i).getString("text");
                    String user_name = jsonArray_trace.getJSONObject(i).getString("user_name");
                    String trace_id_content = jsonArray_trace.getJSONObject(i).getString("trace_id");

                    content_Info content = new content_Info();
                    content.setUser_name(user_name);
                    content.setContent(text);
                    content.setLocation_name(date);

                    List<comment_Info> users = new ArrayList<>();
                    //加载comment list
                    for (int j = 0; j < jsonArray_comment.length(); j++) {
                        comment_Info user = new comment_Info();
                        String trace_id_comment = jsonArray_comment.getJSONObject(j).getString("trace_id");

                        if (trace_id_comment.equals(trace_id_content)) {
                            Log.d("trace_id_comment", trace_id_comment);
                            user.setComments(jsonArray_comment.getJSONObject(j).getString("text"));
                            user.setUser_name(jsonArray_comment.getJSONObject(j).getString("user_name") + ": ");
                            users.add(user);
                            //测试每条comments
                            Log.d(TAG, String.valueOf(jsonArray_comment.getJSONObject(j).getString("text")));
                            Log.d(TAG, String.valueOf(jsonArray_comment.getJSONObject(j).getString("user_name")));
                        }
                    }
                    content.setComment_list(users);
                    content_infos.add(0, content);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            commentAdapter = new CommentAdapter(mapActivity.this, content_infos);
            if (content_infos != null) {
                Log.d("onCreate: ", "not null");
                content_listView.setAdapter(commentAdapter);
            }
        }

        /**
         * Sending GET request with Okhttp
         */
        private JSONObject sendRequestWithOkHttp() {
            Response response = null;
            String code = null;
            JSONObject json = null;
            String url = null;
            Log.d("geohash 0 ", geoHashList.get(0));

            if (geoHashList != null) {
                url = String.format("http://54.254.206.29:5000/api/get_traces?%s&%s&%s&%s&%s&%s&%s&%s&%s",
                        "geohash_1=" + geoHashList.get(0), "geohash_2=" + geoHashList.get(1),
                        "geohash_3=" + geoHashList.get(2), "geohash_4=" + geoHashList.get(3),
                        "geohash_5=" + geoHashList.get(4), "geohash_6=" + geoHashList.get(5),
                        "geohash_7=" + geoHashList.get(6), "geohash_8=" + geoHashList.get(7),
                        "geohash_9=" + geoHashList.get(8));
                Log.d("url", url);
            }

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                response = client.newCall(request).execute();
                Log.d(TAG, String.valueOf(response.code()));

                String responseData = response.body().string();
                json = new JSONObject(responseData);
                code = json.get("code").toString();
                if (json.get("status").equals("OK")) {
                    Log.d(TAG, "刷新成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, code);
            return json;
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation ||
                    bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(bdLocation);
            }

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }
}
