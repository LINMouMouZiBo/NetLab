package com.sysu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.sysu.infoexchange.pojo.MsgText;
 
public class Client extends Socket{
 
    private static final String SERVER_IP ="127.0.0.1";
    private static final int SERVER_PORT =2013;
     
    private Socket client;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private BufferedReader wt;
     
    /**
     * 与服务器连接，并输入发送消息
     */
    public Client()throws Exception{
        super(SERVER_IP, SERVER_PORT);
        client =this;
        out = new ObjectOutputStream(this.getOutputStream());
        in =new ObjectInputStream(this.getInputStream());
    	wt =new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        new readLineThread();
         
        while(true){
            String input = wt.readLine();
            String str=new String(input.getBytes("gbk"), "UTF-8"); 
//            input = ;  
//            System.out.println(input.getBytes("UTF-8").toString());
			MsgText msg = MsgText.fromText("系统消息", input, "0");

			out.writeObject(msg);
            out.flush();
        }
    }
     
    /**
     * 用于监听服务器端向客户端发送消息线程类
     */
    class readLineThread extends Thread{
         
        private ObjectInputStream buff;
        public readLineThread(){
            try {
//            	buff = new ObjectInputStream(client.getInputStream());
//                buff =new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                start();
            }catch (Exception e) {
            	e.printStackTrace();
            }
        }
         
        @Override
        public void run() {
            try {
                while(true){
                	MsgText message;
//                    String result = buff.readLine();
                    if((message = (MsgText)in.readObject()) == null){//客户端申请退出，服务端返回确认退出
                        break;
                    }else{//输出服务端发送消息
                        System.out.println(message.toString());
                    }
                }
            }catch (Exception e) {
            } finally {
                try {
					in.close();
	                wt.close();
	                out.close();
	                client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
    }
     
    public static void main(String[] args) {
        try {
            System.out.println("客户端启动");
            new Client();//启动客户端
        }catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
