package com.example.leoymr.insta;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.leoymr.insta.Adapter.AlbumAdapter;
import com.example.leoymr.insta.Data.footprint_Info.HeadHashMap;
import com.example.leoymr.insta.Data.footprint_Info.comment_Info;
import com.example.leoymr.insta.Data.footprint_Info.content_Info;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by leoymr on 24/4/17.
 */

public class albumActivity extends AppCompatActivity {

    private static final String TAG = "albumActivity";
    private String user_id_intent;
    private ListView albumListview;
    private AlbumAdapter albumAdapter = null;
    private List<content_Info> content_infos = new ArrayList<>();//存放每条足迹content
    private HashMap<String, Integer> headList = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("我的足迹");


        Intent intent = getIntent();
        user_id_intent = intent.getStringExtra("user_id");

        HeadHashMap headmap = new HeadHashMap();
        headList = headmap.initHeadList();

        albumListview = (ListView) findViewById(R.id.tracepoint_listview);

        new albumAsyncTask().execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("返回成功", "onOptionsItemSelected: ");
                Intent intent2 = new Intent(albumActivity.this, personActivity.class);
                if (!user_id_intent.equals("")) {
                    Log.d("user_id", user_id_intent);
                    intent2.putExtra("user_id", user_id_intent);
                }
                startActivity(intent2);
                break;
        }
        return true;
    }

    //发送okhttp请求，异步操作
    class albumAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject json = null;
            json = sendRequestWithOkHttp();
            return json;

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {

                parseJSONwithJSONObject(jsonObject);
                albumAdapter = new AlbumAdapter(albumActivity.this, content_infos);
                if (content_infos != null) {
                    Log.d("onCreate: ", "not null");
                    albumListview.setAdapter(albumAdapter);
                }
            }
        }

        private void parseJSONwithJSONObject(JSONObject jsonObject) {

            JSONArray jsonArray_trace = null;
            JSONArray jsonArray_comment = null;
            JSONArray jsonArray_like = null;

            try {
                jsonArray_trace = jsonObject.getJSONArray("trace_data");
                jsonArray_comment = jsonObject.getJSONArray("comment_data");
                jsonArray_like = jsonObject.getJSONArray("like_data");

                for (int i = 0; i < jsonArray_trace.length(); i++) {
                    String date = jsonArray_trace.getJSONObject(i).getString("date");

                    SimpleDateFormat sdf = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                    Date dateTime =sdf.parse(date);

                    sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    date = sdf.format(dateTime);
                    Log.d("date", date);
                    String text = jsonArray_trace.getJSONObject(i).getString("text");

                    String user_name = jsonArray_trace.getJSONObject(i).getString("user_name");
                    String trace_id_content = jsonArray_trace.getJSONObject(i).getString("trace_id");

                    String user_head = jsonArray_trace.getJSONObject(i).getString("user_head");
                    int headResource = headList.get(user_head);

                    //添加到adapter
                    content_Info content = new content_Info();
                    content.setUser_name(user_name);
                    content.setContent(text);
                    content.setLocation_name(date);
                    content.setTrace_id(trace_id_content);
                    content.setResourceId(headResource);
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

                    //加载like list
                    int like_count = 0;//点赞数量
                    for (int j = 0; j < jsonArray_like.length(); j++) {
                        String trace_id_like = jsonArray_like.getJSONObject(j).getString("trace_id");
                        String user_id_like = jsonArray_like.getJSONObject(j).getString("user_id");
                        Log.d("like_user_id", user_id_like);
                        if (trace_id_like.equals(trace_id_content)) {
                            like_count++;
                            content.setLikeFocus(true);
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

            url = String.format("http://54.254.206.29/api/get_person_traces?%s", "user_id=" + user_id_intent);
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

}
