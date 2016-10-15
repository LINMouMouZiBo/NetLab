package com.sysu.infoexchange.pojo;

import java.io.Serializable;

public class MsgText implements Serializable {
    public String msg;
    public String time;
    public String userName;

    public MsgText(String msg, String time, String userName) {
        this.msg = msg;
        this.time = time;
        this.userName = userName;
    }

    public MsgText() {}

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsg() {
        return msg;
    }

    public String getTime() {
        return time;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(":");
        sb.append("(userName");
        sb.append(":");
        sb.append(userName);
        sb.append(", time");
        sb.append(":");
        sb.append(time);
        sb.append(", msg");
        sb.append(":");
        sb.append(msg);
        sb.append(")");
        return sb.toString();
    }

    public static MsgText toMess(String msg) {
        msg = msg.replace("(", "");
        msg = msg.replace(")", "");
        String[] item = msg.split(",");
        //  部分msg不是标准的数据形式，无法转化为mesText
        if (item.length < 3) {
            return null;
        }
        MsgText message = new MsgText();
        message.setMsg(item[2].substring(item[2].indexOf(':') + 1));
        message.setTime(item[1].substring(item[1].indexOf(':') + 1));
        message.setUserName(item[0].substring(item[0].indexOf(':') + 1));
        return message;
    }
}
