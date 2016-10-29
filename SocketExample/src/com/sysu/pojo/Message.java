package com.sysu.pojo;

import java.io.Serializable;

public class Message implements Serializable {
	public String msg;
	public String time;
	public String userName;
	//	0:代表系统的信息；1：为聊天信息
	public String type;
	
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
}
