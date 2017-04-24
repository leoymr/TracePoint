package com.example.leoymr.insta.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.leoymr.insta.Data.footprint_Info.PersonalData;
import com.example.leoymr.insta.R;

import java.util.List;

/**
 * Created by leoymr on 24/4/17.
 */

public class PersonAdapter extends ArrayAdapter<PersonalData> {
    private int resourceId;

    public PersonAdapter(Context context, int resourceId, List<PersonalData> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PersonalData personalData = getItem(position);
        View view;
        PersonViewHolder personViewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            personViewHolder = new PersonViewHolder();
            personViewHolder.head_imageView = (ImageView) view.findViewById(R.id.person_item_image);
            personViewHolder.name_textView = (TextView) view.findViewById(R.id.person_item_name);
            personViewHolder.content_textView = (TextView) view.findViewById(R.id.person_item_content);
            view.setTag(personViewHolder);
        } else {
            view = convertView;
            personViewHolder = (PersonViewHolder) view.getTag();
        }

        personViewHolder.head_imageView.setImageResource(personalData.getImageId());
        personViewHolder.name_textView.setText(personalData.getItemname());
        personViewHolder.content_textView.setText(personalData.getItemcount());

        return view;
    }

    class PersonViewHolder {
        ImageView head_imageView;
        TextView name_textView;
        TextView content_textView;
    }
}
