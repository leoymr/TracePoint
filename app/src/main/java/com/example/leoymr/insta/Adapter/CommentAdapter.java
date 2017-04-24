package com.example.leoymr.insta.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leoymr.insta.Data.footprint_Info.AnimationTools;
import com.example.leoymr.insta.Data.footprint_Info.content_Info;
import com.example.leoymr.insta.Layout.CusListView;
import com.example.leoymr.insta.R;

import java.util.List;

/**
 * Created by leoymr on 15/4/17.
 */

public class CommentAdapter extends BaseAdapter {

    private Context context;
    private List<content_Info> list;

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
        content_Info bean = list.get(i);

        TextView tv_userName = ViewHolder.get(view, R.id.fp_user_name);
        TextView tv_content = ViewHolder.get(view, R.id.fp_content_text);
        TextView tv_dateTime = ViewHolder.get(view, R.id.fp_time);
        ImageView imageView_head = ViewHolder.get(view,R.id.fp_user_img);
        CusListView listView = ViewHolder.get(view, R.id.fp_comment_listview);

        ImageView btnLike = ViewHolder.get(view, R.id.good);

        TextView LikeNum = ViewHolder.get(view, R.id.like_num);

        LinearLayout com = ViewHolder.get(view, R.id.comment_Linearlayout);

        content_Info item = getItem(i);
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

        if (bean.isLikeFocus()) {
            btnLike.setImageResource(R.mipmap.collection_checked);
        }
        LikeNum.setText(item.getLikeNum() + "");

        return view;
    }


}
