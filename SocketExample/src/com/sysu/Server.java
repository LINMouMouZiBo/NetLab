package com.sysu;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.oracle.tools.packager.Log;
import com.sysu.pojo.Message;
import com.sysu.pojo.User;

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
						User user = thread.getUser();
						if (user.getChattingTarget().equals(message.dst)) {
							Log.info("boardcast... " + message.toString());
							thread.sendMessage(message);
						}
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
		private ObjectOutputStream oos;
		private ObjectInputStream ois;
		private User user;

		public ServerThread(Socket s) throws IOException {
			client = s;
			oos = new ObjectOutputStream(client.getOutputStream());
			ois = new ObjectInputStream(client.getInputStream());
			Message msg = Message.fromText("系统消息", "成功连上聊天室,请输入你的名字：");

			oos.writeObject(msg);
			start();
		}

		@Override
		public void run() {
			try {
				boolean isFirst = true;

				Message message;
				while ((message = (Message)ois.readObject()) != null) {
					String str = message.getText();

					// 查看在线用户列表
					if ("showuser".equals(str)) {
						oos.writeObject(this.getOnlineUsers());
						continue;
					}

					if ("chat with:".equals(str.substring(0, 9))) {
						user.setChattingTarget(str.substring(10));
						Log.info(user.getName() + "enter P2P chatting");
						continue;
					}

					// 第一次进入，保存名字
					if (isFirst) {
						isFirst = false;
						user = new User();
						user.setIp(client.getInetAddress().getHostAddress());
						user.setName(str);
						thread_list.add(this);
						oos.writeObject(Message.fromText(user.getName(), "你好,可以开始聊天了..."));
						this.pushMessage(Message.fromText("系统消息", "Client<" + user.getName() + ">进入聊天室..."));

						Log.info(user.getName() + "enter system");
					} else {
						this.pushMessage(message); // 直接转发
					}
				}
				oos.writeObject(Message.fromText("系统消息", "byeClient"));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {// 用户退出聊天室
				try {
					client.close();
					oos.close();
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				thread_list.remove(this);
//				user_list.remove(name);
				pushMessage(Message.fromText("系统消息", "Client<" + user.getName() + ">退出了聊天室"));
			}
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
		private void sendMessage(Message msg) throws IOException {
			oos.writeObject(msg);
		}

		// 统计在线用户列表
		private Message getOnlineUsers() {
			String onlineUserList = "--- 在线用户列表 ---\015\012";
			for (ServerThread aThread_list : thread_list) {
				onlineUserList += "[" + aThread_list.getUser().getName() + "] "
						+ aThread_list.getUser().getIp() + "\015\012";
			}
			onlineUserList += "--------------------";

			return Message.fromText("系统消息", onlineUserList);
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("启动");
		new Server();// 启动服务端
	}
}
