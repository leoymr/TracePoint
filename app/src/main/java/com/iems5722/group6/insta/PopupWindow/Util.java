package com.iems5722.group6.insta.PopupWindow;

import android.content.Context;

/**
 * Created by leoymr on 23/4/17.
 */

public class Util {

    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int dip2px(Context context, float px) {
        final float scale = getScreenDensity(context);
        return (int) (px * scale + 0.5);
    }
}
