package com.iems5722.group6.insta;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 23/4/17.
 */

public class commentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String POST = "post comment";
    private Button comment_sendBtn;
    private EditText comment_editTxt;
    private String user_id_intent;
    private String trace_id_intent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        setTitle("发表评论");


        comment_sendBtn = (Button) findViewById(R.id.comment_send);
        comment_editTxt = (EditText) findViewById(R.id.comment_edittext_content);

        comment_sendBtn.setOnClickListener(this);

        Intent intent = getIntent();
        user_id_intent = intent.getStringExtra("user_id");
        trace_id_intent = intent.getStringExtra("trace_id");

        Log.d("commentA user_id", user_id_intent);
        Log.d("commentA trace_id", trace_id_intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("返回成功", "onOptionsItemSelected: ");
                Intent intent2 = new Intent(commentActivity.this, mapActivity.class);
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

    @Override
    public void onClick(View v) {
        String comment = comment_editTxt.getText().toString().trim();
        if (comment.equals("")) {
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
            new commentAsyncTask().execute(comment);
            Log.d("评论内容", comment);

            Intent intent2 = new Intent(commentActivity.this, mapActivity.class);
            if (!user_id_intent.equals("")) {
                Log.d("user_id", user_id_intent);
                intent2.putExtra("user_id", user_id_intent);
                intent2.putExtra("intent_flag", "1");
                startActivity(intent2);
            }
        }
    }

    class commentAsyncTask extends AsyncTask<String, Void, JSONObject> {

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
         * @param msg comments
         */
        private JSONObject postRequestWithOkHttp(String msg) {
            JSONObject json = null;


            RequestBody requestBody = new FormBody.Builder()
                    .add("user_id", user_id_intent)
                    .add("trace_id", trace_id_intent)
                    .add("text", msg)
                    .build();
            String url = "http://54.254.206.29/api/publish_comment";
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
