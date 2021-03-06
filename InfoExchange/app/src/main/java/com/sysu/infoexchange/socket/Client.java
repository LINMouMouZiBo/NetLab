package com.sysu.infoexchange.socket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.orhanobut.logger.Logger;
import com.sysu.infoexchange.pojo.MsgText;
import com.sysu.infoexchange.utils.DateUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// 与服务器通讯的socket
public class Client {

    private String SERVER_IP = "127.0.0.1";
    private int SERVER_PORT = 2013;

    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Handler handler;

    /**
     * 与服务器连接，并输入发送消息
     */

    public Client(String ip, int port) {
        SERVER_IP = ip;
        SERVER_PORT = port;
    }

    public String getIp() {
        return client.getInetAddress().getHostAddress();
    }

    public void contSocket() throws Exception {
        client = new Socket(SERVER_IP, SERVER_PORT);
        client.setSoTimeout(1000 * 60 * 5);
        out = new ObjectOutputStream(client.getOutputStream());
        /* 获取输出流 */
        in = new ObjectInputStream(client.getInputStream());
        new readLineThread();
    }

    public void setHandler(Handler h) {
        handler = h;
    }

    public void sendMsg(MsgText msg) throws IOException {
            out.writeObject(msg);
            out.flush();
    }

    public void close() {
        try {
            if (client != null && client.isConnected()) {
                sendMsg(MsgText.fromText("系统消息", "#bye", "0"));
            } else {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (client != null)
                    client.close();
                in = null;
                out = null;
                client = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
        }
    }

    /**
     * 用于监听服务器端向客户端发送消息线程类
     */
    class readLineThread extends Thread {

        public readLineThread() {
            try {
                start();
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    MsgText msgText = (MsgText) in.readObject();
                    String result = msgText.getText();
                    if ("#byeClient".equals(result)) {//客户端申请退出，服务端返回确认退出
                        break;
                    } else {//输出服务端发送消息
                        System.out.println(result);
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putSerializable("msg", msgText);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MsgText msgText = new MsgText("断开连接，退出聊天室",
                        DateUtil.getDateString(DateUtil.getCurrrentDate()), "系统提示");
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putSerializable("msg", msgText);
                msg.setData(data);
                handler.sendMessage(msg);
                close();
            }
        }
    }
}
