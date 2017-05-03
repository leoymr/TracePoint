package com.iems5722.group6.insta.Adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iems5722.group6.insta.Data.footprint_Info.content_Info;
import com.iems5722.group6.insta.Layout.CusListView;
import com.iems5722.group6.insta.R;
import com.like.LikeButton;
import com.like.OnLikeListener;

import org.json.JSONObject;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by leoymr on 15/4/17.
 *
 * 主页面listview的adapter
 */

public class CommentAdapter extends BaseAdapter implements OnLikeListener {

    private static final String POST = "post like";
    private Context context;
    private List<content_Info> list;
    private TextView LikeNum;
    private LikeButton btnLike;
    private content_Info item;
    private String trace_id_intent;
    private String user_id_intent;
    private String code;

    public CommentAdapter(Context context, List<content_Info> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public content_Info getItem(int i) {
        return list == null ? null : list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.content_listview, viewGroup, false);
        }

        TextView tv_userName = ViewHolder.get(view, R.id.fp_user_name);
        TextView tv_content = ViewHolder.get(view, R.id.fp_content_text);
        TextView tv_dateTime = ViewHolder.get(view, R.id.fp_time);
        ImageView imageView_head = ViewHolder.get(view, R.id.fp_user_img);
        CusListView listView = ViewHolder.get(view, R.id.fp_comment_listview);

        btnLike = ViewHolder.get(view, R.id.heart_button);

        LikeNum = ViewHolder.get(view, R.id.like_num);

        LinearLayout com = ViewHolder.get(view, R.id.comment_Linearlayout);

        item = getItem(i);
        imageView_head.setImageResource(item.getResourceId());
        tv_userName.setText(item.getUser_name());
        tv_content.setText(item.getContent());
        tv_dateTime.setText(item.getLocation_name());

        if (item.getComment_list().isEmpty()) {
            com.setVisibility(View.GONE);
        } else {
            listView.setList(item.getComment_list());
            com.setVisibility(View.VISIBLE);
        }

        user_id_intent = item.getUser_id();
        trace_id_intent = item.getTrace_id();

        LikeNum.setText(String.valueOf(item.getLikeNum()));
        if (item.isLikeFocus() == false) {
            btnLike.setLiked(false);
            btnLike.setOnLikeListener(this);
        } else {
            btnLike.setLiked(true);
        }

        return view;
    }

    /**
     * 异步处理点赞操作
     */
    class likeAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            code = String.valueOf(postRequestWithOkHttp());
            Log.d("adapter like code", String.valueOf(code));
            JSONObject json = null;
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            //api返回的code为200，则当前用户未点赞
            if (code.equals("200")) {
                item.setLikeNum(item.getLikeNum() + 1);
                LikeNum.setText(String.valueOf(item.getLikeNum()));

                Toast.makeText(context, "赞+1", Toast.LENGTH_SHORT).show();
            } else if (code.equals("400")) {

                Toast.makeText(context, "点过赞了", Toast.LENGTH_SHORT).show();
            }
            item.setLikeFocus(true);
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

    @Override
    public void liked(LikeButton likeButton) {
        new likeAsyncTask().execute();
    }

    @Override
    public void unLiked(LikeButton likeButton) {
        Toast.makeText(context, "点过赞了", Toast.LENGTH_SHORT).show();
        likeButton.setLiked(true);
    }
}




