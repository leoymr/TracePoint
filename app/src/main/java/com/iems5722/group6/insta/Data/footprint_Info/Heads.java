package com.iems5722.group6.insta.Data.footprint_Info;

/**
 * Created by leoymr on 23/4/17.
 */

public class Heads {
    private String heads_name;
    private int imgId;

    public Heads(String heads_name, int imgId) {
        this.heads_name = heads_name;
        this.imgId = imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getImgId() {
        return imgId;
    }

    public void setHeads_name(String heads_name) {
        this.heads_name = heads_name;
    }

    public String getHeads_name() {
        return heads_name;
    }
}
