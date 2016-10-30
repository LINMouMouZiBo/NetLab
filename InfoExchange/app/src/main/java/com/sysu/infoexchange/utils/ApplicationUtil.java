package com.sysu.infoexchange.utils;

/**
 * Created by linzibo on 2016/10/29.
 */

import android.app.Application;

import com.sysu.infoexchange.socket.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ApplicationUtil extends Application{
    //    这个类用于存一下全局变量
    private Client client; // client作为全局，保证app运行中只有一个socket与服务器通讯
    private String clinetName;

    // 这两个变量是用于p2p对话另一端的信息
    public String receivedip;
    public String receivedName;

    public void initClient(String ip, int port) {
        client = new Client(ip, port);
    }

    public String getClientName() {
        return clinetName;
    }

    public void setClientName(String name) {
        clinetName = name;
    }

    public void closeClient() {
        if (client != null)
            client.close();
        client = null;
    }

    public Client getClient() {
        return client;
    }

}
