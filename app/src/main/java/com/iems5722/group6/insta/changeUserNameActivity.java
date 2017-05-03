package com.iems5722.group6.insta;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 24/4/17.
 *
 * 更换用户名activity
 */

public class changeUserNameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String POST = "changeUserNameActivity";
    private EditText editText;
    private static Pattern pattern;
    private static Matcher matcher;
    private String user_id_intent;
    private String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("更改用户名");

        Intent i = getIntent();
        user_id_intent = i.getStringExtra("user_id");

        setContentView(R.layout.activity_change_user_name);
        ImageButton imageButton = (ImageButton) findViewById(R.id.confirm);
        editText = (EditText) findViewById(R.id.text_name);
        imageButton.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("返回成功", "onOptionsItemSelected: ");
                Intent intent2 = new Intent(changeUserNameActivity.this, personActivity.class);
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
            case R.id.confirm:
                user_name = editText.getText().toString().trim();
                if (user_name.equals("")) {
                    //用户名不为空
                    Toast.makeText(changeUserNameActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidName(user_name)) {
                    //验证用户名
                    Toast.makeText(changeUserNameActivity.this, "请输入不超过20位由数字、字母、下划线和连字符组成的用户名", Toast.LENGTH_SHORT).show();
                } else {

                    new publishAsyncTask().execute();
                    Intent intent = new Intent(changeUserNameActivity.this, personActivity.class);
                    Toast.makeText(changeUserNameActivity.this, "用户名修改成功", Toast.LENGTH_SHORT).show();
                    intent.putExtra("user_id", user_id_intent);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 验证用户名是否正确
     *
     * @param name
     * @return
     */
    private boolean isValidName(String name) {
        boolean flag = false;
        //匹配不超过20位由数字、字母、下划线和连字符组成的用户名
        String REGEX_pwd = "^[a-zA-Z0-9_-]{3,15}$";

        pattern = Pattern.compile(REGEX_pwd);
        matcher = pattern.matcher(name);
        if (matcher.matches())
            flag = true;
        return flag;
    }

    class publishAsyncTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            int code = postRequestWithOkHttp();
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            if (code == 200) {
                Log.d("code change name", String.valueOf(code));
            } else if (code == 500) {
                Log.d("code change name", String.valueOf(code));

            }

        }

        /**
         * Sending POST request of  with Okhttp
         */
        private int postRequestWithOkHttp() {
            JSONObject json = null;
            int code = 0;
            RequestBody requestBody = new FormBody.Builder()
                    .add("user_id", user_id_intent)
                    .add("user_name", user_name)
                    .build();
            String url = "http://54.254.206.29/api/change_name";
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
                code = (int) json.get("code");
                Log.d(POST, String.valueOf(json.get("status")));
                Log.d(POST, String.valueOf(json.get("message")));
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(POST, "POST request error");
            }
            return code;
        }
    }

}
