package com.example.leoymr.insta.Data.footprint_Info;

import android.widget.ListView;

import java.util.List;

/**
 * Created by leoymr on 15/4/17.
 */

public class content_Info {
    private String user_name;
    private String location_name;
    private String content;
    private List<comment_Info> comment_list;

    /*public content_Info(String user_name,List<comment_Info> comment_list) {
        this.user_name = user_name;
        this.content = content;
        this.location_name = location_name;
        this.comment_list = comment_list;
    }*/

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getContent() {
        return content;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setComment_list(List<comment_Info> comment_list) {
        this.comment_list = comment_list;
    }

    public List<comment_Info> getComment_list() {
        return comment_list;
    }
}
