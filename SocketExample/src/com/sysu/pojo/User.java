package com.sysu.pojo;

import java.io.Serializable;

public class User implements Serializable {
	public String name;
	public String ip;
	
	public String getIp() {
		return ip;
	}
	
	public String getName() {
		return name;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
