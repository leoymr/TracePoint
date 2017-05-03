package com.iems5722.group6.insta;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.iems5722.group6.insta.GeoHash.geoHash;
import com.iems5722.group6.insta.GpsClass.Gps;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.iems5722.group6.insta.GpsClass.PositionUtil.gcj02_To_Bd09;

/**
 * Created by leoymr on 21/4/17.
 *
 * 发布新足迹页面activity
 */

public class publishActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "publishActivity";
    private static final String POST = "post trace point";

    public LocationClient mLocationClient;

    private double longitude;
    private double latitude;
    private geoHash geoH;
    private String geohash;
    private MapView pub_mapView;
    private BaiduMap pub_baiduMap;
    private boolean pub_isFirstLocate = true;


    private Button sendContent;
    private EditText editText_content;

    private String user_id_intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("发布足迹");

        //地理位置服务
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListenerPublish());

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_publish);

        //初始化百度地图view
        pub_mapView = (MapView) findViewById(R.id.publish_bmapView);
        pub_baiduMap = pub_mapView.getMap();
        pub_mapView.removeViewAt(1);
        pub_baiduMap.setMyLocationEnabled(true);

        UiSettings uiSettings = pub_baiduMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);

        sendContent = (Button) findViewById(R.id.publish_send);
        editText_content = (EditText) findViewById(R.id.publish_content);

        sendContent.setOnClickListener(this);

        Intent intent = getIntent();
        user_id_intent = intent.getStringExtra("user_id");
        Log.d("user_id", user_id_intent);

        List<String> permissionList = new ArrayList<>();

        //检测权限list，若无则添加
        if (ContextCompat.checkSelfPermission(publishActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(publishActivity.this,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(publishActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(publishActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pub_mapView.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        pub_mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pub_mapView.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("返回成功", "onOptionsItemSelected: ");
                Intent intent2 = new Intent(publishActivity.this, mapActivity.class);
                if (!user_id_intent.equals("")) {
                    Log.d("user_id", user_id_intent);
                    intent2.putExtra("user_id", user_id_intent);
                    intent2.putExtra("intent_flag", "1");
                    startActivity(intent2);
                }
                break;
        }
        return true;
    }

    /**
     * 百度地图方法类集合
     */
    private void navigateTo(BDLocation location) {
        Gps gcj = new Gps(location.getLatitude(), location.getLongitude());
        Log.d("gcj :", gcj.toString());;

        Gps bd = gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
        Log.d("wgs :", bd.toString());;

        LatLng ll = new LatLng(bd.getWgLat(), bd.getWgLon());
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        pub_baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        pub_baiduMap.animateMapStatus(update);
        pub_isFirstLocate = false;

        MyLocationData.Builder locationBuiler = new MyLocationData.Builder();
        locationBuiler.latitude(bd.getWgLat());
        latitude = location.getLatitude();
        Log.d("latitude", String.valueOf(latitude));

        locationBuiler.longitude(bd.getWgLon());
        longitude = location.getLongitude();
        Log.d("Longitude", String.valueOf(longitude));

        MyLocationData locationData = locationBuiler.build();
        pub_baiduMap.setMyLocationData(locationData);

        //获取当前位置的Geohash
        geoH = new geoHash(latitude, longitude);  //先输入纬度，再输入经度
        //Log.d("当前位置的geohash", geoH.getGeoHashBase32());
        geohash = geoH.getGeoHashBase32();

    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListenerPublish implements BDLocationListener {
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

    @Override
    public void onClick(View v) {
        String msg = editText_content.getText().toString().trim();
        switch (v.getId()) {
            case R.id.publish_send:
                if (msg.equals("")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("发送不能为空 !!!");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();
                } else {
                    new publishAsyncTask().execute(msg);
                    Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT);
                    Log.d("内容", msg);

                    Intent intent2 = new Intent(publishActivity.this, mapActivity.class);
                    if (!user_id_intent.equals("")) {
                        Log.d("publish_user_id", user_id_intent);
                        intent2.putExtra("user_id", user_id_intent);
                        intent2.putExtra("intent_flag", "1");
                        startActivity(intent2);
                    }
                }
                break;
        }
    }

    class publishAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject json = postRequestWithOkHttp(params[0]);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

        }

        /**
         * Sending POST request of  with Okhttp
         *
         * @param msg messages
         */
        private JSONObject postRequestWithOkHttp(String msg) {
            JSONObject json = null;


            RequestBody requestBody = new FormBody.Builder()
                    .add("user_id", user_id_intent)
                    .add("text", msg)
                    .add("longitude", String.valueOf(longitude))
                    .add("latitude", String.valueOf(latitude))
                    .add("geohash", geohash)
                    .build();
            String url = "http://54.254.206.29/api/publish_trace";
            OkHttpClient client = new OkHttpClient();
            Log.d(POST, url);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                json = new JSONObject(responseData);
                Log.d(POST, String.valueOf(json.get("status")));
                Log.d(POST, String.valueOf(json.get("message")));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(POST, "POST request error");
            }
            return json;
        }
    }

}
