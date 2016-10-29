package com.sysu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sysu.pojo.Message;
import com.sysu.pojo.User;
import com.sysu.utils.DateUtil;

public class Server extends ServerSocket {

	private static final int SERVER_PORT = 2013;

	private static List<ServerThread> thread_list = new ArrayList<ServerThread>();// 服务器已启用线程集合
	private static LinkedList<Message> message_list = new LinkedList<Message>();// 存放消息队列

	private static Lock lock = new ReentrantLock(true);
	private static Condition notEmpty = lock.newCondition();

	/**
	 * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
	 */
	public Server() throws IOException {
		super(SERVER_PORT);// 创建ServerSocket
		new PrintOutThread();// 创建向客户端发送消息线程

		try {
			while (true) {// 监听客户端请求，启个线程处理
				Socket socket = accept();
				new ServerThread(socket);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	/**
	 * 监听是否有输出消息请求线程类,向客户端发送消息
	 */
	class PrintOutThread extends Thread {

		public PrintOutThread() {
			start();
		}

		@Override
		public void run() {
			while (true) {
				lock.lock();
				try {
					while (message_list.size() <= 0) {
						notEmpty.await();
					}
					// 将缓存在队列中的消息按顺序发送到各客户端，并从队列中清除。
					// System.out.println(thread_list.size());
					Message message = message_list.getFirst();
					for (ServerThread thread : thread_list) {
						thread.sendMessage(message);
					}
					message_list.removeFirst();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}

	/**
	 * 服务器线程类
	 */
	class ServerThread extends Thread {
		private Socket client;
		private PrintWriter out;
		private BufferedReader in;
		private User user;

		public ServerThread(Socket s) throws IOException {
			client = s;
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					client.getInputStream(), "UTF-8"));
			Message msg = produceMsg("成功连上聊天室,请输入你的名字：", 0);
			out.println(msg.toString());
			start();
		}

		@Override
		public void run() {
			try {
				int flag = 0;
				String line = "";
				while (!"bye".equals(line = in.readLine())) {
					if (line == null || "".equals(line)) {
						continue;
					}
					// 查看在线用户列表
					if ("showuser".equals(line)) {
						out.println(this.listOnlineUsers());
						continue;
					}
					// 第一次进入，保存名字
					if (flag++ == 0) {
						user = new User();
						user.setIp(client.getInetAddress().getHostAddress());
						user.setName(line);
						thread_list.add(this);
						out.println(user.getName() + "你好,可以开始聊天了...");
						this.pushMessage(produceMsg("Client<" + user.getName()
								+ ">进入聊天室...", 0));
					} else {
						this.pushMessage(produceMsg(line, 1));
					}
				}
				out.println("byeClient");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {// 用户退出聊天室
				try {
					client.close();
					out.close();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				thread_list.remove(this);
//				user_list.remove(name);
				pushMessage(produceMsg("Client<" + user.getName() + ">退出了聊天室", 0));
			}
		}

		/*
		 * type 为0 代表这是系统发出的提示信息，否则为用户信息
		 */
		private Message produceMsg(String msg, int type) {
			if (msg == null) {
				return null;
			}
			Message message = new Message();
			message.setMsg(msg);
			message.setType(String.valueOf(type));
			message.setTime(DateUtil.getDateString(DateUtil.getCurrrentDate()));
			if (type == 0) {
				message.setUserName("系统提示");
			} else {
				message.setUserName(user.getName());
			}
			return message;
		}
		
		// 获取该线程的user
		private User getUser() {
			return user;
		}

		// 放入消息队列末尾，准备发送给客户端
		private void pushMessage(Message msg) {
			lock.lock();
			try {
				if (msg == null) {
					return;
				}
				message_list.addLast(msg);
				notEmpty.signalAll();
			} catch (Exception e) {
			} finally {
				lock.unlock();
			}
		}

		// 向客户端发送一条消息
		private void sendMessage(Message msg) {
			out.println(msg.toString());
		}

		// 统计在线用户列表
		private String listOnlineUsers() {
			String s = "--- 在线用户列表 ---\015\012";
			for (int i = 0; i < thread_list.size(); i++) {
				s += "[" + thread_list.get(i).getUser().getName() + "] "
						+ thread_list.get(i).getUser().getIp() + "\015\012";
			}
			s += "--------------------";
			return s;
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("启动");
		new Server();// 启动服务端
	}
}
