package com.sysu.infoexchange.pojo;

import java.io.Serializable;

public class MsgText implements Serializable {
    public String time;
    public String userName;

    // 目标位置
    // 空字符表示聊天室内的消息
    public String dst;

    //	0: 系统信息, 1: 聊天信息, 2: 图片, 3: 显示在线用户列表
    public String type;

    // 传送的数据
    public String msg;

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
        sb.append(", msg");
        sb.append(":");
        sb.append(msg);
        sb.append(", type");
        sb.append(":");
        sb.append(type);
        sb.append(")");
        return sb.toString();
    }

    public static MsgText toMess(String msg) {
        msg = msg.replace("(", "");
        msg = msg.replace(")", "");
        String[] item = msg.split(",");
        //  部分msg不是标准的数据形式，无法转化为mesText
        if (item.length < 4) {
            return null;
        }
        MsgText message = new MsgText();
        message.setType(item[3].substring(item[3].indexOf(':') + 1));
        message.setMsg(item[2].substring(item[2].indexOf(':') + 1));
        message.setTime(item[1].substring(item[1].indexOf(':') + 1));
        message.setUserName(item[0].substring(item[0].indexOf(':') + 1));
        return message;
    }
}
