package com.example.leoymr.insta.Layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.leoymr.insta.Data.footprint_Info.comment_Info;
import com.example.leoymr.insta.R;

import java.util.List;

/**
 * Created by leoymr on 15/4/17.
 */

public class CusListView extends LinearLayout {
    public CusListView(Context context) {
        this(context, null);
    }

    public CusListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CusListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public void setList(List<comment_Info> list) {
        if (list == null) {
            throw new RuntimeException("list is null");
        }
        int count = list.size();
        removeAllViews();
        for (int i = 0; i < count; i++) {
            comment_Info comment_info = list.get(i);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.comment_item, null);
            TextView tv_name = f(view, R.id.fp_comment_user_name);
            TextView tv_comments = f(view, R.id.fp_comments);
            tv_name.setText(comment_info.getUser_name());
            tv_comments.setText(comment_info.getComments() + "");
            addView(view);
        }

    }

    private <T extends View> T f(View view, int id) {
        return (T) view.findViewById(id);
    }
}
