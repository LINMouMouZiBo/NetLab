package com.sysu.infoexchange.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sysu.infoexchange.pojo.MsgText;

import java.io.ByteArrayOutputStream;

/**
 * Created by linzibo on 2016/10/30.
 */

public class BitmapUtils {
    public static MsgText toMess(String text) {
        text = text.replace("(", "");
        text = text.replace(")", "");
        String[] item = text.split(",");
        //  部分text不是标准的数据形式，无法转化为mesText
        if (item.length < 4) {
            return null;
        }
        MsgText message = new MsgText();
        message.setType(item[3].substring(item[3].indexOf(':') + 1));
        message.setText(item[2].substring(item[2].indexOf(':') + 1));
        message.setTime(item[1].substring(item[1].indexOf(':') + 1));
        message.setUserName(item[0].substring(item[0].indexOf(':') + 1));
        return message;
    }

    public static byte[] getBytes(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
        return baos.toByteArray();
    }

    public static Bitmap getBitmap(byte[] data){
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
