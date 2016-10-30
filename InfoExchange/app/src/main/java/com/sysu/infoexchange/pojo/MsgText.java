package com.sysu.infoexchange.pojo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.sysu.infoexchange.utils.DateUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MsgText implements Serializable {
    //    private static long serialVersionUID = 1113799434508676095L;
    public String time;
    public String userName;
    // 信息发送源ip
    public String ip;

    // 目标位置
    // 空字符表示聊天室内的消息
    public String dst;

    //	0: 系统信息, 1: 聊天信息, 2: 图片, 3: 显示在线用户列表
    public String type;

    // 传送的数据
    public String text;
    public byte[] image;

    public MsgText() {
        time = "";
        userName = "";
        ip = "";
        dst = "";
        type = "";
        text = "";
        image = new byte[1];
    }

    public MsgText(String text, String time, String userName) {
        this.text = text;
        this.time = time;
        this.userName = userName;
    }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append("(userName");
        sb.append(":");
        sb.append(userName);
        sb.append(", time");
        sb.append(":");
        sb.append(time);
        sb.append(", target");
        sb.append(":");
        sb.append(dst);
        sb.append(", text");
        sb.append(":");
        sb.append(text);
        sb.append(", type");
        sb.append(":");
        sb.append(type);
        sb.append(")");
        return sb.toString();
    }


    /*
 * type 为0 代表这是系统发出的提示信息，否则为用户信息
 */
    public static MsgText fromText(String username, String text, String type) {
        if (text == null) {
            return null;
        }
        MsgText message = new MsgText();
        message.setText(text);
        message.setTime(DateUtil.getDateString(DateUtil.getCurrrentDate()));
        message.setUserName(username);
        message.setType(type);

        if ("0".equals(type) || "3".equals(type)) {
            message.setUserName("系统消息");
        }

        return message;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(time);
        out.writeObject(userName);
        out.writeObject(ip);
        out.writeObject(dst);
        out.writeObject(type);

        if ("0".equals(type) || "1".equals(type)) {
            out.writeObject(text);
        } else if ("2".equals(type)) {
            final int len = image.length;
            out.writeInt(len);
            out.write(image, 0, len);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        time = (String) in.readObject();
        userName = (String) in.readObject();
        ip = (String)in.readObject();
        dst = (String) in.readObject();
        type = (String) in.readObject();

        if ("0".equals(type) || "1".equals(type)) {
            text = (String) in.readObject();
        } else if ("2".equals(type)) {
            final int len = in.readInt();
            if (in.read(image, 0, len) != len) {
                throw new IOException();
            }
        }
    }
}
