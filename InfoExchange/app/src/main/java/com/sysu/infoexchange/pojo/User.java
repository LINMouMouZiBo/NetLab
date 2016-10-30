package com.sysu.infoexchange.pojo;

import java.io.Serializable;

public class User implements Serializable {
	private String name;
	private String ip;
	private String chattingTarget;

    public  User() {
        name = "";
        ip = "";
        chattingTarget = "";
    }

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

	public String getChattingTarget() {
		return chattingTarget;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setChattingTarget(String chattingTarget) {
		this.chattingTarget = chattingTarget;
	}
}
