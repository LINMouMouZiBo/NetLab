package com.sysu.infoexchange.pojo;

import android.graphics.Bitmap;

/**
 * Created by zhuangqh on 2016/10/31.
 */

public class MessageItem {
    String text;
    String nameAndTime;
    Bitmap image;

    public Bitmap getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getNameAndTime() {
        return nameAndTime;
    }

    public void setNameAndTime(String nameAndTime) {
        this.nameAndTime = nameAndTime;
    }
}
