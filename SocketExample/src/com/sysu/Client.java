package com.sysu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
 
public class Client extends Socket{
 
    private static final String SERVER_IP ="127.0.0.1";
    private static final int SERVER_PORT =2013;
     
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader wt;
     
    /**
     * 与服务器连接，并输入发送消息
     */
    public Client()throws Exception{
        super(SERVER_IP, SERVER_PORT);
        client =this;
        out =new PrintWriter(this.getOutputStream(),true);
        in =new BufferedReader(new InputStreamReader(this.getInputStream(), "UTF-8"));
    	wt =new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        new readLineThread();
         
        while(true){
            String input = wt.readLine();
            String str=new String(input.getBytes("gbk"), "UTF-8"); 
//            input = ;  
//            System.out.println(input.getBytes("UTF-8").toString());
            out.println(input);
            out.flush();
        }
    }
     
    /**
     * 用于监听服务器端向客户端发送消息线程类
     */
    class readLineThread extends Thread{
         
        private BufferedReader buff;
        public readLineThread(){
            try {
                buff =new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                start();
            }catch (Exception e) {
            	e.printStackTrace();
            }
        }
         
        @Override
        public void run() {
            try {
                while(true){
                    String result = buff.readLine();
                    if("byeClient".equals(result)){//客户端申请退出，服务端返回确认退出
                        break;
                    }else{//输出服务端发送消息
                        System.out.println(result);
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
