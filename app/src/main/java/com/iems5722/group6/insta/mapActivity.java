package com.iems5722.group6.insta;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.iems5722.group6.insta.Adapter.CommentAdapter;
import com.iems5722.group6.insta.Data.footprint_Info.HeadHashMap;
import com.iems5722.group6.insta.Data.footprint_Info.comment_Info;
import com.iems5722.group6.insta.Data.footprint_Info.content_Info;
import com.iems5722.group6.insta.GeoHash.geoHash;
import com.iems5722.group6.insta.GpsClass.Gps;
import com.iems5722.group6.insta.PopupWindow.ActionItem;
import com.iems5722.group6.insta.PopupWindow.TitlePopup;
import com.iems5722.group6.insta.PopupWindow.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.iems5722.group6.insta.GpsClass.PositionUtil.gcj02_To_Bd09;
import static com.iems5722.group6.insta.GpsClass.PositionUtil.gcj_To_Gps84;

/**
 * Created by leoymr on 8/4/17.
 */

public class mapActivity extends AppCompatActivity implements TitlePopup.OnItemOnClickListener, View.OnClickListener {

    public LocationClient mLocationClient;

    private String TAG = "mapActivity";
    private String POST = "POST like";
    private String user_id_intent;
    private String trace_id_intent;

    private ListView content_listView;
    private TitlePopup titlePopup;
    private TextView likeNum;

    private List<content_Info> content_infos = new ArrayList<>();//存放每条足迹content
    private CommentAdapter commentAdapter = null;

    private MapView mapView;
    private BaiduMap baiduMap;
    private BitmapDescriptor bitmap;

    private double latitude;
    private double longitude;

    private double lat;
    private double lont;

    private List<String> geoHashList;//存放9个当前位置周围的geohash

    private boolean like_flag = false;//点赞标志
    private int position_like = 0;
    private int code_like = 0;

    private int flag = 0;//悬浮按钮标志
    private int width, height;
    private int[] pic = {R.id.center, R.id.addmore, R.id.refresh, R.id.checked};
    private List<ImageView> imageViewList = new ArrayList<>();
    private HashMap<String, Integer> headList = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HeadHashMap headmap = new HeadHashMap();
        headList = headmap.initHeadList();

        Intent intent = getIntent();
        user_id_intent = intent.getStringExtra("user_id");
        Log.d("login user_id", user_id_intent);

        setTitle("足迹");
        likeNum = (TextView) findViewById(R.id.like_num);

        //地理位置服务
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd0911");

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_footprint);

        //初始化百度地图view
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        mapView.removeViewAt(1);
        UiSettings uiSettings = baiduMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(false);

        // 设置marker图标
        bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.t_icon);

        List<String> permissionList = new ArrayList<>();

        requestLocation();

        //moments内容 listview
        content_listView = (ListView) findViewById(R.id.footprint_Listview);

        titlePopup = new TitlePopup(this, Util.dip2px(this, 230), Util.dip2px(
                this, 40));
        titlePopup.addAction(new ActionItem(this, "like", R.mipmap.circle_praise));
        titlePopup.addAction(new ActionItem(this, "replay", R.mipmap.circle_comment));
        titlePopup.addAction(new ActionItem(this, "mark", R.mipmap.locate));

        getWindow().setFormat(PixelFormat.RGBA_8888);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        width = metric.widthPixels;  // 宽度（PX）
        height = metric.heightPixels;  // 高度（PX）

        for (int i = 0; i < pic.length; i++) {
            ImageView imageView = (ImageView) findViewById(pic[i]);
            imageView.setOnClickListener(this);
            imageViewList.add(imageView);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onResume();
        Log.d(TAG, "onRestart: ");
        //new mapAsyncTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.center:
                if (flag == 0)
                    startAni();
                else if (flag == 1)
                    closeAni();
                break;
            case R.id.addmore:
                Intent intent_add = new Intent(this, publishActivity.class);
                intent_add.putExtra("user_id", user_id_intent);
                startActivity(intent_add);
                break;
            case R.id.refresh:
                closeAni();
                Snackbar.make(v, "刷新成功", Snackbar.LENGTH_SHORT).show();
                if (content_infos != null)
                    content_infos.clear();
                baiduMap.clear();
                new mapAsyncTask().execute();
                break;
            case R.id.checked:
                Intent intent_check = new Intent(this, personActivity.class);
                intent_check.putExtra("user_id", user_id_intent);
                startActivity(intent_check);
                break;
        }

    }

    private void startAni() {

        for (int i = 1; i < pic.length; i++) {
            float x = (float) (width / 3 * Math.cos(Math.PI * (i + 1) / 2));
            float y = (float) (height / 6 * Math.sin(Math.PI * (i + 1) / 2));
            ObjectAnimator oa = ObjectAnimator.ofFloat(imageViewList.get(i), "translationY", 0F, y * 0.8f);
            ObjectAnimator ob = ObjectAnimator.ofFloat(imageViewList.get(i), "translationX", 0F, x * 0.8f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(oa, ob);
            set.setInterpolator(new BounceInterpolator());
            set.setDuration(500);

            set.start();
        }
        flag = 1;
    }

    private void closeAni() {

        for (int i = 1; i < pic.length; i++) {
            float x = (float) (width / 3 * Math.cos(Math.PI * (i + 1) / 2));
            float y = (float) (height / 6 * Math.sin(Math.PI * (i + 1) / 2));
            ObjectAnimator oa = ObjectAnimator.ofFloat(imageViewList.get(i), "translationY", y * 0.8f, 0F);
            ObjectAnimator ob = ObjectAnimator.ofFloat(imageViewList.get(i), "translationX", x * 0.8f, 0F);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(oa, ob);

            set.setDuration(500);
            set.setInterpolator(new BounceInterpolator());
            set.start();

        }
        flag = 0;
    }

    /**
     * 百度地图方法类
     * <p>
     * 显示地图中当前位置
     */

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

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        mLocationClient.setLocOption(option);

    }

    //地图移动到当前位置
    private void navigateTo(BDLocation location) {

        Gps gcj = new Gps(location.getLatitude(), location.getLongitude());
        Log.d("gcj :", gcj.toString());
        ;

        Gps bd = gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
        Log.d("wgs :", bd.toString());
        ;

        LatLng ll = new LatLng(bd.getWgLat(), bd.getWgLon());

        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        baiduMap.animateMapStatus(update);

        MyLocationData.Builder locationBuiler = new MyLocationData.Builder();


        locationBuiler.latitude(bd.getWgLat());
        latitude = location.getLatitude();
        Log.d("my location latitude", String.valueOf(latitude));

        locationBuiler.longitude(bd.getWgLon());
        longitude = location.getLongitude();
        Log.d("my location Longitude", String.valueOf(longitude));

        MyLocationData locationData = locationBuiler.build();
        baiduMap.setMyLocationData(locationData);

        //获取当前位置的周围9个Geohash
        geoHash g = new geoHash(latitude, longitude);  //先输入纬度，再输入经度
        System.out.println(g.getGeoHashBase32());
        geoHashList = g.getGeoHashBase32For9();

        //测试获取geohash是否成功
        /*for (String base : geoHashList) {
            Log.d("9 geohash", base);
        }*/
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

    //Popup Window 点击事件
    @Override
    public void onItemClick(ActionItem item, int position) {
        switch (position) {
            case 0:
                baiduMap.clear();
                // 定义Maker坐标点
                Gps gcj = new Gps(lat, lont);
                Gps bd = gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
                LatLng point = new LatLng(bd.getWgLat(), bd.getWgLon());
                // 构建MarkerOption，用于在地图上添加Marker
                MarkerOptions options = new MarkerOptions().position(point).icon(bitmap).animateType(MarkerOptions.MarkerAnimateType.grow);
                // 在地图上添加Marker，并显示
                Log.d("marker", String.valueOf(bd));
                baiduMap.addOverlay(options);
                break;
            case 1:
                Log.d(user_id_intent, " like " + trace_id_intent);
                new mapAsyncTask().execute();
                like_flag = true;
                break;
            case 2:
                Intent intent_replay = new Intent(this, commentActivity.class);
                intent_replay.putExtra("user_id", user_id_intent);
                intent_replay.putExtra("trace_id", trace_id_intent);
                startActivity(intent_replay);
                break;
        }
    }

    //发送okhttp请求，异步操作
    class mapAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject json = null;
            if (like_flag == true) {
                code_like = postRequestWithOkHttp();

                //code_like=200 即用户未点赞
                if (code_like == 200) {

                    like_flag = false;
                }
                return null;

            } else {
                json = sendRequestWithOkHttp();
                return json;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {

                parseJSONwithJSONObject(jsonObject);
                commentAdapter = new CommentAdapter(mapActivity.this, content_infos);
                if (content_infos != null) {
                    Log.d("onCreate: ", "not null");
                    content_listView.setAdapter(commentAdapter);
                }
            } else if (code_like == 200) {
                commentAdapter = new CommentAdapter(mapActivity.this, content_infos);
                if (content_infos != null) {
                    Log.d("like success", "true");
                    Toast.makeText(mapActivity.this, "赞+1", Toast.LENGTH_SHORT).show();
                    int count = content_infos.get(position_like).getLikeNum();
                    count++;
                    content_infos.get(position_like).setLikeNum(count);
                    if (!content_infos.get(position_like).isLikeFocus())
                        content_infos.get(position_like).setLikeFocus(true);
                    Log.d("like num ", String.valueOf(count));

                    commentAdapter.notifyDataSetChanged();
                    content_listView.setSelection(position_like);
                }

            } else if (code_like == 400) {
                Log.d("popup: code_like", String.valueOf(code_like));
                Toast.makeText(mapActivity.this, "点过赞了", Toast.LENGTH_SHORT).show();
            }

            //listview item点击事件
            content_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    position_like = position;
                    Log.d("onItemClick: ", String.valueOf(position_like));
                    content_Info contentInfo = content_infos.get(position);
                    trace_id_intent = contentInfo.getTrace_id();
                    lat = contentInfo.getLatitude();
                    lont = contentInfo.getLongitude();

                    titlePopup.setItemOnClickListener(mapActivity.this);
                    titlePopup.setAnimationStyle(R.style.cricleBottomAnimation);
                    titlePopup.show(view);
                }
            });
        }

        private void parseJSONwithJSONObject(JSONObject jsonObject) {

            JSONArray jsonArray_trace = null;
            JSONArray jsonArray_comment = null;
            JSONArray jsonArray_like = null;
            SimpleDateFormat format;
            try {
                jsonArray_trace = jsonObject.getJSONArray("trace_data");
                jsonArray_comment = jsonObject.getJSONArray("comment_data");
                jsonArray_like = jsonObject.getJSONArray("like_data");

                for (int i = 0; i < jsonArray_trace.length(); i++) {


                    String date = jsonArray_trace.getJSONObject(i).getString("date");

                    SimpleDateFormat sdf = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                    Date dateTime = sdf.parse(date);

                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    date = sdf.format(dateTime);
                    //Log.d("date", date);

                    String text = jsonArray_trace.getJSONObject(i).getString("text");

                    String user_name = jsonArray_trace.getJSONObject(i).getString("user_name");
                    String trace_id_content = jsonArray_trace.getJSONObject(i).getString("trace_id");

                    String user_head = jsonArray_trace.getJSONObject(i).getString("user_head");
                    int headResource = headList.get(user_head);

                    double lat_1 = Double.parseDouble(jsonArray_trace.getJSONObject(i).getString("latitude"));
                    //Log.d(user_name, String.valueOf(lat));
                    double lont_1 = Double.parseDouble(jsonArray_trace.getJSONObject(i).getString("longitude"));
                    //Log.d(user_name, String.valueOf(lont));

                    // 定义Maker坐标点
                    Gps gcj = new Gps(lat_1, lont_1);
                    Log.d("gcj :", gcj.toString());

                    Gps bd = gcj02_To_Bd09(gcj.getWgLat(), gcj.getWgLon());
                    Log.d("wgs :", bd.toString());

                    LatLng point = new LatLng(bd.getWgLat(), bd.getWgLon());
                    // 构建MarkerOption，用于在地图上添加Marker
                    MarkerOptions options = new MarkerOptions().position(point).icon(bitmap).animateType(MarkerOptions.MarkerAnimateType.grow);
                    // 在地图上添加Marker，并显示
                    baiduMap.addOverlay(options);

                    //添加到adapter
                    content_Info content = new content_Info();
                    content.setUser_id(user_id_intent);
                    content.setUser_name(user_name);
                    content.setContent(text);
                    content.setLocation_name(date);
                    content.setTrace_id(trace_id_content);
                    content.setResourceId(headResource);
                    content.setLongitude(lont_1);
                    content.setLatitude(lat_1);
                    List<comment_Info> users = new ArrayList<>();

                    //加载comment list
                    for (int j = 0; j < jsonArray_comment.length(); j++) {
                        comment_Info user = new comment_Info();
                        String trace_id_comment = jsonArray_comment.getJSONObject(j).getString("trace_id");

                        if (trace_id_comment.equals(trace_id_content)) {
                            //Log.d("trace_id_comment", trace_id_comment);
                            user.setComments(jsonArray_comment.getJSONObject(j).getString("text"));
                            user.setUser_name(jsonArray_comment.getJSONObject(j).getString("user_name") + " : ");
                            users.add(user);
                            //测试每条comments
                            /*Log.d(TAG, String.valueOf(jsonArray_comment.getJSONObject(j).getString("text")));
                            Log.d(TAG, String.valueOf(jsonArray_comment.getJSONObject(j).getString("user_name")));*/
                        }
                    }

                    //加载like list
                    int like_count = 0;//点赞数量
                    for (int j = 0; j < jsonArray_like.length(); j++) {
                        String trace_id_like = jsonArray_like.getJSONObject(j).getString("trace_id");
                        String user_id_like = jsonArray_like.getJSONObject(j).getString("user_id");
                        //Log.d("like_user_id", user_id_like);
                        if (trace_id_like.equals(trace_id_content)) {
                            like_count++;
                            if (user_id_like.equals(user_id_intent)) {
                                content.setLikeFocus(true);
                            } else {
                                content.setLikeFocus(false);
                            }
                        }
                    }
                    content.setLikeNum(like_count);
                    content.setComment_list(users);
                    content_infos.add(0, content);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
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
                url = String.format("http://54.254.206.29/api/get_traces?%s&%s&%s&%s&%s&%s&%s&%s&%s",
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

        /**
         * Sending POST request of  with Okhttp
         */
        private int postRequestWithOkHttp() {
            JSONObject json = null;
            int code = 0;

            RequestBody requestBody = new FormBody.Builder()
                    .add("trace_id", trace_id_intent)
                    .add("user_id", user_id_intent)
                    .build();
            String url = "http://54.254.206.29/api/publish_like";
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
                code = json.getInt("code");
                Log.d(POST, String.valueOf(json.get("status")));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(POST, "POST request error");
            }
            return code;
        }
    }


}
