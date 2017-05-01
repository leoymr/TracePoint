package com.iems5722.group6.insta.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iems5722.group6.insta.Data.footprint_Info.content_Info;
import com.iems5722.group6.insta.Layout.CusListView;
import com.iems5722.group6.insta.R;

import java.util.List;

/**
 * Created by leoymr on 15/4/17.
 */

public class CommentAdapter extends BaseAdapter {

    private static final String POST = "post like";
    private Context context;
    private List<content_Info> list;
    private TextView LikeNum;
    private ImageView btnLike;
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

        btnLike = ViewHolder.get(view, R.id.good);

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

        if (item.isLikeFocus()) {
            btnLike.setImageResource(R.mipmap.collection_checked);
        }

        return view;
    }


}




