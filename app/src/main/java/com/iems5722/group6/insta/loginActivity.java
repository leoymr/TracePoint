package com.iems5722.group6.insta;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.iems5722.group6.insta.FirebaseService.MyFirebaseInstanceIDService;
import com.iems5722.group6.insta.Layout.AndroidBug5497Workaround;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

    private boolean isGooglePlayAvailable = false;
    private String token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AndroidBug5497Workaround.assistActivity(this);

        setTitle("登录");

        Intent intent = getIntent();
        String account = "";
        String pwd = "";


        List<String> permissionList = new ArrayList<>();

        //检测权限list，若无则添加
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(loginActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(loginActivity.this, permissions, 1);
        }

        isGooglePlayAvailable = isGooglePlayServicesAvailable(this);
        Log.d("isGoogleService", String.valueOf(isGooglePlayAvailable));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        rememPwd = (CheckBox) findViewById(R.id.remember_pwd);
        login = (Button) findViewById(R.id.loginBtn);
        register = (Button) findViewById(R.id.register_login);

        editText_user_email = (EditText) findViewById(R.id.loginId_edtxt);
        editText_user_pw = (EditText) findViewById(R.id.loginPwd_edtxt);

        boolean isRemember = preferences.getBoolean("remember_password", false);
        if (isRemember && (intent.getStringExtra("user_id") == null)) {
            account = preferences.getString("user_email", "");
            pwd = preferences.getString("user_pw", "");
            editText_user_email.setText(account);
            editText_user_pw.setText(pwd);
            rememPwd.setChecked(true);
        } else if (intent.getStringExtra("user_id") != null) {
            account = intent.getStringExtra("user_id");
            pwd = intent.getStringExtra("user_pwd");
            Log.d("register", account);
            editText_user_email.setText(account);
            editText_user_pw.setText(pwd);
        }

        token = FirebaseInstanceId.getInstance().getToken();

        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    public String load() {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = openFileInput("token.txt");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d("token reader", content.toString());
        return content.toString();
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
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
            return postRequestWithOkHttp();
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
         * Sending POST request with Okhttp
         */
        private String postRequestWithOkHttp() {
            Response response = null;
            String code = null;
            JSONObject json = null;
            if (token != null) {
                Log.d("Token: ", token);
            } else {
                token = load();
            }
            String url = "http://54.254.206.29/api/login";
            Log.d("postRequestWithOkHttp: ", url);
            RequestBody requestBody = new FormBody.Builder()
                    .add("user_email", user_email)
                    .add("user_pw", user_pw)
                    .add("user_token", token)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
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
