package com.example.leoymr.insta.Data.footprint_Info;

/**
 * Created by leoymr on 24/4/17.
 */

public class PersonalData {
    private String itemname;
    private int imageId;
    private String itemcount;

    public PersonalData(String itemname, int imageId, String itemcont) {
        this.itemname = itemname;
        this.imageId = imageId;
        this.itemcount = itemcont;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setItemcount(String itemcount) {
        this.itemcount = itemcount;
    }

    public String getItemname() {
        return itemname;
    }

    public int getImageId() {
        return imageId;
    }

    public String getItemcount() {
        return itemcount;
    }
}