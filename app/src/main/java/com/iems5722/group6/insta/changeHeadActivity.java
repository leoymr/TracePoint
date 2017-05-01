package com.iems5722.group6.insta;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.iems5722.group6.insta.Adapter.HeadsAdapter;
import com.iems5722.group6.insta.Data.footprint_Info.HeadHashMap;
import com.iems5722.group6.insta.Data.footprint_Info.Heads;

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

public class changeHeadActivity extends AppCompatActivity {
    private static final String POST = "post head";
    private List<Heads> headsList = new ArrayList<>();
    private List<String> headsNameList = new ArrayList<>();
    private HashMap<String, Integer> headHashMap = new HashMap<>();
    private String user_id_intent;
    private String head_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.head_listview);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("更改头像");

        initHeads();
        HeadHashMap headmap = new HeadHashMap();
        headHashMap = headmap.initHeadList();
        final Intent intent = getIntent();
        user_id_intent = intent.getStringExtra("user_id");
        //user_id_intent = "2";

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.head_recyclerview);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        HeadsAdapter headsAdapter = new HeadsAdapter(headsList);
        recyclerView.setAdapter(headsAdapter);

        headsAdapter.setmOnItemClickListener(new HeadsAdapter.OnItemClickListener() {
            @Override
            public void setOnItemClick(View view, int position) {
                head_name = headsNameList.get(position);
                Toast.makeText(changeHeadActivity.this, "头像修改成功", Toast.LENGTH_SHORT).show();

                Intent intent1 = new Intent(changeHeadActivity.this, personActivity.class);
                intent1.putExtra("user_id", user_id_intent);
                Log.d("head_name", head_name);
                new personAsyncTask().execute();
                startActivity(intent1);
            }

            @Override
            public void setOnLongItemClick(View view, int position) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("返回成功", "onOptionsItemSelected: ");
                Intent intent2 = new Intent(changeHeadActivity.this, personActivity.class);
                if (!user_id_intent.equals("")) {
                    Log.d("user_id", user_id_intent);
                    intent2.putExtra("user_id", user_id_intent);
                }
                startActivity(intent2);
                break;
        }
        return true;
    }

    private void initHeads() {
        Heads airjordan = new Heads("Air Jordan", R.mipmap.airjordan);
        headsList.add(airjordan);
        Heads anonymousmask = new Heads("V-Mask", R.mipmap.anonymousmask);
        headsList.add(anonymousmask);
        Heads bellsprout = new Heads("Bell Sprout", R.mipmap.bellsprout);
        headsList.add(bellsprout);
        Heads brutus = new Heads("Brutus", R.mipmap.brutus);
        headsList.add(brutus);
        Heads darthvader = new Heads("Darth Vader", R.mipmap.darthvader);
        headsList.add(darthvader);
        Heads dratini = new Heads("Dratini", R.mipmap.dratini);
        headsList.add(dratini);
        Heads facepalm = new Heads("Facepalm", R.mipmap.facepalm);
        headsList.add(facepalm);
        Heads gorilla = new Heads("Gorilla", R.mipmap.gorilla);
        headsList.add(gorilla);
        Heads jigglypuff = new Heads("Jigglypuff", R.mipmap.jigglypuff);
        headsList.add(jigglypuff);
        Heads moderatorfemale = new Heads("Mode Female", R.mipmap.moderatorfemale);
        headsList.add(moderatorfemale);
        Heads Moderatormale = new Heads("Mode Male", R.mipmap.moderatormale);
        headsList.add(Moderatormale);
        Heads oscar = new Heads("Oscar", R.mipmap.oscar);
        headsList.add(oscar);
        Heads pidgey = new Heads("Pidgey", R.mipmap.pidgey);
        headsList.add(pidgey);
        Heads pikachu = new Heads("Pikachu", R.mipmap.pikachu);
        headsList.add(pikachu);
        Heads pokecoin = new Heads("Pokecoin", R.mipmap.pokecoin);
        headsList.add(pokecoin);
        Heads shakespeare = new Heads("Shakespeare", R.mipmap.shakespeare);
        headsList.add(shakespeare);
        Heads snorlax = new Heads("Snorlax", R.mipmap.snorlax);
        headsList.add(snorlax);
        Heads theflashhead = new Heads("The Flash", R.mipmap.theflashhead);
        headsList.add(theflashhead);

        headsNameList.add("airjordan");
        headsNameList.add("anonymousmask");
        headsNameList.add("bellsprout");
        headsNameList.add("brutus");
        headsNameList.add("darthvader");
        headsNameList.add("dratini");
        headsNameList.add("facepalm");
        headsNameList.add("gorilla");
        headsNameList.add("jigglypuff");
        headsNameList.add("moderatorfemale");
        headsNameList.add("moderatormale");
        headsNameList.add("oscar");
        headsNameList.add("pidgey");
        headsNameList.add("pikachu");
        headsNameList.add("pokecoin");
        headsNameList.add("shakespeare");
        headsNameList.add("snorlax");
        headsNameList.add("theflashhead");
    }

    class personAsyncTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            postRequestWithOkHttp();
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

        }

        /**
         * Sending POST request of  with Okhttp
         *
         */
        private JSONObject postRequestWithOkHttp() {
            JSONObject json = null;

            RequestBody requestBody = new FormBody.Builder()
                    .add("user_id", user_id_intent)
                    .add("user_head", head_name)
                    .build();
            String url = "http://54.254.206.29/api/change_head";
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
