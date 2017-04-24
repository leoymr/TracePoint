package com.example.leoymr.insta;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.leoymr.insta.Adapter.CommentAdapter;
import com.example.leoymr.insta.Adapter.PersonAdapter;
import com.example.leoymr.insta.Data.footprint_Info.HeadHashMap;
import com.example.leoymr.insta.Data.footprint_Info.PersonalData;
import com.example.leoymr.insta.Data.footprint_Info.comment_Info;
import com.example.leoymr.insta.Data.footprint_Info.content_Info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 23/4/17.
 */

public class personActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String POST = "post person";
    private static final String TAG = "personActivity";
    private String user_id_intent;
    private HashMap<String, Integer> headList = new HashMap<>();

    private Button logoutBtn;
    private ListView personListview;
    private PersonAdapter personAdapter;
    private List<PersonalData> personList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        setTitle("个人中心");


        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HeadHashMap headmap = new HeadHashMap();
        headList = headmap.initHeadList();

        Intent intent = getIntent();
        user_id_intent = intent.getStringExtra("user_id");
        Log.d("person_user_id", user_id_intent);

        personListview = (ListView) findViewById(R.id.person_listview);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        new personAsyncTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("返回成功", "onOptionsItemSelected: ");
                Intent intent2 = new Intent(personActivity.this, mapActivity.class);
                if (!user_id_intent.equals("")) {
                    Log.d("user_id", user_id_intent);
                    intent2.putExtra("user_id", user_id_intent);
                }
                startActivity(intent2);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutBtn:
                Intent intent = new Intent(this, loginActivity.class);
                startActivity(intent);
                break;
        }

    }

    //发送okhttp请求，异步操作
    class personAsyncTask extends AsyncTask<String, Void, JSONObject> {

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
                personAdapter = new PersonAdapter(personActivity.this, R.layout.item_person, personList);
                personAdapter.notifyDataSetChanged();
                personListview.setAdapter(personAdapter);

            }

            //listview item点击事件
            personListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PersonalData pData = personList.get(position);
                    String titlename = pData.getItemname();
                    if (titlename.equals("头像")) {
                        Intent i = new Intent(personActivity.this, changeHeadActivity.class);
                        i.putExtra("user_id", user_id_intent);
                        startActivity(i);
                    } else if (titlename.equals("用户名")) {
                        Intent i = new Intent(personActivity.this, changeUserNameActivity.class);
                        i.putExtra("user_id", user_id_intent);
                        startActivity(i);
                    } else if (titlename.equals("足迹")) {
                        Intent i = new Intent(personActivity.this, albumActivity.class);
                        i.putExtra("user_id", user_id_intent);
                        startActivity(i);
                    }
                }
            });
        }

        private void parseJSONwithJSONObject(JSONObject jsonObject) {

            try {
                String user_head = jsonObject.get("user_head").toString();
                String user_name = jsonObject.get("user_name").toString();
                String count = jsonObject.get("count_trace").toString();
                int headResource = headList.get(user_head);
                Log.d("head id", String.valueOf(headResource));
                personList.add(new PersonalData("头像", headResource, user_head));
                personList.add(new PersonalData("用户名", R.mipmap.name, user_name));
                personList.add(new PersonalData("足迹", R.mipmap.album, "已发布" + count + "个"));

            } catch (JSONException e) {
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

            url = String.format("http://54.254.206.29/api/request_info?%s", "user_id=" + user_id_intent);
            Log.d("url", url);


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                response = client.newCall(request).execute();

                String responseData = response.body().string();
                json = new JSONObject(responseData);
                code = json.get("code").toString();
                if (json.get("status").equals("OK")) {
                    Log.d(TAG, "刷新成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "response " + code);
            return json;
        }

    }
}
