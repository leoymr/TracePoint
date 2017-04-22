package com.example.leoymr.insta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class loginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "login";

    private String user_email;
    private String user_id_intent;
    private String user_pw;

    private Button login;
    private Button register;
    private EditText editText_user_email;
    private EditText editText_user_pw;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private CheckBox rememPwd;

    private static Pattern pattern;
    private static Matcher matcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        rememPwd = (CheckBox) findViewById(R.id.remember_pwd);
        login = (Button) findViewById(R.id.loginBtn);
        register = (Button) findViewById(R.id.register_login);

        editText_user_email = (EditText) findViewById(R.id.loginId_edtxt);
        editText_user_pw = (EditText) findViewById(R.id.loginPwd_edtxt);

        boolean isRemember = preferences.getBoolean("remember_password", false);
        if (isRemember) {
            String account = preferences.getString("user_email", "");
            String pwd = preferences.getString("user_pw", "");
            editText_user_email.setText(account);
            editText_user_pw.setText(pwd);
            rememPwd.setChecked(true);
        }

        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        user_email = editText_user_email.getText().toString();
        user_pw = editText_user_pw.getText().toString();
        switch (v.getId()) {
            case R.id.loginBtn:
                if (user_email.equals("") || user_pw.equals("")) {
                    //验证输入不为空
                    Toast.makeText(loginActivity.this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                } else if (!isValidEmail(user_email)) {
                    //验证邮箱
                    Toast.makeText(loginActivity.this, "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
                } else if (!isValidPwd(user_pw)) {
                    //验证密码
                    Toast.makeText(loginActivity.this, "请输入6～20位，包含字母与数字的密码", Toast.LENGTH_SHORT).show();
                } else {
                    editor = preferences.edit();
                    if (rememPwd.isChecked()) {
                        editor.putBoolean("remember_password", true);
                        editor.putString("user_email", user_email);
                        editor.putString("user_pw", user_pw);
                    } else {
                        editor.clear();
                    }
                    editor.apply();
                    new LoginAsyncTask().execute();

                }
                break;
            case R.id.register_login:
                Intent intent = new Intent(loginActivity.this, registerActivity.class);
                startActivity(intent);
                break;
        }
    }

    class LoginAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return sendRequestWithOkHttp();
        }

        @Override
        protected void onPostExecute(String code) {
            super.onPostExecute(code);
            if (code.equals("200")) {
                Toast.makeText(loginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(loginActivity.this, mapActivity.class);
                if (!user_id_intent.equals("")) {
                    Log.d("user_id", user_id_intent);
                    intent.putExtra("user_id", user_id_intent);
                }
                startActivity(intent);
            } else if (code.equals("400") || code.equals("500")) {
                Toast.makeText(loginActivity.this, "用户名或密码有错，请重新输入", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Sending GET request with Okhttp
         */
        private String sendRequestWithOkHttp() {
            Response response = null;
            String code = null;
            JSONObject json = null;
            String url = null;
            if (user_email != null) {
                url = String.format("http://54.254.206.29:5000/api/login?%s&%s", "user_email=" + user_email, "user_pw=" + user_pw);
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
                    Log.d(TAG, "登录成功");
                }
                user_id_intent = json.get("user_id").toString();
                if (!user_id_intent.equals("")) {
                    Log.d("user_id", user_id_intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, code);
            return code;
        }
    }

    /**
     * 验证邮箱是否正确
     *
     * @param email
     * @return
     */
    private boolean isValidEmail(String email) {
        boolean flag = false;
        String REGEX_Email = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(REGEX_Email);
        matcher = pattern.matcher(email);
        if (matcher.matches())
            flag = true;
        return flag;
    }

    /**
     * 验证密码是否正确
     *
     * @param pwd
     * @return
     */
    private boolean isValidPwd(String pwd) {
        boolean flag = false;
        //匹配6～20位包含数字和字母的密码
        String REGEX_pwd = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$";

        pattern = Pattern.compile(REGEX_pwd);
        matcher = pattern.matcher(pwd);
        if (matcher.matches())
            flag = true;
        return flag;
    }
}
