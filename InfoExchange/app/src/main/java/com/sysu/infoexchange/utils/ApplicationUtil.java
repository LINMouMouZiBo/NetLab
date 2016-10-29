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

    private Socket socket;
    private DataOutputStream out = null;
    private DataInputStream in = null;
    private Client client; // client作为全局，保证app运行中只有一个socket与服务器通讯

    public void initClient(String ip, int port) {
        client = new Client(ip, port);
    }

    public void closeClient() {
        client.close();
        client = null;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void init() throws IOException, Exception{
        this.socket = new Socket("192.168.1.104",10202);
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

}
