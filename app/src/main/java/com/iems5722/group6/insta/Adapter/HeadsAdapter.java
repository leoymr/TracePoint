package com.iems5722.group6.insta.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iems5722.group6.insta.Data.footprint_Info.Heads;
import com.iems5722.group6.insta.R;

import java.util.List;

/**
 * Created by leoymr on 23/4/17.
 */

public class HeadsAdapter extends RecyclerView.Adapter<HeadsAdapter.headViewHolder> {
    private List<Heads> mHeadsList;

    static class headViewHolder extends RecyclerView.ViewHolder {
        View hView;
        ImageView headImage;
        TextView headName;

        public headViewHolder(View itemView) {
            super(itemView);
            hView = itemView;
            headImage = (ImageView) itemView.findViewById(R.id.head_image);
            headName = (TextView) itemView.findViewById(R.id.head_name);
        }
    }

    public HeadsAdapter(List<Heads> headsList) {
        mHeadsList = headsList;
    }

    //定义一个接口，响应点击事件
    protected OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        //点击事件的回调
        void setOnItemClick(View view, int position);

        void setOnLongItemClick(View view, int position);
    }

    public void setmOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    protected void initClick(headViewHolder holder, final int position) {
        //单击事件的回调
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mOnItemClickListener是在MainActivity调用之后传过来的，
                // 如果不为空，说明被调用了，把当前的position回调给MainActivity
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.setOnItemClick(v, position);
                }
            }
        });
        //长按事件的回调
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.setOnLongItemClick(v, position);
                }
                return true; //注意：一定要返回true，这样可以消费事件，让点击事件不再生效
            }
        });
    }

    @Override
    public headViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.head_item, parent, false);
        final headViewHolder holder = new headViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(headViewHolder holder, int position) {
        Heads heads = mHeadsList.get(position);
        holder.headImage.setImageResource(heads.getImgId());
        holder.headName.setText(heads.getHeads_name());
        initClick(holder, position);
    }

    @Override
    public int getItemCount() {
        return mHeadsList.size();
    }


}