package com.example.leoymr.insta;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

/**
 * Created by leoymr on 15/4/17.
 */

public class tabViewActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewStub[] viewStub = new ViewStub[3];
    private Button currentBtn;
    private Button lastBtn;
    Button Btn_footprint;
    Button Btn_publish;
    Button Btn_person;

    private int[] tabBtnIds = {R.id.btn_footprint, R.id.btn_publish,
            R.id.btn_person};

    private Button[] tabBtn = new Button[3];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabview);
        viewStub[0] = (ViewStub) findViewById(R.id.footrint_ViewStub);
        viewStub[1] = (ViewStub) findViewById(R.id.publish_ViewStub);
        viewStub[2] = (ViewStub) findViewById(R.id.person_ViewStub);
        currentBtn = (Button) findViewById(R.id.btn_footprint);
        for (int i = 0; i < tabBtnIds.length; i++) {
            tabBtn[i] = (Button) findViewById(tabBtnIds[i]);
            tabBtn[i].setOnClickListener(this);
        }
        viewStub[0].setVisibility(View.VISIBLE);
        currentBtn.setTextColor(getResources().getColor(R.color.btn_activity));
        currentBtn.setGravity(Gravity.CENTER_HORIZONTAL);

        initView();
    }


    @Override
    public void onClick(View v) {
        lastBtn = currentBtn;
        currentBtn = (Button) v;
        if (currentBtn.getId() == lastBtn.getId()) {
            return;
        }
        currentBtn.setTextColor(getResources().getColor(R.color.btn_activity));
        currentBtn.setGravity(Gravity.CENTER_HORIZONTAL);
        lastBtn.setTextColor(Color.GRAY);
        lastBtn.setGravity(Gravity.CENTER_HORIZONTAL);
        int currentIndex = -1;
        switch (currentBtn.getId()) {
            case R.id.btn_footprint:
                currentIndex = 0;
                break;
            case R.id.btn_publish:
                currentIndex = 1;
                break;
            case R.id.btn_person:
                currentIndex = 2;
                break;
        }
        for (int i = 0; i < viewStub.length; i++) {
            viewStub[i].setVisibility(View.INVISIBLE);
        }
        for (int i = 0; i < viewStub.length; i++) {
            if (currentIndex == -1) {
                break;
            }
            if (currentIndex != i) {
                viewStub[i].setVisibility(View.INVISIBLE);
            } else {

                viewStub[i].setVisibility(View.VISIBLE);
            }
        }
    }

    private void initView() {
        Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");//记得加上这句

        Btn_footprint = (Button) findViewById(R.id.btn_footprint);
        Btn_publish = (Button) findViewById(R.id.btn_publish);
        Btn_person = (Button) findViewById(R.id.btn_person);

        Btn_footprint.setTypeface(font);
        Btn_publish.setTypeface(font);
        Btn_person.setTypeface(font);
    }

}
